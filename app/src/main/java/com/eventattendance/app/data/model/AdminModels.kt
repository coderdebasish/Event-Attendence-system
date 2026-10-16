package com.eventattendance.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImportBatch(
    @SerialName("id")
    val id: String,

    @SerialName("event_id")
    val eventId: String,

    @SerialName("imported_by")
    val importedBy: String? = null,

    @SerialName("filename")
    val filename: String,

    @SerialName("total_rows")
    val totalRows: Int = 0,

    @SerialName("valid_rows")
    val validRows: Int = 0,

    @SerialName("invalid_rows")
    val invalidRows: Int = 0,

    @SerialName("duplicate_rows")
    val duplicateRows: Int = 0,

    @SerialName("imported_rows")
    val importedRows: Int = 0,

    @SerialName("status")
    val status: String = "completed",

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AuditLog(
    @SerialName("id")
    val id: String,

    @SerialName("event_id")
    val eventId: String? = null,

    @SerialName("action")
    val action: String,

    @SerialName("performed_by")
    val performedBy: String? = null,

    @SerialName("target_id")
    val targetId: String? = null,

    @SerialName("details")
    val details: String? = null,

    @SerialName("reason")
    val reason: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String,

    @SerialName("email")
    val email: String? = null,

    @SerialName("full_name")
    val fullName: String,

    @SerialName("role")
    val role: String = "operator" // admin, operator
) {
    val isAdmin: Boolean get() = role.equals("admin", ignoreCase = true)
}
