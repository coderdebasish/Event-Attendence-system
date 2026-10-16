package com.eventattendance.app.data.repository

import com.eventattendance.app.data.model.AttendanceOutcome
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.EventStudent
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun observeStudent(eventId: String, enrollmentNo: String): Flow<EventStudent?>
    suspend fun getStudent(eventId: String, enrollmentNo: String): EventStudent?
    fun observeAttendance(eventId: String, enrollmentNo: String): Flow<AttendanceRecord?>
    suspend fun getAttendance(eventId: String, enrollmentNo: String): AttendanceRecord?
    fun searchStudents(eventId: String, query: String): Flow<List<EventStudent>>
    fun getRoster(eventId: String): Flow<List<EventStudent>>
    fun getAllAttendanceRecords(eventId: String): Flow<List<AttendanceRecord>>
    fun getAttendanceCount(eventId: String): Flow<Int>
    fun getStudentCount(eventId: String): Flow<Int>

    suspend fun markAttendance(
        eventId: String,
        enrollmentNo: String,
        operatorName: String?,
        deviceId: String?
    ): AttendanceOutcome

    suspend fun correctAttendance(
        attendanceId: String,
        newStatus: String,
        reason: String
    ): Result<Unit>

    suspend fun syncRoster(eventId: String): Result<Int>
    suspend fun syncAttendance(eventId: String): Result<Int>
    suspend fun saveImportedStudents(students: List<EventStudent>): Result<Int>
}
