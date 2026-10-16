package com.eventattendance.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["eventId", "enrollmentNo"], unique = true),
        Index(value = ["eventId"]),
        Index(value = ["markedAt"])
    ]
)
data class AttendanceRecord(
    @PrimaryKey
    @SerialName("id")
    val id: String,

    @SerialName("event_id")
    val eventId: String,

    @SerialName("event_student_id")
    val eventStudentId: String,

    @SerialName("enrollment_no")
    val enrollmentNo: String,

    @SerialName("marked_by")
    val markedBy: String? = null,

    @SerialName("operator_name")
    val operatorName: String? = null,

    @SerialName("device_id")
    val deviceId: String? = null,

    @SerialName("status")
    val status: String = "present", // present, corrected

    @SerialName("marked_at")
    val markedAt: String,

    @SerialName("created_at")
    val createdAt: String? = null
)

sealed class AttendanceOutcome {
    data class Success(
        val attendanceId: String,
        val studentName: String,
        val enrollmentNo: String,
        val markedAt: String,
        val operatorName: String?
    ) : AttendanceOutcome()

    data class AlreadyMarked(
        val attendanceId: String?,
        val studentName: String,
        val enrollmentNo: String,
        val markedAt: String,
        val operatorName: String?
    ) : AttendanceOutcome()

    data class StudentNotRegistered(
        val enrollmentNo: String,
        val message: String = "Student is not registered for this event."
    ) : AttendanceOutcome()

    data class EventClosed(
        val message: String = "Attendance marking is closed for this event."
    ) : AttendanceOutcome()

    data class PermissionDenied(
        val message: String = "Permission denied."
    ) : AttendanceOutcome()

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : AttendanceOutcome()
}
