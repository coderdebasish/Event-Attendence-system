package com.eventattendance.app.core.network

import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.repository.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSyncManager @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val networkMonitor: NetworkMonitor,
    private val preferences: UserPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        scope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                if (isOnline) {
                    attendanceRepository.syncAllPendingRecords()
                    val activeEventId = preferences.selectedEventIdFlow.first()
                    if (!activeEventId.isNullOrBlank()) {
                        attendanceRepository.syncAttendance(activeEventId)
                    }
                }
            }
        }
    }

    fun triggerSync(eventId: String, onComplete: ((Int) -> Unit)? = null) {
        scope.launch {
            val result = attendanceRepository.syncPendingRecords(eventId)
            attendanceRepository.syncAttendance(eventId)
            onComplete?.invoke(result.getOrDefault(0))
        }
    }

    fun syncAll(onComplete: ((Int) -> Unit)? = null) {
        scope.launch {
            val result = attendanceRepository.syncAllPendingRecords()
            val activeEventId = preferences.selectedEventIdFlow.first()
            if (!activeEventId.isNullOrBlank()) {
                attendanceRepository.syncAttendance(activeEventId)
            }
            onComplete?.invoke(result.getOrDefault(0))
        }
    }
}
