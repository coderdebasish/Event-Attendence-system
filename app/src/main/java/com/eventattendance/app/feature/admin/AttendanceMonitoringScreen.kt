package com.eventattendance.app.feature.admin

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.MetricStatCard
import com.eventattendance.app.core.designsystem.PrimaryActionButton
import com.eventattendance.app.core.designsystem.StatusBadge
import com.eventattendance.app.core.designsystem.SuccessGreen
import com.eventattendance.app.data.excel.SpreadsheetExporter
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AttendanceMonitoringViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    val attendanceRecords = attendanceRepository.getAllAttendanceRecords(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val roster = attendanceRepository.getRoster(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRegistered = attendanceRepository.getStudentCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPresent = attendanceRepository.getAttendanceCount(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    var correctionTargetRecord by mutableStateOf<AttendanceRecord?>(null)
        private set

    var correctionReason by mutableStateOf("")
        private set

    var isExporting by mutableStateOf(false)
        private set

    fun openCorrectionDialog(record: AttendanceRecord) {
        correctionTargetRecord = record
        correctionReason = ""
    }

    fun closeCorrectionDialog() {
        correctionTargetRecord = null
    }

    fun updateCorrectionReason(reason: String) {
        correctionReason = reason
    }

    fun submitCorrection(newStatus: String) {
        val record = correctionTargetRecord ?: return
        viewModelScope.launch {
            attendanceRepository.correctAttendance(
                attendanceId = record.id,
                newStatus = newStatus,
                reason = correctionReason.ifBlank { "Administrative correction" }
            )
            correctionTargetRecord = null
        }
    }

    fun exportRosterCsv(context: Context) {
        viewModelScope.launch {
            isExporting = true
            withContext(Dispatchers.IO) {
                val students = roster.value
                val attendanceMap = attendanceRecords.value.associateBy { it.enrollmentNo }

                val file = File(context.cacheDir, "event_${eventId.take(8)}_attendance.csv")
                SpreadsheetExporter.exportToFile(file, students, attendanceMap)

                try {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Event Attendance Export")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(sendIntent, "Export Attendance CSV").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                } catch (_: Exception) {
                    // Fallback sharing without fileprovider if running in test environment
                }
            }
            isExporting = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceMonitoringScreen(
    onNavigateBack: () -> Unit,
    viewModel: AttendanceMonitoringViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val records by viewModel.attendanceRecords.collectAsState()
    val roster by viewModel.roster.collectAsState()
    val studentMap = remember(roster) { roster.associateBy { it.enrollmentNo } }
    val totalRegistered by viewModel.totalRegistered.collectAsState()
    val totalPresent by viewModel.totalPresent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Attendance & Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportRosterCsv(context) }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Present",
                    value = "$totalPresent",
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Total Registered",
                    value = "$totalRegistered",
                    icon = Icons.Default.Groups,
                    iconTint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            PrimaryActionButton(
                text = "Export Attendance as CSV / Excel",
                onClick = { viewModel.exportRosterCsv(context) },
                isLoading = viewModel.isExporting,
                icon = Icons.Default.Share
            )

            Text(
                text = "Live Marked Records (${records.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    val student = studentMap[record.enrollmentNo]
                    AttendanceLogCard(
                        record = record,
                        studentName = student?.name ?: "Student",
                        onCorrect = { viewModel.openCorrectionDialog(record) }
                    )
                }
            }
        }
    }

    // Attendance Correction Dialog
    if (viewModel.correctionTargetRecord != null) {
        val record = viewModel.correctionTargetRecord!!
        AlertDialog(
            onDismissRequest = viewModel::closeCorrectionDialog,
            title = { Text("Correct Attendance Record") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Student ID: ${record.enrollmentNo}")
                    Text("Original status: ${record.status.uppercase()} marked by ${record.operatorName ?: "Operator"}")
                    OutlinedTextField(
                        value = viewModel.correctionReason,
                        onValueChange = viewModel::updateCorrectionReason,
                        label = { Text("Audit Reason *") },
                        placeholder = { Text("e.g. Scanned wrong student card accidentally") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitCorrection("corrected") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Revoke / Mark Corrected")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeCorrectionDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AttendanceLogCard(
    record: AttendanceRecord,
    studentName: String,
    onCorrect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ID: ${record.enrollmentNo} • By ${record.operatorName ?: "Operator"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = record.markedAt.take(19).replace("T", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(status = record.status)
                IconButton(onClick = onCorrect) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Correct",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
