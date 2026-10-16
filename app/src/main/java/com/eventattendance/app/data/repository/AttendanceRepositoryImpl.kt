package com.eventattendance.app.data.repository

import com.eventattendance.app.data.local.AttendanceDao
import com.eventattendance.app.data.local.StudentDao
import com.eventattendance.app.data.model.AttendanceOutcome
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.remote.SupabaseAttendanceApi
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    override fun getPendingSyncCount(eventId: String): Flow<Int> {
        return attendanceDao.getPendingSyncCountForEvent(eventId)
    }

    override suspend fun markAttendance(
        eventId: String,
        enrollmentNo: String,
        operatorName: String?,
        deviceId: String?
    ): AttendanceOutcome {
        // 1. Check local student roster
        val localStudent = studentDao.getStudentByEnrollment(eventId, enrollmentNo)

        // 2. Check local attendance to prevent duplicates immediately
        val localAttendance = attendanceDao.getAttendance(eventId, enrollmentNo)
        if (localAttendance != null && localAttendance.status == "present") {
            return AttendanceOutcome.AlreadyMarked(
                attendanceId = localAttendance.id,
                studentName = localStudent?.name ?: "Student",
                enrollmentNo = enrollmentNo,
                markedAt = localAttendance.markedAt,
                operatorName = localAttendance.operatorName
            )
        }

        // 3. Attempt Supabase online write
        return try {
            val outcome = api.markAttendance(eventId, enrollmentNo, operatorName, deviceId)

            when (outcome) {
                is AttendanceOutcome.Success -> {
                    val record = AttendanceRecord(
                        id = outcome.attendanceId.ifEmpty { UUID.randomUUID().toString() },
                        eventId = eventId,
                        eventStudentId = localStudent?.id ?: outcome.attendanceId,
                        enrollmentNo = enrollmentNo,
                        operatorName = outcome.operatorName,
                        markedAt = outcome.markedAt,
                        status = "present",
                        syncStatus = "synced"
                    )
                    attendanceDao.insertAttendance(record)
                    outcome
                }
                is AttendanceOutcome.AlreadyMarked -> {
                    val record = AttendanceRecord(
                        id = outcome.attendanceId ?: UUID.randomUUID().toString(),
                        eventId = eventId,
                        eventStudentId = localStudent?.id ?: "",
                        enrollmentNo = enrollmentNo,
                        operatorName = outcome.operatorName,
                        markedAt = outcome.markedAt,
                        status = "present",
                        syncStatus = "synced"
                    )
                    attendanceDao.insertAttendance(record)
                    outcome
                }
                else -> outcome
            }
        } catch (networkException: Exception) {
            // 4. Offline Fallback: If student is in local roster, queue attendance locally!
            if (localStudent != null) {
                val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                val localId = UUID.randomUUID().toString()
                val offlineRecord = AttendanceRecord(
                    id = localId,
                    eventId = eventId,
                    eventStudentId = localStudent.id,
                    enrollmentNo = enrollmentNo,
                    operatorName = operatorName ?: "Operator",
                    deviceId = deviceId ?: "android-offline",
                    status = "present",
                    markedAt = nowIso,
                    syncStatus = "pending_sync"
                )
                attendanceDao.insertAttendance(offlineRecord)

                AttendanceOutcome.OfflineQueued(
                    attendanceId = localId,
                    studentName = localStudent.name,
                    enrollmentNo = enrollmentNo,
                    markedAt = nowIso,
                    operatorName = operatorName
                )
            } else {
                AttendanceOutcome.StudentNotRegistered(
                    enrollmentNo = enrollmentNo,
                    message = "Student not found in offline roster."
                )
            }
        }
    }

    override suspend fun correctAttendance(
        attendanceId: String,
        newStatus: String,
        reason: String
    ): Result<Unit> {
        return try {
            val result = api.correctAttendance(attendanceId, newStatus, reason)
            if (result.isSuccess) {
                attendanceDao.updateStatus(attendanceId, newStatus)
            }
            result
        } catch (e: Exception) {
            // Local update fallback
            attendanceDao.updateStatus(attendanceId, newStatus)
            Result.success(Unit)
        }
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

    override suspend fun syncPendingRecords(eventId: String): Result<Int> {
        val pending = attendanceDao.getPendingRecordsForEvent(eventId)
        if (pending.isEmpty()) return Result.success(0)

        var syncedCount = 0
        for (record in pending) {
            try {
                val outcome = api.markAttendance(
                    eventId = record.eventId,
                    enrollmentNo = record.enrollmentNo,
                    operatorName = record.operatorName,
                    deviceId = record.deviceId
                )
                if (outcome is AttendanceOutcome.Success || outcome is AttendanceOutcome.AlreadyMarked) {
                    attendanceDao.updateSyncStatus(record.id, "synced")
                    syncedCount++
                }
            } catch (_: Exception) {
                // Break on repeated network failure
                break
            }
        }
        return Result.success(syncedCount)
    }

    override suspend fun syncAllPendingRecords(): Result<Int> {
        val pending = attendanceDao.getAllPendingRecords()
        if (pending.isEmpty()) return Result.success(0)

        var syncedCount = 0
        for (record in pending) {
            try {
                val outcome = api.markAttendance(
                    eventId = record.eventId,
                    enrollmentNo = record.enrollmentNo,
                    operatorName = record.operatorName,
                    deviceId = record.deviceId
                )
                if (outcome is AttendanceOutcome.Success || outcome is AttendanceOutcome.AlreadyMarked) {
                    attendanceDao.updateSyncStatus(record.id, "synced")
                    syncedCount++
                }
            } catch (_: Exception) {
                // Network unreachable or failing, pause attempt
                break
            }
        }
        return Result.success(syncedCount)
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
