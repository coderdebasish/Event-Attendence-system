package com.eventattendance.app.data.local

import androidx.room.*
import com.eventattendance.app.data.model.EventStudent
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<EventStudent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: EventStudent)

    @Query("SELECT * FROM event_students WHERE eventId = :eventId AND enrollmentNo = :enrollmentNo LIMIT 1")
    suspend fun getStudentByEnrollment(eventId: String, enrollmentNo: String): EventStudent?

    @Query("SELECT * FROM event_students WHERE eventId = :eventId AND enrollmentNo = :enrollmentNo LIMIT 1")
    fun observeStudentByEnrollment(eventId: String, enrollmentNo: String): Flow<EventStudent?>

    @Query("""
        SELECT * FROM event_students 
        WHERE eventId = :eventId AND enrollmentNo LIKE :prefix || '%' 
        ORDER BY enrollmentNo ASC
    """)
    fun searchByEnrollmentPrefix(eventId: String, prefix: String): Flow<List<EventStudent>>

    @Query("""
        SELECT * FROM event_students 
        WHERE eventId = :eventId AND enrollmentNo LIKE '%' || :query || '%' 
        ORDER BY enrollmentNo ASC
    """)
    fun searchByEnrollmentSubstring(eventId: String, query: String): Flow<List<EventStudent>>

    @Query("""
        SELECT * FROM event_students 
        WHERE eventId = :eventId AND name LIKE '%' || :query || '%' 
        ORDER BY name ASC
    """)
    fun searchByName(eventId: String, query: String): Flow<List<EventStudent>>

    @Query("""
        SELECT * FROM event_students 
        WHERE eventId = :eventId AND (enrollmentNo LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%')
        ORDER BY name ASC
    """)
    fun searchCombined(eventId: String, query: String): Flow<List<EventStudent>>

    @Query("SELECT * FROM event_students WHERE eventId = :eventId ORDER BY name ASC")
    fun getAllStudentsForEvent(eventId: String): Flow<List<EventStudent>>

    @Query("SELECT * FROM event_students WHERE eventId = :eventId ORDER BY name ASC")
    suspend fun getStudentsListForEvent(eventId: String): List<EventStudent>

    @Query("SELECT COUNT(*) FROM event_students WHERE eventId = :eventId")
    fun getStudentCountForEvent(eventId: String): Flow<Int>

    @Query("DELETE FROM event_students WHERE eventId = :eventId")
    suspend fun clearRosterForEvent(eventId: String)
}
