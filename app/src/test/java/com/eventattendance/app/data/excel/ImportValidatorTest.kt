package com.eventattendance.app.data.excel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ImportValidatorTest {

    @Test
    fun validate_onlyNameAndEnrollmentPresent_passesValidation() {
        val mapping = SpreadsheetColumnMapping(
            nameColumnIndex = 0,
            enrollmentColumnIndex = 1,
            sectionColumnIndex = null,
            rollNoColumnIndex = null,
            emailColumnIndex = null,
            mobileColumnIndex = null
        )

        val rows = listOf(
            listOf("Aarav Sharma", "12025052002292"),
            listOf("Ananya Das", "00123456789012")
        )

        val report = ImportValidator.validate(
            dataRows = rows,
            mapping = mapping,
            eventId = "event-123",
            existingRosterEnrollments = emptySet()
        )

        assertThat(report.totalRows).isEqualTo(2)
        assertThat(report.validStudents).hasSize(2)
        assertThat(report.errors).isEmpty()
        assertThat(report.validStudents[1].enrollmentNo).isEqualTo("00123456789012")
        assertThat(report.validStudents[1].section).isNull()
    }

    @Test
    fun validate_missingRequiredEnrollment_flagsError() {
        val mapping = SpreadsheetColumnMapping(
            nameColumnIndex = 0,
            enrollmentColumnIndex = 1
        )

        val rows = listOf(
            listOf("Aarav Sharma", ""),
            listOf("Ananya Das", "2294")
        )

        val report = ImportValidator.validate(
            dataRows = rows,
            mapping = mapping,
            eventId = "event-123",
            existingRosterEnrollments = emptySet()
        )

        assertThat(report.validStudents).hasSize(1)
        assertThat(report.errors).hasSize(1)
        assertThat(report.errors[0].reason).contains("Enrollment number is missing")
    }

    @Test
    fun validate_duplicateEnrollmentInFile_flagsDuplicateError() {
        val mapping = SpreadsheetColumnMapping(
            nameColumnIndex = 0,
            enrollmentColumnIndex = 1
        )

        val rows = listOf(
            listOf("Student 1", "2292"),
            listOf("Student 2", "2292")
        )

        val report = ImportValidator.validate(
            dataRows = rows,
            mapping = mapping,
            eventId = "event-123",
            existingRosterEnrollments = emptySet()
        )

        assertThat(report.validStudents).hasSize(1)
        assertThat(report.errors).hasSize(1)
        assertThat(report.duplicateInFileCount).isEqualTo(1)
    }

    @Test
    fun detectMappings_heuristicallyFindsHeaders() {
        val headers = listOf("Student Name", "Enrollment No", "Dept Section", "Roll", "Email Address", "Phone Number")
        val detected = SpreadsheetColumnMapping.detectMappings(headers)

        assertThat(detected.nameColumnIndex).isEqualTo(0)
        assertThat(detected.enrollmentColumnIndex).isEqualTo(1)
        assertThat(detected.sectionColumnIndex).isEqualTo(2)
        assertThat(detected.rollNoColumnIndex).isEqualTo(3)
        assertThat(detected.emailColumnIndex).isEqualTo(4)
        assertThat(detected.mobileColumnIndex).isEqualTo(5)
        assertThat(detected.isValid).isTrue()
    }
}
