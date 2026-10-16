package com.eventattendance.app.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.SearchBarField
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.repository.AttendanceRepository
import com.eventattendance.app.feature.workspace.StudentRosterCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RosterManagementViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val roster = attendanceRepository.getRoster(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceMap = attendanceRepository.getAllAttendanceRecords(eventId)
        .map { list -> list.associateBy { it.enrollmentNo } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val filteredRoster: StateFlow<List<EventStudent>> = combine(_searchQuery, roster) { q, list ->
        if (q.isBlank()) list else list.filter {
            it.name.contains(q, ignoreCase = true) || it.enrollmentNo.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQueryChange(q: String) {
        _searchQuery.value = q
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RosterManagementScreen(
    onNavigateBack: () -> Unit,
    onStudentClick: (String) -> Unit,
    viewModel: RosterManagementViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredRoster by viewModel.filteredRoster.collectAsState()
    val attendanceMap by viewModel.attendanceMap.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Student Roster", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchBarField(
                query = searchQuery,
                onQueryChange = viewModel::onQueryChange,
                placeholderText = "Filter roster by name or ID..."
            )

            Text(
                text = "Total Registered: ${filteredRoster.size} students",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRoster, key = { it.id }) { student ->
                    val attendance = attendanceMap[student.enrollmentNo]
                    StudentRosterCard(
                        student = student,
                        isPresent = attendance?.status == "present",
                        markedAt = attendance?.markedAt,
                        onClick = { onStudentClick(student.enrollmentNo) }
                    )
                }
            }
        }
    }
}
