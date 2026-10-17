package com.eventattendance.app.feature.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.audio.SoundFeedbackManager
import com.eventattendance.app.core.designsystem.*
import com.eventattendance.app.core.util.QrParseResult
import com.eventattendance.app.core.util.StudentQrParser
import com.eventattendance.app.core.network.NetworkMonitor
import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.model.AttendanceOutcome
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.repository.AttendanceRepository
import com.eventattendance.app.data.repository.EventRepository
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

sealed class ScannerFeedback {
    data class Success(val studentName: String, val enrollmentNo: String) : ScannerFeedback()
    data class OfflineQueued(val studentName: String, val enrollmentNo: String) : ScannerFeedback()
    data class Duplicate(val studentName: String, val enrollmentNo: String, val markedAt: String) : ScannerFeedback()
    data class NotRegistered(val enrollmentNo: String) : ScannerFeedback()
    data class Error(val message: String) : ScannerFeedback()
}

@HiltViewModel
class ScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val eventRepository: EventRepository,
    private val soundManager: SoundFeedbackManager,
    private val preferences: UserPreferences,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val eventId: String = checkNotNull(savedStateHandle["eventId"])

    val event: StateFlow<Event?> = eventRepository.getEventById(eventId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    var feedback by mutableStateOf<ScannerFeedback?>(null)
        private set

    var isTorchOn by mutableStateOf(false)
        private set

    private var isInFlight = false
    private var lastScannedCode: String? = null
    private var lastScanTimestamp: Long = 0L

    fun toggleTorch() {
        isTorchOn = !isTorchOn
    }

    fun onBarcodeDetected(rawPayload: String) {
        val now = System.currentTimeMillis()

        // 1. In-flight and cooldown guards to prevent duplicate scans of the same frame
        if (isInFlight) return
        if (rawPayload == lastScannedCode && (now - lastScanTimestamp) < 2500) return

        lastScannedCode = rawPayload
        lastScanTimestamp = now

        com.eventattendance.app.core.util.AppLogger.i("Scanner", "Barcode detected: $rawPayload")

        // 2. Strict QR parsing using StudentQrParser (extracts Field 1 strictly)
        when (val parseResult = StudentQrParser.parse(rawPayload)) {
            is QrParseResult.Invalid -> {
                com.eventattendance.app.core.util.AppLogger.w("Scanner", "Invalid QR: ${parseResult.reason}")
                feedback = ScannerFeedback.Error("Invalid QR Code: ${parseResult.reason}")
                viewModelScope.launch {
                    delay(1200)
                    feedback = null
                }
            }
            is QrParseResult.Success -> {
                processAttendance(parseResult.enrollmentNumber)
            }
        }
    }

    private fun processAttendance(enrollmentNo: String) {
        viewModelScope.launch {
            isInFlight = true
            val operatorName = preferences.operatorNameFlow.first() ?: "Operator"
            com.eventattendance.app.core.util.AppLogger.i("Scanner", "Processing attendance for $enrollmentNo by $operatorName")

            when (val outcome = attendanceRepository.markAttendance(eventId, enrollmentNo, operatorName, "scanner")) {
                is AttendanceOutcome.Success -> {
                    soundManager.playSuccessFeedback()
                    com.eventattendance.app.core.util.AppLogger.s("Scanner", "Marked PRESENT: ${outcome.studentName} ($enrollmentNo)")
                    feedback = ScannerFeedback.Success(
                        studentName = outcome.studentName,
                        enrollmentNo = outcome.enrollmentNo
                    )
                    // Visible for ~1 second, then automatically resumes continuous scanning
                    delay(1000)
                    feedback = null
                    isInFlight = false
                }
                is AttendanceOutcome.OfflineQueued -> {
                    soundManager.playSuccessFeedback()
                    com.eventattendance.app.core.util.AppLogger.w("Scanner", "Offline QUEUED: ${outcome.studentName} ($enrollmentNo)")
                    feedback = ScannerFeedback.OfflineQueued(
                        studentName = outcome.studentName,
                        enrollmentNo = outcome.enrollmentNo
                    )
                    // Visible for ~1 second, then automatically resumes continuous scanning
                    delay(1000)
                    feedback = null
                    isInFlight = false
                }
                is AttendanceOutcome.AlreadyMarked -> {
                    soundManager.playWarningFeedback()
                    com.eventattendance.app.core.util.AppLogger.w("Scanner", "ALREADY MARKED: ${outcome.studentName} ($enrollmentNo) at ${outcome.markedAt}")
                    feedback = ScannerFeedback.Duplicate(
                        studentName = outcome.studentName,
                        enrollmentNo = outcome.enrollmentNo,
                        markedAt = outcome.markedAt
                    )
                    delay(1200)
                    feedback = null
                    isInFlight = false
                }
                is AttendanceOutcome.StudentNotRegistered -> {
                    soundManager.playWarningFeedback()
                    feedback = ScannerFeedback.NotRegistered(enrollmentNo)
                    delay(1400)
                    feedback = null
                    isInFlight = false
                }
                is AttendanceOutcome.EventClosed -> {
                    feedback = ScannerFeedback.Error("Attendance marking is closed for this event.")
                    delay(1400)
                    feedback = null
                    isInFlight = false
                }
                is AttendanceOutcome.PermissionDenied -> {
                    feedback = ScannerFeedback.Error("Permission denied.")
                    delay(1400)
                    feedback = null
                    isInFlight = false
                }
                is AttendanceOutcome.Failure -> {
                    feedback = ScannerFeedback.Error(outcome.message)
                    delay(1400)
                    feedback = null
                    isInFlight = false
                }
            }
        }
    }
}

@Composable
fun ContinuousScannerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    var showTestScanDialog by remember { mutableStateOf(false) }
    var testEnrollmentInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraPreviewView(
                    isTorchOn = viewModel.isTorchOn,
                    onBarcodeDetected = viewModel::onBarcodeDetected
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Camera Permission Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Grant camera access to scan student ID cards continuously.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }

            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                val isOnline by viewModel.isOnline.collectAsState()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Continuous Scanner",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = if (isOnline) "online" else "offline")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Test Simulator Button for Laptop / Emulator Testing
                    IconButton(
                        onClick = { showTestScanDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Test Scan Input",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = viewModel::toggleTorch,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (viewModel.isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = if (viewModel.isTorchOn) Color.Yellow else Color.White
                        )
                    }
                }
            }

            if (showTestScanDialog) {
                AlertDialog(
                    onDismissRequest = { showTestScanDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Laptop Test Scanner")
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Simulate scanning a student QR barcode without needing physical printouts or a camera.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = testEnrollmentInput,
                                onValueChange = { testEnrollmentInput = it },
                                label = { Text("Enrollment Number or Raw QR") },
                                placeholder = { Text("e.g. 12026105200229") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val code = testEnrollmentInput.trim()
                                if (code.isNotEmpty()) {
                                    showTestScanDialog = false
                                    testEnrollmentInput = ""
                                    viewModel.onBarcodeDetected(code)
                                }
                            }
                        ) {
                            Text("Trigger Scan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTestScanDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Scanning Reticle Overlay
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.Center)
                    .border(2.dp, BrandAccent.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            )

            // Dynamic ~1 Second Auto-Resume Feedback Banner
            AnimatedVisibility(
                visible = viewModel.feedback != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp, start = 20.dp, end = 20.dp)
            ) {
                viewModel.feedback?.let { fb ->
                    ScannerFeedbackBanner(feedback = fb)
                }
            }
        }
    }
}

@Composable
fun ScannerFeedbackBanner(feedback: ScannerFeedback) {
    val (bgColor, icon, title, subtitle) = when (feedback) {
        is ScannerFeedback.Success -> Quadruple(
            SuccessGreen,
            Icons.Default.CheckCircle,
            feedback.studentName,
            "Present: ${feedback.enrollmentNo}"
        )
        is ScannerFeedback.OfflineQueued -> Quadruple(
            BrandAccent,
            Icons.Default.CloudSync,
            feedback.studentName,
            "Queued Offline (ID: ${feedback.enrollmentNo})"
        )
        is ScannerFeedback.Duplicate -> Quadruple(
            WarningAmber,
            Icons.Default.Info,
            feedback.studentName,
            "Already Marked (ID: ${feedback.enrollmentNo})"
        )
        is ScannerFeedback.NotRegistered -> Quadruple(
            ErrorRose,
            Icons.Default.Cancel,
            "Not Registered",
            "Enrollment: ${feedback.enrollmentNo}"
        )
        is ScannerFeedback.Error -> Quadruple(
            ErrorRose,
            Icons.Default.Error,
            "Scan Error",
            feedback.message
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreviewView(
    isTorchOn: Boolean,
    onBarcodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    LaunchedEffect(isTorchOn) {
        cameraControl?.enableTorch(isTorchOn)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        barcodeScanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue
                                    if (!rawValue.isNullOrBlank()) {
                                        onBarcodeDetected(rawValue)
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    cameraControl = camera.cameraControl
                } catch (_: Exception) {}
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
