package com.eventattendance.app.feature.workspace

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.*
import com.eventattendance.app.core.network.NetworkMonitor
import com.eventattendance.app.core.network.OfflineSyncManager
import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.remote.SupabaseRealtimeManager
import com.eventattendance.app.data.repository.AttendanceRepository
import com.eventattendance.app.data.repository.EventRepository
import com.eventattendance.app.domain.usecase.SearchResultState
import com.eventattendance.app.domain.usecase.SearchStudentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceWorkspaceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val searchUseCase: SearchStudentsUseCase,
    private val realtimeManager: SupabaseRealtimeManager,
    private val networkMonitor: NetworkMonitor,
    private val offlineSyncManager: OfflineSyncManager,
    private val preferences: UserPreferences
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    val event: StateFlow<Event?> = eventRepository.getEventById(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val pendingSyncCount: StateFlow<Int> = attendanceRepository.getPendingSyncCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val fullRoster = attendanceRepository.getRoster(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords = attendanceRepository.getAllAttendanceRecords(eventId)
        .map { list -> list.associateBy { it.enrollmentNo } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalRegistered = attendanceRepository.getStudentCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPresent = attendanceRepository.getAttendanceCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _autoNavigateStudent = MutableStateFlow<EventStudent?>(null)
    val autoNavigateStudent: StateFlow<EventStudent?> = _autoNavigateStudent.asStateFlow()

    private var lastNavigatedEnrollment: String? = null

    val searchResultState: StateFlow<SearchResultState> = combine(
        _searchQuery,
        fullRoster
    ) { query, roster ->
        val result = searchUseCase.execute(query, roster)

        if (result.isExactUniqueMatch) {
            val student = result.exactUniqueMatch!!
            if (lastNavigatedEnrollment != student.enrollmentNo) {
                lastNavigatedEnrollment = student.enrollmentNo
                _autoNavigateStudent.value = student
            }
        } else {
            lastNavigatedEnrollment = null
            _autoNavigateStudent.value = null
        }

        result
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SearchResultState("", emptyList(), null)
    )

    init {
        realtimeManager.subscribeToEvent(eventId)
    }

    override fun onCleared() {
        super.onCleared()
        realtimeManager.unsubscribe()
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onNavigatedToProfile() {
        _autoNavigateStudent.value = null
    }

    fun syncNow() {
        offlineSyncManager.triggerSync(eventId)
    }

    fun resetSearch() {
        _searchQuery.value = ""
        lastNavigatedEnrollment = null
        _autoNavigateStudent.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceWorkspaceScreen(
    onNavigateToProfile: (enrollmentNo: String) -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AttendanceWorkspaceViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResultState.collectAsState()
    val attendanceMap by viewModel.attendanceRecords.collectAsState()
    val totalRegistered by viewModel.totalRegistered.collectAsState()
    val totalPresent by viewModel.totalPresent.collectAsState()
    val autoNavigateStudent by viewModel.autoNavigateStudent.collectAsState()

    LaunchedEffect(autoNavigateStudent) {
        autoNavigateStudent?.let { student ->
            viewModel.onNavigatedToProfile()
            onNavigateToProfile(student.enrollmentNo)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = event?.title ?: "Attendance Workspace",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Present: $totalPresent / $totalRegistered",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    StatusBadge(
                        status = if (isOnline) "ONLINE" else "OFFLINE",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToScanner,
                icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                text = { Text("Open Scanner") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Offline Pending Sync Notification Banner
            AnimatedVisibility(
                visible = pendingSyncCount > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = WarningAmberDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "$pendingSyncCount record(s) queued offline",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = WarningAmberDark
                            )
                        }
                        TextButton(
                            onClick = viewModel::syncNow,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Sync Now", fontWeight = FontWeight.Bold, color = WarningAmberDark)
                        }
                    }
                }
            }

            // Unified Search Field: Searches both Enrollment Number & Student Name
            SearchBarField(
                query = searchQuery,
                onQueryChange = viewModel::onQueryChange,
                placeholderText = "Search by enrollment no. or name..."
            )

            // Results List
            if (searchResult.filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "No matching student found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Check the enrollment number or student name spelling.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResult.filteredStudents, key = { it.id }) { student ->
                        val attendance = attendanceMap[student.enrollmentNo]
                        val isPresent = attendance != null && attendance.status == "present"
                        val isPendingSync = attendance?.syncStatus == "pending_sync"

                        StudentRosterCard(
                            student = student,
                            isPresent = isPresent,
                            isPendingSync = isPendingSync,
                            markedAt = attendance?.markedAt,
                            onClick = {
                                onNavigateToProfile(student.enrollmentNo)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentRosterCard(
    student: EventStudent,
    isPresent: Boolean,
    isPendingSync: Boolean = false,
    markedAt: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ID: ${student.enrollmentNo}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!student.section.isNullOrBlank()) {
                        Text(
                            text = "Sec: ${student.section}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!student.rollNo.isNullOrBlank()) {
                        Text(
                            text = "Roll: ${student.rollNo}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val badgeStatus = when {
                !isPresent -> "UNMARKED"
                isPendingSync -> "PENDING_SYNC"
                else -> "PRESENT"
            }
            StatusBadge(status = badgeStatus)
        }
    }
}
