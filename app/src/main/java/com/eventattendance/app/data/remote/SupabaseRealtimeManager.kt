package com.eventattendance.app.data.remote

import com.eventattendance.app.data.local.AttendanceDao
import com.eventattendance.app.data.model.AttendanceRecord
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseRealtimeManager @Inject constructor(
    private val clientProvider: SupabaseClientProvider,
    private val attendanceDao: AttendanceDao
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var activeChannel: RealtimeChannel? = null
    private var subscriptionJob: Job? = null

    /**
     * Subscribes to Realtime attendance events for the active event.
     */
    fun subscribeToEvent(eventId: String) {
        subscriptionJob?.cancel()
        subscriptionJob = scope.launch {
            try {
                val client = clientProvider.getClient()
                activeChannel = client.realtime.channel("event_attendance_$eventId")

                val changeFlow = activeChannel!!.postgresChangeFlow<AttendanceRecord>(schema = "public") {
                    table = "attendance_records"
                    filter = "event_id=eq.$eventId"
                }

                activeChannel!!.subscribe()

                changeFlow
                    .catch { /* Handle connection retries silently */ }
                    .collect { change ->
                        // Synchronize incoming realtime attendance record into local Room database
                        change.record?.let { record ->
                            attendanceDao.insertAttendance(record)
                        }
                    }
            } catch (_: Exception) {
                // Network unavailable or realtime disabled fallback
            }
        }
    }

    fun unsubscribe() {
        subscriptionJob?.cancel()
        subscriptionJob = null
        scope.launch {
            try {
                activeChannel?.unsubscribe()
                activeChannel = null
            } catch (_: Exception) {}
        }
    }
}
