package com.eventattendance.app.feature.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.*
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.repository.AttendanceRepository
import com.eventattendance.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    val event: StateFlow<Event?> = eventRepository.getEventById(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val totalRegistered: StateFlow<Int> = attendanceRepository.getStudentCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPresent: StateFlow<Int> = attendanceRepository.getAttendanceCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleAttendanceAllowed() {
        val current = event.value ?: return
        viewModelScope.launch {
            val updated = current.copy(allowAttendance = !current.allowAttendance)
            eventRepository.updateEvent(updated)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWorkspace: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToRoster: () -> Unit,
    onNavigateToMonitoring: () -> Unit,
    onNavigateToAuditLogs: () -> Unit,
    onNavigateToEditEvent: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val totalRegistered by viewModel.totalRegistered.collectAsState()
    val totalPresent by viewModel.totalPresent.collectAsState()
    val attendanceRate = if (totalRegistered > 0) ((totalPresent * 100) / totalRegistered) else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEditEvent) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Event")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Event Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = event?.title ?: "Event",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadge(status = event?.status ?: "OPEN")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attendance Status: ${if (event?.allowAttendance == true) "Marking Open" else "Closed"}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (event?.allowAttendance == true) SuccessGreen else ErrorRose
                        )

                        Switch(
                            checked = event?.allowAttendance == true,
                            onCheckedChange = { viewModel.toggleAttendanceAllowed() }
                        )
                    }
                }
            }

            // Live Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Registered",
                    value = "$totalRegistered",
                    icon = Icons.Default.People,
                    iconTint = BrandPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Present",
                    value = "$totalPresent",
                    subtitle = "$attendanceRate% rate",
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Administrative Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Management Action Tiles
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    AdminActionTile(
                        title = "Mark Attendance",
                        subtitle = "Operator workspace",
                        icon = Icons.Default.HowToReg,
                        tint = BrandPrimary,
                        onClick = onNavigateToWorkspace
                    )
                }
                item {
                    AdminActionTile(
                        title = "Import Roster",
                        subtitle = "Excel / CSV upload",
                        icon = Icons.Default.UploadFile,
                        tint = BrandAccent,
                        onClick = onNavigateToImport
                    )
                }
                item {
                    AdminActionTile(
                        title = "Student Roster",
                        subtitle = "Manage registered list",
                        icon = Icons.Default.Badge,
                        tint = InfoBlue,
                        onClick = onNavigateToRoster
                    )
                }
                item {
                    AdminActionTile(
                        title = "Live Monitoring",
                        subtitle = "Stats & CSV export",
                        icon = Icons.Default.BarChart,
                        tint = SuccessGreen,
                        onClick = onNavigateToMonitoring
                    )
                }
                item {
                    AdminActionTile(
                        title = "Audit History",
                        subtitle = "Corrections & logs",
                        icon = Icons.Default.History,
                        tint = WarningAmber,
                        onClick = onNavigateToAuditLogs
                    )
                }
                item {
                    AdminActionTile(
                        title = "Event Settings",
                        subtitle = "Date, rules, status",
                        icon = Icons.Default.Settings,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onNavigateToEditEvent
                    )
                }
                item {
                    AdminActionTile(
                        title = "Manage Users",
                        subtitle = "Add operators & admins",
                        icon = Icons.Default.GroupAdd,
                        tint = BrandPrimary,
                        onClick = onNavigateToUserManagement
                    )
                }
            }
        }
    }
}

@Composable
fun AdminActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(tint.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
