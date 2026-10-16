package com.eventattendance.app.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StudentQrParserTest {

    @Test
    fun parse_validPipeSeparatedPayload_extractsFirstField() {
        val payload = "12025052002292|CSE|B.TECH|104202505200628"
        val result = StudentQrParser.parse(payload)

        assertThat(result).isInstanceOf(QrParseResult.Success::class.java)
        val success = result as QrParseResult.Success
        assertThat(success.enrollmentNumber).isEqualTo("12025052002292")
    }

    @Test
    fun parse_leadingZeroEnrollmentNumber_preservesLeadingZeros() {
        val payload = "00123456789012|CSE|B.TECH|123456"
        val result = StudentQrParser.parse(payload)

        assertThat(result).isInstanceOf(QrParseResult.Success::class.java)
        val success = result as QrParseResult.Success
        assertThat(success.enrollmentNumber).isEqualTo("00123456789012")
        assertThat(success.enrollmentNumber).startsWith("00")
    }

    @Test
    fun parse_plainEnrollmentNumber_acceptsDirectly() {
        val payload = "12025052002292"
        val result = StudentQrParser.parse(payload)

        assertThat(result).isInstanceOf(QrParseResult.Success::class.java)
        val success = result as QrParseResult.Success
        assertThat(success.enrollmentNumber).isEqualTo("12025052002292")
    }

    @Test
    fun parse_missingFirstField_rejectsAsInvalid() {
        val payload = "|CSE|B.TECH|104202505200628"
        val result = StudentQrParser.parse(payload)

        assertThat(result).isInstanceOf(QrParseResult.Invalid::class.java)
        val invalid = result as QrParseResult.Invalid
        assertThat(invalid.reason).contains("missing")
    }

    @Test
    fun parse_delimiterOnly_rejectsAsInvalid() {
        val payload = "|"
        val result = StudentQrParser.parse(payload)

        assertThat(result).isInstanceOf(QrParseResult.Invalid::class.java)
    }

    @Test
    fun parse_emptyOrWhitespacePayload_rejectsAsInvalid() {
        val emptyResult = StudentQrParser.parse("")
        assertThat(emptyResult).isInstanceOf(QrParseResult.Invalid::class.java)

        val whitespaceResult = StudentQrParser.parse("   ")
        assertThat(whitespaceResult).isInstanceOf(QrParseResult.Invalid::class.java)

        val nullResult = StudentQrParser.parse(null)
        assertThat(nullResult).isInstanceOf(QrParseResult.Invalid::class.java)
    }

    @Test
    fun parse_payloadWithSurroundingWhitespace_trimsEnrollmentNumber() {
        val payload = "  12025052002292  |CSE|B.TECH|104202505200628"
        val result = StudentQrParser.parse(payload)

        assertThat(result).isInstanceOf(QrParseResult.Success::class.java)
        val success = result as QrParseResult.Success
        assertThat(success.enrollmentNumber).isEqualTo("12025052002292")
    }
}
