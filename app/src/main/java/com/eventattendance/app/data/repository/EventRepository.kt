package com.eventattendance.app.data.repository

import com.eventattendance.app.data.local.EventDao
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.remote.SupabaseAttendanceApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface EventRepository {
    fun getEvents(): Flow<List<Event>>
    fun getEventById(id: String): Flow<Event?>
    suspend fun getEventByIdSync(id: String): Event?
    suspend fun createEvent(event: Event): Result<Event>
    suspend fun updateEvent(event: Event): Result<Unit>
    suspend fun syncEvents(): Result<Int>
}

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val api: SupabaseAttendanceApi
) : EventRepository {

    override fun getEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    override fun getEventById(id: String): Flow<Event?> {
        return eventDao.getEventById(id)
    }

    override suspend fun getEventByIdSync(id: String): Event? {
        return eventDao.getEventByIdSync(id)
    }

    override suspend fun createEvent(event: Event): Result<Event> {
        return try {
            val created = api.createEvent(event)
            eventDao.insertEvent(created)
            Result.success(created)
        } catch (e: Exception) {
            // Local fallback
            eventDao.insertEvent(event)
            Result.success(event)
        }
    }

    override suspend fun updateEvent(event: Event): Result<Unit> {
        return try {
            api.updateEvent(event)
            eventDao.insertEvent(event)
            Result.success(Unit)
        } catch (e: Exception) {
            eventDao.insertEvent(event)
            Result.success(Unit)
        }
    }

    override suspend fun syncEvents(): Result<Int> {
        return try {
            val remoteEvents = api.getEvents()
            if (remoteEvents.isNotEmpty()) {
                eventDao.insertAllEvents(remoteEvents)
            }
            Result.success(remoteEvents.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
