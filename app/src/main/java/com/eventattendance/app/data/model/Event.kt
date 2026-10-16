package com.eventattendance.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    @SerialName("id")
    val id: String,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = "",

    @SerialName("event_date")
    val eventDate: String,

    @SerialName("status")
    val status: String = "open", // draft, published, open, closed, archived

    @SerialName("allow_attendance")
    val allowAttendance: Boolean = true,

    @SerialName("created_by")
    val createdBy: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    val registrationCount: Int = 0,
    val attendanceCount: Int = 0
) {
    val isAttendanceAllowed: Boolean
        get() = allowAttendance && (status == "open" || status == "published")
}
