package com.eventattendance.app.feature.importexport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.*
import com.eventattendance.app.data.excel.*
import com.eventattendance.app.data.model.EventStudent
import com.eventattendance.app.data.remote.SupabaseAttendanceApi
import com.eventattendance.app.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ImportStep {
    SELECT_FILE,
    MAP_COLUMNS,
    VALIDATION_REPORT,
    IMPORT_COMPLETE
}

@HiltViewModel
class SpreadsheetImportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val api: SupabaseAttendanceApi
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    var currentStep by mutableStateOf(ImportStep.SELECT_FILE)
        private set

    var parsedSpreadsheet by mutableStateOf<ParsedSpreadsheet?>(null)
        private set

    var mapping by mutableStateOf(SpreadsheetColumnMapping())
        private set

    var validationReport by mutableStateOf<ImportValidationReport?>(null)
        private set

    var isProcessing by mutableStateOf(false)
        private set

    var importMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var conflictResolution by mutableStateOf("skip") // "skip" or "update"
        private set

    fun onFileParsed(spreadsheet: ParsedSpreadsheet) {
        errorMessage = null
        parsedSpreadsheet = spreadsheet
        mapping = SpreadsheetColumnMapping.detectMappings(spreadsheet.headers)
        currentStep = ImportStep.MAP_COLUMNS
    }

    fun onParseError(error: String) {
        errorMessage = error
    }

    fun clearError() {
        errorMessage = null
    }

    fun updateMapping(newMapping: SpreadsheetColumnMapping) {
        mapping = newMapping
    }

    fun setConflictStrategy(strategy: String) {
        conflictResolution = strategy
    }

    fun validateFile() {
        val sheet = parsedSpreadsheet ?: return
        viewModelScope.launch {
            isProcessing = true
            withContext(Dispatchers.IO) {
                val existingStudents = attendanceRepository.getRoster(eventId).first()
                val existingEnrollments = existingStudents.map { it.enrollmentNo }.toSet()

                val report = ImportValidator.validate(
                    dataRows = sheet.rows,
                    mapping = mapping,
                    eventId = eventId,
                    existingRosterEnrollments = existingEnrollments
                )
                validationReport = report
            }
            isProcessing = false
            currentStep = ImportStep.VALIDATION_REPORT
        }
    }

    fun commitImport(onSuccess: () -> Unit) {
        val report = validationReport ?: return
        viewModelScope.launch {
            isProcessing = true
            withContext(Dispatchers.IO) {
                val studentsToImport = if (conflictResolution == "skip") {
                    val existing = attendanceRepository.getRoster(eventId).first().map { it.enrollmentNo }.toSet()
                    report.validStudents.filterNot { existing.contains(it.enrollmentNo) }
                } else {
                    report.validStudents
                }

                // 1. Save to local Room database
                attendanceRepository.saveImportedStudents(studentsToImport)

                // 2. Batch commit to Supabase if connected
                try {
                    api.importRosterBatch(
                        eventId = eventId,
                        students = studentsToImport,
                        filename = parsedSpreadsheet?.filename ?: "import.csv",
                        totalRows = report.totalRows,
                        validRows = report.validStudents.size,
                        invalidRows = report.errors.size,
                        duplicateRows = report.duplicateInFileCount
                    )
                } catch (_: Exception) {}
            }

            isProcessing = false
            currentStep = ImportStep.IMPORT_COMPLETE
            importMessage = "Successfully imported ${report.validStudents.size} students into event roster."
        }
    }

    fun reset() {
        currentStep = ImportStep.SELECT_FILE
        parsedSpreadsheet = null
        validationReport = null
        importMessage = null
        errorMessage = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: SpreadsheetImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val filename = StreamingSpreadsheetParser.resolveFileName(context, uri)
            try {
                val parsed = StreamingSpreadsheetParser.parse(context, uri, filename)
                viewModel.onFileParsed(parsed)
            } catch (e: Exception) {
                viewModel.onParseError(e.message ?: "Failed to read spreadsheet file.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Student Roster", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (viewModel.currentStep) {
                ImportStep.SELECT_FILE -> {
                    SelectFileStep(
                        errorMessage = viewModel.errorMessage,
                        onDismissError = viewModel::clearError,
                        onSelectFile = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel",
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "*/*"
                                )
                            )
                        }
                    )
                }
                ImportStep.MAP_COLUMNS -> {
                    ColumnMappingStep(
                        headers = viewModel.parsedSpreadsheet?.headers ?: emptyList(),
                        mapping = viewModel.mapping,
                        onMappingChange = viewModel::updateMapping,
                        onProceedToValidate = viewModel::validateFile,
                        isProcessing = viewModel.isProcessing
                    )
                }
                ImportStep.VALIDATION_REPORT -> {
                    ValidationReportStep(
                        report = viewModel.validationReport,
                        conflictStrategy = viewModel.conflictResolution,
                        onConflictStrategyChange = viewModel::setConflictStrategy,
                        onConfirmImport = { viewModel.commitImport(onNavigateBack) },
                        onBackToMapping = { viewModel.updateMapping(viewModel.mapping) },
                        isProcessing = viewModel.isProcessing
                    )
                }
                ImportStep.IMPORT_COMPLETE -> {
                    ImportCompleteStep(
                        message = viewModel.importMessage ?: "Import completed successfully.",
                        onDone = onNavigateBack
                    )
                }
            }
        }
    }
}

@Composable
fun SelectFileStep(
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    onSelectFile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(BrandPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Upload Registration Spreadsheet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Supports Excel (.xlsx, .xls) and CSV (.csv) files from Google Forms, ERP, or college registration portals.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (!errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismissError) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        PrimaryActionButton(
            text = "Choose Spreadsheet File",
            onClick = onSelectFile,
            icon = Icons.Default.FolderOpen
        )
    }
}

@Composable
fun ColumnMappingStep(
    headers: List<String>,
    mapping: SpreadsheetColumnMapping,
    onMappingChange: (SpreadsheetColumnMapping) -> Unit,
    onProceedToValidate: () -> Unit,
    isProcessing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Map Spreadsheet Columns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Name and Enrollment Number are required. Section, Roll Number, Email, and Mobile can be left unmapped.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Required Fields
        Text("Required Fields", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        ColumnDropdownSelector(
            label = "1. Student Name *",
            headers = headers,
            selectedIndex = mapping.nameColumnIndex,
            allowUnmapped = false,
            onSelect = { onMappingChange(mapping.copy(nameColumnIndex = it)) }
        )

        ColumnDropdownSelector(
            label = "2. Enrollment Number *",
            headers = headers,
            selectedIndex = mapping.enrollmentColumnIndex,
            allowUnmapped = false,
            onSelect = { onMappingChange(mapping.copy(enrollmentColumnIndex = it)) }
        )

        // Optional Fields
        Text("Optional Fields", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

        ColumnDropdownSelector(
            label = "3. Section (Optional)",
            headers = headers,
            selectedIndex = mapping.sectionColumnIndex,
            allowUnmapped = true,
            onSelect = { onMappingChange(mapping.copy(sectionColumnIndex = it)) }
        )

        ColumnDropdownSelector(
            label = "4. Roll Number (Optional)",
            headers = headers,
            selectedIndex = mapping.rollNoColumnIndex,
            allowUnmapped = true,
            onSelect = { onMappingChange(mapping.copy(rollNoColumnIndex = it)) }
        )

        ColumnDropdownSelector(
            label = "5. Email ID (Optional)",
            headers = headers,
            selectedIndex = mapping.emailColumnIndex,
            allowUnmapped = true,
            onSelect = { onMappingChange(mapping.copy(emailColumnIndex = it)) }
        )

        ColumnDropdownSelector(
            label = "6. Mobile Number (Optional)",
            headers = headers,
            selectedIndex = mapping.mobileColumnIndex,
            allowUnmapped = true,
            onSelect = { onMappingChange(mapping.copy(mobileColumnIndex = it)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        PrimaryActionButton(
            text = "Validate & Preview Rows",
            onClick = onProceedToValidate,
            enabled = mapping.isValid,
            isLoading = isProcessing,
            icon = Icons.Default.Check
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnDropdownSelector(
    label: String,
    headers: List<String>,
    selectedIndex: Int?,
    allowUnmapped: Boolean,
    onSelect: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = when {
        selectedIndex != null && selectedIndex in headers.indices -> "${headers[selectedIndex]} (Column ${selectedIndex + 1})"
        else -> if (allowUnmapped) "Not Mapped" else "Select Column..."
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (allowUnmapped) {
                DropdownMenuItem(
                    text = { Text("Not Mapped", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
                    onClick = {
                        onSelect(null)
                        expanded = false
                    }
                )
            }
            headers.forEachIndexed { index, header ->
                DropdownMenuItem(
                    text = { Text("$header (Col ${index + 1})") },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ValidationReportStep(
    report: ImportValidationReport?,
    conflictStrategy: String,
    onConflictStrategyChange: (String) -> Unit,
    onConfirmImport: () -> Unit,
    onBackToMapping: () -> Unit,
    isProcessing: Boolean
) {
    if (report == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Validation Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Total Rows",
                value = "${report.totalRows}",
                icon = Icons.Default.Description,
                iconTint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Valid Rows",
                value = "${report.validStudents.size}",
                icon = Icons.Default.CheckCircle,
                iconTint = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        if (report.duplicateInFileCount > 0 || report.existingInRosterCount > 0 || report.errors.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Conflicts & Warnings", fontWeight = FontWeight.Bold, color = WarningAmber)
                    if (report.duplicateInFileCount > 0) {
                        Text("• ${report.duplicateInFileCount} duplicate enrollment numbers detected within file.")
                    }
                    if (report.existingInRosterCount > 0) {
                        Text("• ${report.existingInRosterCount} students already exist in this event's roster.")
                    }
                    if (report.errors.isNotEmpty()) {
                        Text("• ${report.errors.size} invalid rows (missing required name/enrollment).", color = ErrorRose)
                    }
                }
            }
        }

        // Conflict Resolution Strategy
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Existing Records Conflict Strategy",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = conflictStrategy == "skip",
                        onClick = { onConflictStrategyChange("skip") },
                        label = { Text("Skip Existing") }
                    )
                    FilterChip(
                        selected = conflictStrategy == "update",
                        onClick = { onConflictStrategyChange("update") },
                        label = { Text("Update Details") }
                    )
                }
                Text(
                    text = "Existing attendance records are never overwritten or reset.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        PrimaryActionButton(
            text = "Confirm & Commit Import (${report.validStudents.size} Students)",
            onClick = onConfirmImport,
            isLoading = isProcessing,
            icon = Icons.Default.Save
        )
    }
}

@Composable
fun ImportCompleteStep(message: String, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(SuccessGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Import Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        PrimaryActionButton(
            text = "Back to Event Dashboard",
            onClick = onDone
        )
    }
}
