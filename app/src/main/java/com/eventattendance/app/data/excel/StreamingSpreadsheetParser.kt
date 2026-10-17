package com.eventattendance.app.data.excel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ParsedSpreadsheet(
    val headers: List<String>,
    val rows: List<List<String>>,
    val filename: String
)

object StreamingSpreadsheetParser {

    private fun createXmlParser(): XmlPullParser {
        return try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            factory.newPullParser()
        } catch (_: Throwable) {
            try {
                val clazz = Class.forName("org.kxml2.io.KXmlParser")
                clazz.getDeclaredConstructor().newInstance() as XmlPullParser
            } catch (_: Throwable) {
                android.util.Xml.newPullParser()
            }
        }
    }

    /**
     * Resolves the real human-readable display name from a content or file URI.
     * Prevents fallback to non-descriptive SAF document IDs (e.g. document:1000000001).
     */
    fun resolveFileName(context: Context, uri: Uri): String {
        var name: String? = null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = cursor.getString(index)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return name ?: uri.lastPathSegment?.substringAfterLast('/') ?: "imported_roster.xlsx"
    }

    /**
     * High-level entry point called from the UI.
     * Inspects magic bytes and MIME type so that .xlsx files are NEVER accidentally
     * routed to the CSV parser (which produces garbled ZIP binary headers like PK!#Jr [Content_Types].xml).
     */
    fun parse(context: Context, uri: Uri, fallbackFilename: String = ""): ParsedSpreadsheet {
        val resolvedFilename = resolveFileName(context, uri).ifBlank { fallbackFilename }
        val mimeType = try { context.contentResolver.getType(uri) ?: "" } catch (_: Exception) { "" }

        val rawStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not open file stream for selected document.")

        return parseStream(rawStream, resolvedFilename, mimeType)
    }

    /**
     * Parses an input stream using magic byte inspection for guaranteed format detection.
     */
    fun parseStream(inputStream: InputStream, filename: String, mimeType: String = ""): ParsedSpreadsheet {
        val bufferedStream = if (inputStream is BufferedInputStream) inputStream else BufferedInputStream(inputStream)
        bufferedStream.mark(16)
        val header = ByteArray(8)
        val bytesRead = bufferedStream.read(header)
        bufferedStream.reset()

        // Magic bytes for ZIP container (OOXML .xlsx): 'P', 'K' (0x50, 0x4B)
        val isZip = bytesRead >= 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()

        // Magic bytes for legacy OLE2 compound document (.xls): 0xD0, 0xCF, 0x11, 0xE0
        val isOle2 = bytesRead >= 4 &&
                header[0] == 0xD0.toByte() && header[1] == 0xCF.toByte() &&
                header[2] == 0x11.toByte() && header[3] == 0xE0.toByte()

        if (isOle2) {
            throw IllegalArgumentException(
                "Legacy Excel format (.xls) detected. Please open this file in Excel or Google Sheets and save/export it as modern Excel (.xlsx) or CSV (.csv) before importing."
            )
        }

        return when {
            isZip -> {
                parseXlsx(bufferedStream, filename)
            }
            filename.endsWith(".xlsx", ignoreCase = true) ||
            mimeType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ignoreCase = true) -> {
                parseXlsx(bufferedStream, filename)
            }
            else -> {
                parseCsv(bufferedStream, filename)
            }
        }
    }

    /**
     * Converts an Excel cell reference (e.g. "A1", "F98", "AA10") into a 0-based column index.
     * Prevents sparse/missing cell shifts in OOXML sheets.
     */
    fun cellRefToColumnIndex(ref: String): Int {
        var col = 0
        var foundAny = false
        for (ch in ref.uppercase()) {
            if (ch in 'A'..'Z') {
                col = col * 26 + (ch - 'A' + 1)
                foundAny = true
            } else {
                break
            }
        }
        return if (foundAny) col - 1 else -1
    }

    /**
     * Cleans and formats numeric cell values:
     * - Expands scientific notation (e.g. 1.20261E+13 -> 12026100000000 or full precision).
     * - Strips extraneous .0 from integer values (e.g. roll number 54.0 -> 54).
     */
    fun formatNumericCellValue(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""

        // Expand scientific notation (e.g. 1.20261E+13)
        if (trimmed.contains('E', ignoreCase = true)) {
            try {
                val bd = BigDecimal(trimmed)
                val plain = bd.toPlainString()
                return if (plain.contains('.')) plain.trimEnd('0').trimEnd('.') else plain
            } catch (_: Exception) {}
        }

        // Float with trailing .0 (e.g. roll number 54.0)
        if (trimmed.endsWith(".0")) {
            val before = trimmed.substring(0, trimmed.length - 2)
            if (before.all { it.isDigit() || it == '-' || it == '+' }) {
                return before
            }
        }

        return trimmed
    }

    /**
     * Streaming OOXML (.xlsx) parser that reads sharedStrings.xml and sheet1.xml
     * without high memory consumption or third-party binary bloat.
     */
    fun parseXlsx(inputStream: InputStream, filename: String): ParsedSpreadsheet {
        var sharedStringsBytes: ByteArray? = null
        val sheetMap = mutableMapOf<String, ByteArray>()

        // 1. Read entries from ZIP container safely into byte arrays
        ZipInputStream(inputStream).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                val entryName = entry.name.lowercase().replace('\\', '/')
                if (entryName == "xl/sharedstrings.xml") {
                    sharedStringsBytes = zip.readBytes()
                } else if (entryName.startsWith("xl/worksheets/sheet") && entryName.endsWith(".xml")) {
                    sheetMap[entryName] = zip.readBytes()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        // 2. Parse shared strings if present
        val sharedStrings = if (sharedStringsBytes != null) {
            parseSharedStrings(ByteArrayInputStream(sharedStringsBytes))
        } else {
            emptyList()
        }

        // 3. Find the primary worksheet (prioritizing sheet1.xml)
        val rawSheetBytes = sheetMap["xl/worksheets/sheet1.xml"]
            ?: sheetMap.entries.sortedBy { it.key }.firstOrNull()?.value

        if (rawSheetBytes == null) {
            return ParsedSpreadsheet(emptyList(), emptyList(), filename)
        }

        // 4. Parse sheet XML using streaming XmlPullParser
        val rows = mutableListOf<List<String>>()
        val parser = createXmlParser()
        parser.setInput(ByteArrayInputStream(rawSheetBytes), "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableListOf<String>()
        var cellRef: String? = null
        var cellType: String? = null
        val cellValue = StringBuilder()
        var insideCell = false
        var insideValue = false
        var insideInlineStr = false
        var lastColIndex = -1

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name.lowercase()
                    when (tagName) {
                        "row" -> {
                            currentRow = mutableListOf()
                            lastColIndex = -1
                        }
                        "c" -> {
                            insideCell = true
                            cellRef = parser.getAttributeValue(null, "r")
                            cellType = parser.getAttributeValue(null, "t")
                            cellValue.setLength(0)
                        }
                        "v" -> {
                            insideValue = true
                        }
                        "t" -> {
                            if (insideCell) {
                                insideInlineStr = true
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideValue || insideInlineStr) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name.lowercase()
                    when (tagName) {
                        "v" -> {
                            insideValue = false
                        }
                        "t" -> {
                            insideInlineStr = false
                        }
                        "c" -> {
                            insideCell = false
                            val targetCol = if (cellRef != null) cellRefToColumnIndex(cellRef) else lastColIndex + 1
                            if (targetCol >= 0) {
                                while (currentRow.size < targetCol) {
                                    currentRow.add("")
                                }
                            }

                            val raw = cellValue.toString().trim()
                            val text = when (cellType) {
                                "s" -> {
                                    val idx = raw.toIntOrNull()
                                    if (idx != null && idx in sharedStrings.indices) {
                                        sharedStrings[idx]
                                    } else {
                                        raw
                                    }
                                }
                                "b" -> {
                                    if (raw == "1") "TRUE" else if (raw == "0") "FALSE" else raw
                                }
                                "str", "inlineStr" -> {
                                    raw
                                }
                                else -> {
                                    formatNumericCellValue(raw)
                                }
                            }
                            currentRow.add(text)
                            lastColIndex = if (targetCol >= 0) targetCol else currentRow.size - 1
                        }
                        "row" -> {
                            if (currentRow.any { it.isNotBlank() }) {
                                rows.add(currentRow)
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        if (rows.isEmpty()) {
            return ParsedSpreadsheet(emptyList(), emptyList(), filename)
        }

        // Align column count across all rows based on header count
        val headerCount = rows.first().size
        val normalizedRows = rows.map { row ->
            if (row.size < headerCount) {
                row + List(headerCount - row.size) { "" }
            } else {
                row
            }
        }

        val headers = normalizedRows.first()
        val dataRows = normalizedRows.drop(1)
        return ParsedSpreadsheet(headers, dataRows, filename)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val list = mutableListOf<String>()
        val parser = createXmlParser()
        parser.setInput(stream, "UTF-8")
        var eventType = parser.eventType
        val currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name.equals("t", ignoreCase = true)) {
                        insideT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("t", ignoreCase = true)) {
                        insideT = false
                    } else if (parser.name.equals("si", ignoreCase = true)) {
                        list.add(currentText.toString())
                        currentText.setLength(0)
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    /**
     * RFC-4180 compliant CSV parser supporting quotes, embedded commas, newlines, and BOM markers.
     */
    fun parseCsv(inputStream: InputStream, filename: String): ParsedSpreadsheet {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        val currentField = StringBuilder()
        var insideQuotes = false

        reader.use { r ->
            var ch = r.read()
            // Skip UTF-8 BOM if present
            if (ch == 0xFEFF) {
                ch = r.read()
            }

            while (ch != -1) {
                val c = ch.toChar()
                when {
                    c == '"' -> {
                        insideQuotes = !insideQuotes
                    }
                    c == ',' && !insideQuotes -> {
                        val cleaned = formatNumericCellValue(currentField.toString())
                        currentRow.add(cleaned)
                        currentField.setLength(0)
                    }
                    (c == '\n' || c == '\r') && !insideQuotes -> {
                        if (c == '\r') {
                            r.mark(1)
                            val next = r.read()
                            if (next != '\n'.code && next != -1) {
                                r.reset()
                            }
                        }
                        val cleaned = formatNumericCellValue(currentField.toString())
                        currentRow.add(cleaned)
                        currentField.setLength(0)
                        if (currentRow.any { it.isNotBlank() }) {
                            rows.add(currentRow)
                        }
                        currentRow = mutableListOf()
                    }
                    else -> {
                        currentField.append(c)
                    }
                }
                ch = r.read()
            }

            if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
                val cleaned = formatNumericCellValue(currentField.toString())
                currentRow.add(cleaned)
                if (currentRow.any { it.isNotBlank() }) {
                    rows.add(currentRow)
                }
            }
        }

        if (rows.isEmpty()) {
            return ParsedSpreadsheet(emptyList(), emptyList(), filename)
        }

        val headerCount = rows.first().size
        val normalizedRows = rows.map { row ->
            if (row.size < headerCount) {
                row + List(headerCount - row.size) { "" }
            } else {
                row
            }
        }

        val headers = normalizedRows.first()
        val dataRows = normalizedRows.drop(1)
        return ParsedSpreadsheet(headers, dataRows, filename)
    }
}
