package com.eventattendance.app.feature.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Save
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
import com.eventattendance.app.core.designsystem.PrimaryActionButton
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EventCreateEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository
) : ViewModel() {

    val eventId: String? = savedStateHandle["eventId"]

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    var status by mutableStateOf("open")
        private set

    var allowAttendance by mutableStateOf(true)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        if (!eventId.isNullOrBlank()) {
            viewModelScope.launch {
                val existing = eventRepository.getEventByIdSync(eventId)
                existing?.let {
                    title = it.title
                    description = it.description ?: ""
                    status = it.status
                    allowAttendance = it.allowAttendance
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        title = newTitle
        errorMessage = null
    }

    fun onDescriptionChange(newDesc: String) {
        description = newDesc
    }

    fun onStatusChange(newStatus: String) {
        status = newStatus
    }

    fun onAllowAttendanceChange(allowed: Boolean) {
        allowAttendance = allowed
    }

    fun saveEvent(onSaved: () -> Unit) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.length < 3) {
            errorMessage = "Please enter an event title (at least 3 characters)."
            return
        }

        viewModelScope.launch {
            isSaving = true
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            val event = Event(
                id = eventId ?: UUID.randomUUID().toString(),
                title = trimmedTitle,
                description = description.trim(),
                eventDate = now,
                status = status,
                allowAttendance = allowAttendance
            )

            val result = if (eventId.isNullOrBlank()) {
                eventRepository.createEvent(event)
            } else {
                eventRepository.updateEvent(event).map { event }
            }

            isSaving = false
            if (result.isSuccess) {
                onSaved()
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to save event."
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: EventCreateEditViewModel = hiltViewModel()
) {
    val isEdit = !viewModel.eventId.isNullOrBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Event" else "Create New Event", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Event Title *") },
                placeholder = { Text("e.g. National Hackathon 2026") },
                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = viewModel.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Event Description (Optional)") },
                placeholder = { Text("Add guidelines, venue, or workshop schedule...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Event Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val statuses = listOf("draft", "published", "open", "closed", "archived")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        statuses.forEach { s ->
                            FilterChip(
                                selected = viewModel.status == s,
                                onClick = { viewModel.onStatusChange(s) },
                                label = { Text(s.uppercase(), fontSize = 11.sp) }
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Permit Attendance Marking",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Operators can mark students present when enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = viewModel.allowAttendance,
                            onCheckedChange = viewModel::onAllowAttendanceChange
                        )
                    }
                }
            }

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            PrimaryActionButton(
                text = if (isEdit) "Update Event" else "Create Event",
                onClick = { viewModel.saveEvent(onNavigateBack) },
                isLoading = viewModel.isSaving,
                icon = Icons.Default.Save
            )
        }
    }
}
