package com.eventattendance.app.data.local

import androidx.room.*
import com.eventattendance.app.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<Event>)

    @Query("SELECT * FROM events ORDER BY eventDate DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun getEventById(id: String): Flow<Event?>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdSync(id: String): Event?

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: String)
}
