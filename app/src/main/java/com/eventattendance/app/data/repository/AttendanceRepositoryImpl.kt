package com.eventattendance.app.data.repository

import com.eventattendance.app.data.local.AttendanceDao
import com.eventattendance.app.data.local.StudentDao
import com.eventattendance.app.data.model.AttendanceOutcome
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.remote.SupabaseAttendanceApi
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepositoryImpl @Inject constructor(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val api: SupabaseAttendanceApi
) : AttendanceRepository {

    override fun observeStudent(eventId: String, enrollmentNo: String): Flow<EventStudent?> {
        return studentDao.observeStudentByEnrollment(eventId, enrollmentNo)
    }

    override suspend fun getStudent(eventId: String, enrollmentNo: String): EventStudent? {
        return studentDao.getStudentByEnrollment(eventId, enrollmentNo)
    }

    override fun observeAttendance(eventId: String, enrollmentNo: String): Flow<AttendanceRecord?> {
        return attendanceDao.observeAttendance(eventId, enrollmentNo)
    }

    override suspend fun getAttendance(eventId: String, enrollmentNo: String): AttendanceRecord? {
        return attendanceDao.getAttendance(eventId, enrollmentNo)
    }

    override fun searchStudents(eventId: String, query: String): Flow<List<EventStudent>> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            studentDao.getAllStudentsForEvent(eventId)
        } else {
            studentDao.searchCombined(eventId, trimmed)
        }
    }

    override fun getRoster(eventId: String): Flow<List<EventStudent>> {
        return studentDao.getAllStudentsForEvent(eventId)
    }

    override fun getAllAttendanceRecords(eventId: String): Flow<List<AttendanceRecord>> {
        return attendanceDao.getAllAttendanceForEvent(eventId)
    }

    override fun getAttendanceCount(eventId: String): Flow<Int> {
        return attendanceDao.getAttendanceCountForEvent(eventId)
    }

    override fun getStudentCount(eventId: String): Flow<Int> {
        return studentDao.getStudentCountForEvent(eventId)
    }

    override suspend fun markAttendance(
        eventId: String,
        enrollmentNo: String,
        operatorName: String?,
        deviceId: String?
    ): AttendanceOutcome {
        return try {
            // Call Supabase atomic RPC procedure
            val outcome = api.markAttendance(eventId, enrollmentNo, operatorName, deviceId)

            // When server confirms success, persist directly into local Room database
            if (outcome is AttendanceOutcome.Success) {
                val student = studentDao.getStudentByEnrollment(eventId, enrollmentNo)
                val record = AttendanceRecord(
                    id = outcome.attendanceId.ifEmpty { UUID.randomUUID().toString() },
                    eventId = eventId,
                    eventStudentId = student?.id ?: outcome.attendanceId,
                    enrollmentNo = enrollmentNo,
                    operatorName = outcome.operatorName,
                    markedAt = outcome.markedAt,
                    status = "present"
                )
                attendanceDao.insertAttendance(record)
            } else if (outcome is AttendanceOutcome.AlreadyMarked) {
                // Keep local cache updated
                val existing = attendanceDao.getAttendance(eventId, enrollmentNo)
                if (existing == null) {
                    val student = studentDao.getStudentByEnrollment(eventId, enrollmentNo)
                    attendanceDao.insertAttendance(
                        AttendanceRecord(
                            id = outcome.attendanceId ?: UUID.randomUUID().toString(),
                            eventId = eventId,
                            eventStudentId = student?.id ?: "",
                            enrollmentNo = enrollmentNo,
                            operatorName = outcome.operatorName,
                            markedAt = outcome.markedAt,
                            status = "present"
                        )
                    )
                }
            }
            outcome
        } catch (e: Exception) {
            // Local fallback if network is unreachable
            val existingLocal = attendanceDao.getAttendance(eventId, enrollmentNo)
            if (existingLocal != null && existingLocal.status == "present") {
                val student = studentDao.getStudentByEnrollment(eventId, enrollmentNo)
                AttendanceOutcome.AlreadyMarked(
                    attendanceId = existingLocal.id,
                    studentName = student?.name ?: "",
                    enrollmentNo = enrollmentNo,
                    markedAt = existingLocal.markedAt,
                    operatorName = existingLocal.operatorName
                )
            } else {
                val student = studentDao.getStudentByEnrollment(eventId, enrollmentNo)
                if (student == null) {
                    AttendanceOutcome.StudentNotRegistered(enrollmentNo)
                } else {
                    AttendanceOutcome.Failure(
                        message = "Unable to connect to server: ${e.localizedMessage ?: "Network error"}",
                        cause = e
                    )
                }
            }
        }
    }

    override suspend fun correctAttendance(
        attendanceId: String,
        newStatus: String,
        reason: String
    ): Result<Unit> {
        val result = api.correctAttendance(attendanceId, newStatus, reason)
        if (result.isSuccess) {
            attendanceDao.updateStatus(attendanceId, newStatus)
        }
        return result
    }

    override suspend fun syncRoster(eventId: String): Result<Int> {
        return try {
            val remoteStudents = api.getRosterForEvent(eventId)
            if (remoteStudents.isNotEmpty()) {
                studentDao.insertStudents(remoteStudents)
            }
            Result.success(remoteStudents.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncAttendance(eventId: String): Result<Int> {
        return try {
            val remoteRecords = api.getAttendanceForEvent(eventId)
            if (remoteRecords.isNotEmpty()) {
                attendanceDao.insertAllAttendance(remoteRecords)
            }
            Result.success(remoteRecords.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveImportedStudents(students: List<EventStudent>): Result<Int> {
        return try {
            studentDao.insertStudents(students)
            Result.success(students.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
