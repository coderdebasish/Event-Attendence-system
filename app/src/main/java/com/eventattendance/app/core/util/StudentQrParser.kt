package com.eventattendance.app.core.util

/**
 * Result of parsing a Student ID QR code or Barcode payload.
 */
sealed class QrParseResult {
    data class Success(val enrollmentNumber: String, val rawPayload: String) : QrParseResult()
    data class Invalid(val reason: String, val rawPayload: String) : QrParseResult()
}

/**
 * Pure, unit-testable parser for student ID card QR codes and barcodes.
 *
 * QR Code Format:
 * Pipe-separated fields: [EnrollmentNumber]|[Department]|[Degree]|[AdditionalID]
 * Example: "12025052002292|CSE|B.TECH|104202505200628"
 *
 * Only Field 1 (Enrollment Number) is extracted.
 * Plain enrollment numbers without pipes are also supported.
 * Leading zeros are strictly preserved as Strings.
 */
object StudentQrParser {

    private const val DELIMITER = "|"

    fun parse(rawPayload: String?): QrParseResult {
        if (rawPayload.isNullOrBlank()) {
            return QrParseResult.Invalid(
                reason = "QR code payload is empty or blank.",
                rawPayload = rawPayload ?: ""
            )
        }

        val trimmed = rawPayload.trim()

        if (trimmed == DELIMITER || trimmed.all { it == '|' }) {
            return QrParseResult.Invalid(
                reason = "Invalid QR code payload: delimiter only.",
                rawPayload = trimmed
            )
        }

        val extracted = if (trimmed.contains(DELIMITER)) {
            val parts = trimmed.split(DELIMITER)
            val firstField = parts.firstOrNull()?.trim()
            if (firstField.isNullOrEmpty()) {
                return QrParseResult.Invalid(
                    reason = "Enrollment number is missing in the QR code.",
                    rawPayload = trimmed
                )
            }
            firstField
        } else {
            // Support plain enrollment number
            trimmed
        }

        if (extracted.isEmpty()) {
            return QrParseResult.Invalid(
                reason = "Extracted enrollment number is blank.",
                rawPayload = trimmed
            )
        }

        return QrParseResult.Success(
            enrollmentNumber = extracted,
            rawPayload = trimmed
        )
    }
}
