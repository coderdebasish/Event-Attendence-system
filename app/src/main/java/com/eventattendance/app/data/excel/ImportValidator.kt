package com.eventattendance.app.data.excel

import com.eventattendance.app.data.model.EventStudent
import java.util.UUID

data class SpreadsheetColumnMapping(
    val nameColumnIndex: Int? = null,
    val enrollmentColumnIndex: Int? = null,
    val sectionColumnIndex: Int? = null,
    val rollNoColumnIndex: Int? = null,
    val emailColumnIndex: Int? = null,
    val mobileColumnIndex: Int? = null
) {
    val isValid: Boolean
        get() = nameColumnIndex != null &&
                enrollmentColumnIndex != null &&
                nameColumnIndex != enrollmentColumnIndex

    companion object {
        fun detectMappings(headers: List<String>): SpreadsheetColumnMapping {
            var nameIdx: Int? = null
            var enrollIdx: Int? = null
            var secIdx: Int? = null
            var rollIdx: Int? = null
            var emailIdx: Int? = null
            var mobileIdx: Int? = null

            headers.forEachIndexed { index, header ->
                val h = header.lowercase().trim()
                when {
                    enrollIdx == null && (h.contains("enroll") || h.contains("reg") || h.contains("id") || h.contains("admission")) -> {
                        enrollIdx = index
                    }
                    nameIdx == null && (h.contains("name") || h.contains("student")) -> {
                        nameIdx = index
                    }
                    secIdx == null && (h.contains("sec") || h.contains("branch") || h.contains("class")) -> {
                        secIdx = index
                    }
                    rollIdx == null && (h.contains("roll") || h.contains("serial")) -> {
                        rollIdx = index
                    }
                    emailIdx == null && (h.contains("email") || h.contains("mail")) -> {
                        emailIdx = index
                    }
                    mobileIdx == null && (h.contains("mobile") || h.contains("phone") || h.contains("contact")) -> {
                        mobileIdx = index
                    }
                }
            }

            return SpreadsheetColumnMapping(
                nameColumnIndex = nameIdx,
                enrollmentColumnIndex = enrollIdx,
                sectionColumnIndex = secIdx,
                rollNoColumnIndex = rollIdx,
                emailColumnIndex = emailIdx,
                mobileColumnIndex = mobileIdx
            )
        }
    }
}

data class RowValidationError(
    val rowIndex: Int,
    val reason: String,
    val rawValues: List<String>
)

data class ImportValidationReport(
    val totalRows: Int,
    val validStudents: List<EventStudent>,
    val errors: List<RowValidationError>,
    val duplicateInFileCount: Int,
    val existingInRosterCount: Int
) {
    val isValid: Boolean get() = validStudents.isNotEmpty()
}

object ImportValidator {

    fun validate(
        dataRows: List<List<String>>,
        mapping: SpreadsheetColumnMapping,
        eventId: String,
        existingRosterEnrollments: Set<String>
    ): ImportValidationReport {
        val validStudents = mutableListOf<EventStudent>()
        val errors = mutableListOf<RowValidationError>()
        val seenEnrollments = mutableSetOf<String>()
        var duplicateCount = 0
        var existingCount = 0

        dataRows.forEachIndexed { index, row ->
            val rowNum = index + 2 // 1-based, accounting for header row

            val name = mapping.nameColumnIndex?.let { idx ->
                row.getOrNull(idx)?.trim()
            }
            val enrollment = mapping.enrollmentColumnIndex?.let { idx ->
                row.getOrNull(idx)?.trim()
            }

            if (name.isNullOrBlank()) {
                errors.add(RowValidationError(rowNum, "Student name is missing or blank.", row))
                return@forEachIndexed
            }

            if (enrollment.isNullOrBlank()) {
                errors.add(RowValidationError(rowNum, "Enrollment number is missing or blank.", row))
                return@forEachIndexed
            }

            // Check duplicate within the uploaded file
            if (seenEnrollments.contains(enrollment)) {
                duplicateCount++
                errors.add(RowValidationError(rowNum, "Duplicate enrollment number '$enrollment' within file.", row))
                return@forEachIndexed
            }
            seenEnrollments.add(enrollment)

            // Check if student already registered in the event
            if (existingRosterEnrollments.contains(enrollment)) {
                existingCount++
            }

            val section = mapping.sectionColumnIndex?.let { idx -> row.getOrNull(idx)?.trim().takeIf { !it.isNullOrBlank() } }
            val rollNo = mapping.rollNoColumnIndex?.let { idx -> row.getOrNull(idx)?.trim().takeIf { !it.isNullOrBlank() } }
            val email = mapping.emailColumnIndex?.let { idx -> row.getOrNull(idx)?.trim().takeIf { !it.isNullOrBlank() } }
            val mobile = mapping.mobileColumnIndex?.let { idx -> row.getOrNull(idx)?.trim().takeIf { !it.isNullOrBlank() } }

            val student = EventStudent(
                id = UUID.randomUUID().toString(),
                eventId = eventId,
                name = name,
                enrollmentNo = enrollment,
                section = section,
                rollNo = rollNo,
                email = email,
                mobile = mobile
            )
            validStudents.add(student)
        }

        return ImportValidationReport(
            totalRows = dataRows.size,
            validStudents = validStudents,
            errors = errors,
            duplicateInFileCount = duplicateCount,
            existingInRosterCount = existingCount
        )
    }
}
