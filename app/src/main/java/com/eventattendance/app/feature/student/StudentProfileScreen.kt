package com.eventattendance.app.feature.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.eventattendance.app.core.audio.SoundFeedbackManager
import com.eventattendance.app.core.designsystem.PrimaryActionButton
import com.eventattendance.app.core.designsystem.StatusBadge
import com.eventattendance.app.core.designsystem.SuccessGreen
import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.model.AttendanceOutcome
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.repository.AttendanceRepository
import com.eventattendance.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val soundManager: SoundFeedbackManager,
    private val preferences: UserPreferences
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])
    val enrollmentNo: String = checkNotNull(savedStateHandle["enrollmentNo"])

    val event: StateFlow<Event?> = eventRepository.getEventById(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val student: StateFlow<EventStudent?> = attendanceRepository.observeStudent(eventId, enrollmentNo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val attendanceRecord: StateFlow<AttendanceRecord?> = attendanceRepository.observeAttendance(eventId, enrollmentNo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    var isSubmitting by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    var feedbackIsError by mutableStateOf(false)
        private set

    fun markAttendance(onSuccessDone: () -> Unit) {
        viewModelScope.launch {
            isSubmitting = true
            feedbackMessage = null
            val operatorName = preferences.operatorNameFlow.first() ?: "Operator"

            when (val outcome = attendanceRepository.markAttendance(eventId, enrollmentNo, operatorName, "android-app")) {
                is AttendanceOutcome.Success -> {
                    soundManager.playSuccessFeedback()
                    feedbackMessage = "Attendance marked successfully!"
                    feedbackIsError = false
                    isSubmitting = false
                    onSuccessDone()
                }
                is AttendanceOutcome.AlreadyMarked -> {
                    soundManager.playWarningFeedback()
                    feedbackMessage = "Already marked at ${outcome.markedAt.take(19)} by ${outcome.operatorName ?: "Operator"}"
                    feedbackIsError = false
                    isSubmitting = false
                }
                is AttendanceOutcome.StudentNotRegistered -> {
                    feedbackMessage = "Student is not registered for this event."
                    feedbackIsError = true
                    isSubmitting = false
                }
                is AttendanceOutcome.EventClosed -> {
                    feedbackMessage = "Attendance is closed for this event."
                    feedbackIsError = true
                    isSubmitting = false
                }
                is AttendanceOutcome.PermissionDenied -> {
                    feedbackMessage = "Permission denied."
                    feedbackIsError = true
                    isSubmitting = false
                }
                is AttendanceOutcome.Failure -> {
                    feedbackMessage = outcome.message
                    feedbackIsError = true
                    isSubmitting = false
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentProfileViewModel = hiltViewModel()
) {
    val event by viewModel.event.collectAsState()
    val student by viewModel.student.collectAsState()
    val attendanceRecord by viewModel.attendanceRecord.collectAsState()
    val isPresent = attendanceRecord != null && attendanceRecord?.status == "present"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Profile", fontWeight = FontWeight.Bold) },
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
        if (student == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val s = student!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Event Context & Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Event",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = event?.title ?: "Event",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            StatusBadge(status = if (isPresent) "PRESENT" else "UNMARKED")
                        }
                    }

                    // Exactly 6 Student Data Fields Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Student Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            // 1. Name (Required)
                            ProfileFieldRow(
                                label = "Name",
                                value = s.name,
                                icon = Icons.Default.Person
                            )

                            // 2. Enrollment Number (Required)
                            ProfileFieldRow(
                                label = "Enrollment Number",
                                value = s.enrollmentNo,
                                icon = Icons.Default.Badge
                            )

                            // 3. Section (Optional)
                            ProfileFieldRow(
                                label = "Section",
                                value = s.section ?: "—",
                                icon = Icons.Default.Class
                            )

                            // 4. Roll Number (Optional)
                            ProfileFieldRow(
                                label = "Roll Number",
                                value = s.rollNo ?: "—",
                                icon = Icons.Default.FormatListNumbered
                            )

                            // 5. Email ID (Optional)
                            ProfileFieldRow(
                                label = "Email ID",
                                value = s.email ?: "—",
                                icon = Icons.Default.Email
                            )

                            // 6. Mobile Number (Optional)
                            ProfileFieldRow(
                                label = "Mobile Number",
                                value = s.mobile ?: "—",
                                icon = Icons.Default.Phone
                            )
                        }
                    }

                    if (viewModel.feedbackMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.feedbackIsError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = viewModel.feedbackMessage!!,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (viewModel.feedbackIsError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Action Button: Mark Attendance or Already Marked State
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isPresent) {
                        Button(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = SuccessGreen.copy(alpha = 0.2f),
                                disabledContentColor = SuccessGreen
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Text("Attendance Already Marked", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        val isAllowed = event?.isAttendanceAllowed ?: true
                        PrimaryActionButton(
                            text = if (isAllowed) "Mark Attendance" else "Attendance Closed",
                            onClick = {
                                viewModel.markAttendance(onSuccessDone = onNavigateBack)
                            },
                            enabled = isAllowed,
                            isLoading = viewModel.isSubmitting,
                            icon = Icons.Default.CheckCircle
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel / Return to Search")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileFieldRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
