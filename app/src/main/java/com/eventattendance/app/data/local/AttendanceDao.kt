package com.eventattendance.app.data.local

import androidx.room.*
import com.eventattendance.app.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceRecord>)

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId AND enrollmentNo = :enrollmentNo LIMIT 1")
    suspend fun getAttendance(eventId: String, enrollmentNo: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId AND enrollmentNo = :enrollmentNo LIMIT 1")
    fun observeAttendance(eventId: String, enrollmentNo: String): Flow<AttendanceRecord?>

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId ORDER BY markedAt DESC")
    fun getAllAttendanceForEvent(eventId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId ORDER BY markedAt DESC")
    suspend fun getAttendanceListForEvent(eventId: String): List<AttendanceRecord>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE eventId = :eventId AND status = 'present'")
    fun getAttendanceCountForEvent(eventId: String): Flow<Int>

    @Query("SELECT * FROM attendance_records WHERE syncStatus = 'pending_sync'")
    suspend fun getAllPendingRecords(): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE eventId = :eventId AND syncStatus = 'pending_sync'")
    suspend fun getPendingRecordsForEvent(eventId: String): List<AttendanceRecord>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE eventId = :eventId AND syncStatus = 'pending_sync'")
    fun getPendingSyncCountForEvent(eventId: String): Flow<Int>

    @Query("UPDATE attendance_records SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: String)

    @Query("UPDATE attendance_records SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM attendance_records WHERE eventId = :eventId")
    suspend fun clearAttendanceForEvent(eventId: String)
}
