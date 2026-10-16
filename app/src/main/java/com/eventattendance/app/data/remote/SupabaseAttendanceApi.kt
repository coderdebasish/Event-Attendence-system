package com.eventattendance.app.data.remote

import com.eventattendance.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAttendanceApi @Inject constructor(
    private val clientProvider: SupabaseClientProvider
) {

    suspend fun getEvents(): List<Event> {
        val client = clientProvider.getClient()
        return client.postgrest["events"]
            .select()
            .decodeList<Event>()
    }

    suspend fun createEvent(event: Event): Event {
        val client = clientProvider.getClient()
        return client.postgrest["events"]
            .insert(event) {
                select()
            }
            .decodeSingle<Event>()
    }

    suspend fun updateEvent(event: Event) {
        val client = clientProvider.getClient()
        client.postgrest["events"]
            .update(event) {
                filter {
                    eq("id", event.id)
                }
            }
    }

    suspend fun getRosterForEvent(eventId: String): List<EventStudent> {
        val client = clientProvider.getClient()
        return client.postgrest["event_students"]
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList<EventStudent>()
    }

    suspend fun getAttendanceForEvent(eventId: String): List<AttendanceRecord> {
        val client = clientProvider.getClient()
        return client.postgrest["attendance_records"]
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList<AttendanceRecord>()
    }

    /**
     * Executes the atomic mark_attendance stored procedure on PostgreSQL.
     * Prevents race conditions and guarantees unique attendance records.
     */
    suspend fun markAttendance(
        eventId: String,
        enrollmentNo: String,
        operatorName: String?,
        deviceId: String?
    ): AttendanceOutcome {
        val client = clientProvider.getClient()
        val params = buildJsonObject {
            put("p_event_id", eventId)
            put("p_enrollment_no", enrollmentNo)
            put("p_operator_name", operatorName ?: "Operator")
            put("p_device_id", deviceId ?: "android-device")
        }

        val responseJson = client.postgrest.rpc("mark_attendance", params).decodeAs<JsonObject>()
        val status = responseJson["status"]?.jsonPrimitive?.content ?: "UNKNOWN"

        return when (status) {
            "SUCCESS" -> {
                AttendanceOutcome.Success(
                    attendanceId = responseJson["attendance_id"]?.jsonPrimitive?.content ?: "",
                    studentName = responseJson["student_name"]?.jsonPrimitive?.content ?: "",
                    enrollmentNo = responseJson["enrollment_no"]?.jsonPrimitive?.content ?: enrollmentNo,
                    markedAt = responseJson["marked_at"]?.jsonPrimitive?.content ?: "",
                    operatorName = responseJson["operator_name"]?.jsonPrimitive?.content ?: operatorName
                )
            }
            "ALREADY_MARKED" -> {
                AttendanceOutcome.AlreadyMarked(
                    attendanceId = responseJson["attendance_id"]?.jsonPrimitive?.content,
                    studentName = responseJson["student_name"]?.jsonPrimitive?.content ?: "",
                    enrollmentNo = responseJson["enrollment_no"]?.jsonPrimitive?.content ?: enrollmentNo,
                    markedAt = responseJson["marked_at"]?.jsonPrimitive?.content ?: "",
                    operatorName = responseJson["operator_name"]?.jsonPrimitive?.content
                )
            }
            "STUDENT_NOT_REGISTERED" -> {
                AttendanceOutcome.StudentNotRegistered(
                    enrollmentNo = enrollmentNo,
                    message = responseJson["message"]?.jsonPrimitive?.content ?: "Student not registered."
                )
            }
            "EVENT_CLOSED" -> {
                AttendanceOutcome.EventClosed(
                    message = responseJson["message"]?.jsonPrimitive?.content ?: "Event attendance closed."
                )
            }
            "PERMISSION_DENIED" -> {
                AttendanceOutcome.PermissionDenied(
                    message = responseJson["message"]?.jsonPrimitive?.content ?: "Permission denied."
                )
            }
            else -> {
                AttendanceOutcome.Failure(
                    message = responseJson["message"]?.jsonPrimitive?.content ?: "Failed with status: $status"
                )
            }
        }
    }

    suspend fun correctAttendance(
        attendanceId: String,
        newStatus: String,
        reason: String
    ): Result<Unit> {
        return try {
            val client = clientProvider.getClient()
            val params = buildJsonObject {
                put("p_attendance_id", attendanceId)
                put("p_new_status", newStatus)
                put("p_reason", reason)
            }
            client.postgrest.rpc("correct_attendance", params)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importRosterBatch(
        eventId: String,
        students: List<EventStudent>,
        filename: String,
        totalRows: Int,
        validRows: Int,
        invalidRows: Int,
        duplicateRows: Int
    ): Result<Int> {
        return try {
            val client = clientProvider.getClient()
            if (students.isNotEmpty()) {
                client.postgrest["event_students"].upsert(students)
            }
            // Record import batch summary
            val batch = buildJsonObject {
                put("event_id", eventId)
                put("filename", filename)
                put("total_rows", totalRows)
                put("valid_rows", validRows)
                put("invalid_rows", invalidRows)
                put("duplicate_rows", duplicateRows)
                put("imported_rows", students.size)
                put("status", "completed")
            }
            client.postgrest["import_batches"].insert(batch)
            Result.success(students.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAuditLogs(eventId: String): List<AuditLog> {
        val client = clientProvider.getClient()
        return client.postgrest["audit_logs"]
            .select {
                filter {
                    eq("event_id", eventId)
                }
            }
            .decodeList<AuditLog>()
    }

    suspend fun getUsers(): List<UserProfile> {
        val client = clientProvider.getClient()
        return client.postgrest["profiles"]
            .select()
            .decodeList<UserProfile>()
    }

    suspend fun createUser(email: String, password: String, fullName: String, role: String): Result<String> {
        return try {
            val client = clientProvider.getClient()
            val params = buildJsonObject {
                put("p_email", email.trim())
                put("p_password", password)
                put("p_full_name", fullName.trim())
                put("p_role", role.lowercase().trim())
            }
            val responseJson = client.postgrest.rpc("admin_create_user", params).decodeAs<JsonObject>()
            val isSuccess = responseJson["success"]?.jsonPrimitive?.booleanOrNull ?: false
            val message = responseJson["message"]?.jsonPrimitive?.content ?: "Unknown response"
            if (isSuccess) {
                Result.success(message)
            } else {
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserRole(userId: String, newRole: String): Result<Unit> {
        return try {
            val client = clientProvider.getClient()
            val updateBody = buildJsonObject {
                put("role", newRole.lowercase())
            }
            client.postgrest["profiles"].update(updateBody) {
                filter {
                    eq("id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
