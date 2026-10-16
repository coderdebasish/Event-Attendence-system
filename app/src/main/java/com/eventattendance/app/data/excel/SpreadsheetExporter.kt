package com.eventattendance.app.data.excel

import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.EventStudent
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object SpreadsheetExporter {

    fun generateCsv(
        students: List<EventStudent>,
        attendanceMap: Map<String, AttendanceRecord>
    ): String {
        val sb = StringBuilder()
        // CSV Header
        sb.append("Name,Enrollment Number,Section,Roll Number,Email,Mobile,Attendance Status,Marked At,Operator\n")

        students.forEach { student ->
            val attendance = attendanceMap[student.enrollmentNo]
            val status = if (attendance != null && attendance.status == "present") "PRESENT" else "UNMARKED"
            val markedAt = attendance?.markedAt ?: "—"
            val operator = attendance?.operatorName ?: "—"

            sb.append(escapeCsv(student.name)).append(",")
            sb.append(escapeCsv(student.enrollmentNo)).append(",")
            sb.append(escapeCsv(student.section ?: "—")).append(",")
            sb.append(escapeCsv(student.rollNo ?: "—")).append(",")
            sb.append(escapeCsv(student.email ?: "—")).append(",")
            sb.append(escapeCsv(student.mobile ?: "—")).append(",")
            sb.append(escapeCsv(status)).append(",")
            sb.append(escapeCsv(markedAt)).append(",")
            sb.append(escapeCsv(operator)).append("\n")
        }

        return sb.toString()
    }

    fun exportToFile(
        file: File,
        students: List<EventStudent>,
        attendanceMap: Map<String, AttendanceRecord>
    ): File {
        val csvContent = generateCsv(students, attendanceMap)
        FileOutputStream(file).use { fos ->
            OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                // Write UTF-8 BOM for Microsoft Excel compatibility
                writer.write("\uFEFF")
                writer.write(csvContent)
            }
        }
        return file
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
