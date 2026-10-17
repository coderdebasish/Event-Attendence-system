package com.eventattendance.app.data.excel

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class StreamingSpreadsheetParserTest {

    @Test
    fun cellRefToColumnIndex_correctlyConvertsLetters() {
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("A1")).isEqualTo(0)
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("B98")).isEqualTo(1)
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("F105")).isEqualTo(5)
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("Z1")).isEqualTo(25)
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("AA2")).isEqualTo(26)
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("AB5")).isEqualTo(27)
        assertThat(StreamingSpreadsheetParser.cellRefToColumnIndex("123")).isEqualTo(-1)
    }

    @Test
    fun formatNumericCellValue_cleansScientificNotationAndFloats() {
        // Scientific notation from Excel for large enrollment numbers
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("1.20261E+13")).isEqualTo("12026100000000")
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("1.2026105200229E+13")).isEqualTo("12026105200229")

        // Integer stored as float
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("54.0")).isEqualTo("54")
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("0.0")).isEqualTo("0")
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("100.0")).isEqualTo("100")

        // Non-numbers or integers without float
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("54")).isEqualTo("54")
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("B7")).isEqualTo("B7")
        assertThat(StreamingSpreadsheetParser.formatNumericCellValue("user@example.com")).isEqualTo("user@example.com")
    }

    @Test
    fun parseCsv_handlesQuotedStringsAndNumericValues() {
        val csvData = """
            "Email address","Student Name","Section","Roll Number","Gmail","Enrollment Number"
            karmakardipyaman007,Dipyaman Karmakar,B7,54.0,karmakardipyaman007@gmail.com,1.2026105200229E+13
        """.trimIndent()

        val parsed = StreamingSpreadsheetParser.parseCsv(
            ByteArrayInputStream(csvData.toByteArray(Charsets.UTF_8)),
            "roster.csv"
        )

        assertThat(parsed.headers).containsExactly(
            "Email address", "Student Name", "Section", "Roll Number", "Gmail", "Enrollment Number"
        ).inOrder()

        assertThat(parsed.rows).hasSize(1)
        val row = parsed.rows.first()
        assertThat(row[0]).isEqualTo("karmakardipyaman007")
        assertThat(row[1]).isEqualTo("Dipyaman Karmakar")
        assertThat(row[2]).isEqualTo("B7")
        assertThat(row[3]).isEqualTo("54")
        assertThat(row[4]).isEqualTo("karmakardipyaman007@gmail.com")
        assertThat(row[5]).isEqualTo("12026105200229")
    }

    @Test
    fun parseStream_detectsZipMagicBytesAndParsesXlsx() {
        val sharedStringsXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="6" uniqueCount="6">
                <si><t>Email address</t></si>
                <si><t>Student Name</t></si>
                <si><t>Section</t></si>
                <si><t>Roll Number</t></si>
                <si><t>Gmail</t></si>
                <si><t>Enrollment Number</t></si>
                <si><t>karmakardipyaman007</t></si>
                <si><t>Dipyaman Karmakar</t></si>
                <si><t>B7</t></si>
                <si><t>karmakardipyaman007@gmail.com</t></si>
            </sst>
        """.trimIndent()

        val sheet1Xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
                <sheetData>
                    <row r="1">
                        <c r="A1" t="s"><v>0</v></c>
                        <c r="B1" t="s"><v>1</v></c>
                        <c r="C1" t="s"><v>2</v></c>
                        <c r="D1" t="s"><v>3</v></c>
                        <c r="E1" t="s"><v>4</v></c>
                        <c r="F1" t="s"><v>5</v></c>
                    </row>
                    <row r="2">
                        <c r="A2" t="s"><v>6</v></c>
                        <c r="B2" t="s"><v>7</v></c>
                        <c r="C2" t="s"><v>8</v></c>
                        <c r="D2"><v>54.0</v></c>
                        <c r="E2" t="s"><v>9</v></c>
                        <c r="F2"><v>1.2026105200229E+13</v></c>
                    </row>
                </sheetData>
            </worksheet>
        """.trimIndent()

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("xl/sharedStrings.xml"))
            zos.write(sharedStringsXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("xl/worksheets/sheet1.xml"))
            zos.write(sheet1Xml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        val xlsxBytes = baos.toByteArray()

        // Notice we pass "unknown_file_without_extension" to simulate SAF content URI missing extension
        val parsed = StreamingSpreadsheetParser.parseStream(
            ByteArrayInputStream(xlsxBytes),
            "unknown_file_without_extension"
        )

        assertThat(parsed.headers).containsExactly(
            "Email address", "Student Name", "Section", "Roll Number", "Gmail", "Enrollment Number"
        ).inOrder()

        assertThat(parsed.rows).hasSize(1)
        val studentRow = parsed.rows.first()
        assertThat(studentRow[0]).isEqualTo("karmakardipyaman007")
        assertThat(studentRow[1]).isEqualTo("Dipyaman Karmakar")
        assertThat(studentRow[2]).isEqualTo("B7")
        assertThat(studentRow[3]).isEqualTo("54")
        assertThat(studentRow[4]).isEqualTo("karmakardipyaman007@gmail.com")
        assertThat(studentRow[5]).isEqualTo("12026105200229")
    }

    @Test
    fun detectMappings_accuratelyMapsUserSpreadsheetHeaders() {
        val headers = listOf(
            "Email address", "Student Name", "Section", "Roll Number", "Gmail", "Enrollment Number"
        )
        val mapping = SpreadsheetColumnMapping.detectMappings(headers)

        assertThat(mapping.nameColumnIndex).isEqualTo(1) // Student Name
        assertThat(mapping.enrollmentColumnIndex).isEqualTo(5) // Enrollment Number
        assertThat(mapping.sectionColumnIndex).isEqualTo(2) // Section
        assertThat(mapping.rollNoColumnIndex).isEqualTo(3) // Roll Number
        assertThat(mapping.emailColumnIndex).isEqualTo(0) // Email address
        assertThat(mapping.isValid).isTrue()
    }
}
