package com.eventattendance.app.feature.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.BrandPrimary
import com.eventattendance.app.core.designsystem.StatusBadge
import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.repository.AttendanceRepository
import com.eventattendance.app.data.repository.AuthRepository
import com.eventattendance.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventSelectionViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository,
    private val authRepository: AuthRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val operatorName = authRepository.operatorNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Operator")

    val isAdmin = authRepository.isAdminFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val events = eventRepository.getEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        syncEvents()
    }

    fun syncEvents() {
        viewModelScope.launch {
            isRefreshing = true
            eventRepository.syncEvents()
            isRefreshing = false
        }
    }

    fun selectEvent(eventId: String, onSelected: () -> Unit) {
        viewModelScope.launch {
            preferences.setSelectedEventId(eventId)
            // Preload roster & attendance into Room SQLite for offline/sub-50ms search
            attendanceRepository.syncRoster(eventId)
            attendanceRepository.syncAttendance(eventId)
            onSelected()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventSelectionScreen(
    onEventSelected: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDiagnostics: () -> Unit = {},
    onNavigateToAdminDashboard: (String) -> Unit = {},
    onNavigateToCreateEvent: () -> Unit = {},
    viewModel: EventSelectionViewModel = hiltViewModel()
) {
    val operatorName by viewModel.operatorName.collectAsState()
    val events by viewModel.events.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Attendance Stations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Operator: ${operatorName ?: "Volunteer"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToDiagnostics) {
                        Icon(Icons.Default.MonitorHeart, contentDescription = "System Diagnostics")
                    }
                    IconButton(onClick = viewModel::syncEvents) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EventBusy,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "No Events Available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ask an event coordinator to publish an active event from the web dashboard.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(events, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onSelect = { viewModel.selectEvent(event.id) { onEventSelected(event.id) } }
                    )
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = event.status)
            }

            if (!event.description.isNullOrBlank()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.eventDate.take(10),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Open Station",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
