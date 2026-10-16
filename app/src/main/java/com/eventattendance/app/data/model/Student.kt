package com.eventattendance.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Event Student Roster representation.
 * Exactly 6 student data fields as required by specification:
 * 1. Name (Required)
 * 2. Enrollment Number (Required)
 * 3. Section (Optional)
 * 4. Roll Number (Optional)
 * 5. Email ID (Optional)
 * 6. Mobile Number (Optional)
 */
@Serializable
@Entity(
    tableName = "event_students",
    indices = [
        Index(value = ["eventId", "enrollmentNo"], unique = true),
        Index(value = ["eventId", "name"]),
        Index(value = ["enrollmentNo"])
    ]
)
data class EventStudent(
    @PrimaryKey
    @SerialName("id")
    val id: String,

    @SerialName("event_id")
    val eventId: String,

    @SerialName("student_id")
    val studentId: String? = null,

    // 1. Name (Required)
    @SerialName("name")
    val name: String,

    // 2. Enrollment Number (Required, preserved strictly as String)
    @SerialName("enrollment_no")
    val enrollmentNo: String,

    // 3. Section (Optional)
    @SerialName("section")
    val section: String? = null,

    // 4. Roll Number (Optional)
    @SerialName("roll_no")
    val rollNo: String? = null,

    // 5. Email ID (Optional)
    @SerialName("email")
    val email: String? = null,

    // 6. Mobile Number (Optional)
    @SerialName("mobile")
    val mobile: String? = null,

    @SerialName("status")
    val status: String = "registered",

    @SerialName("created_at")
    val createdAt: String? = null
)
