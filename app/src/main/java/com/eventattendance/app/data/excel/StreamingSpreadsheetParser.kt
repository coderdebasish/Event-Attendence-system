package com.eventattendance.app.data.excel

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class ParsedSpreadsheet(
    val headers: List<String>,
    val rows: List<List<String>>,
    val filename: String
)

object StreamingSpreadsheetParser {

    fun parse(context: Context, uri: Uri, filename: String): ParsedSpreadsheet {
        val lower = filename.lowercase()
        return when {
            lower.endsWith(".csv") -> parseCsv(context.contentResolver.openInputStream(uri)!!, filename)
            lower.endsWith(".xlsx") -> parseXlsx(context.contentResolver.openInputStream(uri)!!, filename)
            lower.endsWith(".xls") -> parseCsvOrTsvFallback(context.contentResolver.openInputStream(uri)!!, filename)
            else -> parseCsv(context.contentResolver.openInputStream(uri)!!, filename)
        }
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
                        currentRow.add(currentField.toString().trim())
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
                        currentRow.add(currentField.toString().trim())
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
                currentRow.add(currentField.toString().trim())
                if (currentRow.any { it.isNotBlank() }) {
                    rows.add(currentRow)
                }
            }
        }

        if (rows.isEmpty()) {
            return ParsedSpreadsheet(emptyList(), emptyList(), filename)
        }

        val headers = rows.first()
        val dataRows = rows.drop(1)
        return ParsedSpreadsheet(headers, dataRows, filename)
    }

    /**
     * Streaming OOXML (.xlsx) parser that reads sharedStrings.xml and sheet1.xml
     * without high memory consumption or third-party binary bloat.
     */
    fun parseXlsx(inputStream: InputStream, filename: String): ParsedSpreadsheet {
        val sharedStrings = mutableListOf<String>()
        val rawSheetBytes: ByteArray?
        var sheetBytes: ByteArray? = null

        // First pass: extract shared strings and sheet1 XML bytes from the ZIP container
        ZipInputStream(inputStream).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                when {
                    entry.name.equals("xl/sharedStrings.xml", ignoreCase = true) -> {
                        val strings = parseSharedStrings(zip)
                        sharedStrings.addAll(strings)
                    }
                    entry.name.equals("xl/worksheets/sheet1.xml", ignoreCase = true) ||
                    entry.name.matches(Regex("xl/worksheets/sheet[0-9]+\\.xml", RegexOption.IGNORE_CASE)) -> {
                        if (sheetBytes == null) {
                            sheetBytes = zip.readBytes()
                        }
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        rawSheetBytes = sheetBytes

        if (rawSheetBytes == null) {
            return ParsedSpreadsheet(emptyList(), emptyList(), filename)
        }

        // Second pass: parse rows from sheet XML using streaming XmlPullParser
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(rawSheetBytes.inputStream(), "UTF-8")

        var eventType = parser.eventType
        var currentRow = mutableListOf<String>()
        var cellType: String? = null
        var cellValue = StringBuilder()
        var insideCell = false
        var insideValue = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            currentRow = mutableListOf()
                        }
                        "c" -> {
                            insideCell = true
                            cellType = parser.getAttributeValue(null, "t")
                            cellValue.setLength(0)
                        }
                        "v" -> {
                            insideValue = true
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideValue) {
                        cellValue.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> {
                            insideValue = false
                        }
                        "c" -> {
                            insideCell = false
                            val raw = cellValue.toString().trim()
                            val text = if (cellType == "s") {
                                val index = raw.toIntOrNull()
                                if (index != null && index in sharedStrings.indices) {
                                    sharedStrings[index]
                                } else raw
                            } else {
                                raw
                            }
                            currentRow.add(text)
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

        val headers = rows.first()
        val dataRows = rows.drop(1)
        return ParsedSpreadsheet(headers, dataRows, filename)
    }

    private fun parseSharedStrings(stream: InputStream): List<String> {
        val list = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        var eventType = parser.eventType
        var currentText = StringBuilder()
        var insideT = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "t") {
                        insideT = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideT) {
                        currentText.append(parser.text)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "t") {
                        insideT = false
                    } else if (parser.name == "si") {
                        list.add(currentText.toString())
                        currentText.setLength(0)
                    }
                }
            }
            eventType = parser.next()
        }
        return list
    }

    private fun parseCsvOrTsvFallback(inputStream: InputStream, filename: String): ParsedSpreadsheet {
        // Fallback for tab-delimited or plain-text formatted spreadsheets
        return parseCsv(inputStream, filename)
    }
}
