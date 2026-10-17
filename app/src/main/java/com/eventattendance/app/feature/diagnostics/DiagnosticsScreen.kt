package com.eventattendance.app.feature.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.BuildConfig
import com.eventattendance.app.core.config.SupabaseConfig
import com.eventattendance.app.core.designsystem.*
import com.eventattendance.app.core.network.NetworkMonitor
import com.eventattendance.app.core.network.OfflineSyncManager
import com.eventattendance.app.core.util.AppLogger
import com.eventattendance.app.core.util.LogEntry
import com.eventattendance.app.core.util.LogLevel
import com.eventattendance.app.data.local.AttendanceDao
import com.eventattendance.app.data.local.EventDao
import com.eventattendance.app.data.local.StudentDao
import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.remote.SupabaseAttendanceApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val api: SupabaseAttendanceApi,
    private val eventDao: EventDao,
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val preferences: UserPreferences,
    private val networkMonitor: NetworkMonitor,
    private val offlineSyncManager: OfflineSyncManager
) : ViewModel() {

    val isOnline = networkMonitor.isOnline
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val logs = AppLogger.logs

    var supabaseUrl by mutableStateOf("")
        private set

    var isConfigured by mutableStateOf(false)
        private set

    var pingResult by mutableStateOf<String?>(null)
        private set

    var isPinging by mutableStateOf(false)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var syncResult by mutableStateOf<String?>(null)
        private set

    val eventsCount = eventDao.getAllEvents()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _totalStudents = MutableStateFlow(0)
    val totalStudents: StateFlow<Int> = _totalStudents.asStateFlow()

    private val _totalAttendance = MutableStateFlow(0)
    val totalAttendance: StateFlow<Int> = _totalAttendance.asStateFlow()

    private val _pendingSyncCount = MutableStateFlow(0)
    val pendingSyncCount: StateFlow<Int> = _pendingSyncCount.asStateFlow()

    init {
        viewModelScope.launch {
            val url = preferences.customSupabaseUrlFlow.first() ?: BuildConfig.SUPABASE_URL
            val key = preferences.customSupabaseAnonKeyFlow.first() ?: BuildConfig.SUPABASE_ANON_KEY
            supabaseUrl = SupabaseConfig.getBaseUrl(url)
            isConfigured = SupabaseConfig.isConfigured(url, key)
            refreshDatabaseStats()
        }
    }

    fun refreshDatabaseStats() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val pending = attendanceDao.getAllPendingRecords().size
                _pendingSyncCount.value = pending
            }
        }
    }

    fun pingSupabase() {
        viewModelScope.launch {
            isPinging = true
            pingResult = null
            AppLogger.i("Diagnostics", "Pinging Supabase backend at $supabaseUrl...")
            val startTime = System.currentTimeMillis()
            try {
                withContext(Dispatchers.IO) {
                    api.getEvents()
                }
                val latency = System.currentTimeMillis() - startTime
                pingResult = "Online - Roundtrip: ${latency}ms"
                AppLogger.s("Diagnostics", "Supabase ping SUCCESS in ${latency}ms")
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                pingResult = "Failed (${latency}ms): ${e.message}"
                AppLogger.e("Diagnostics", "Supabase ping FAILED", e)
            } finally {
                isPinging = false
                refreshDatabaseStats()
            }
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            isSyncing = true
            syncResult = null
            AppLogger.i("Diagnostics", "Forcing offline sync...")
            offlineSyncManager.syncAll { count ->
                syncResult = "Synced $count pending records"
                AppLogger.s("Diagnostics", "Offline sync completed ($count records synced)")
                isSyncing = false
                refreshDatabaseStats()
            }
        }
    }

    fun clearLogs() {
        AppLogger.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isOnline by viewModel.isOnline.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val eventsCount by viewModel.eventsCount.collectAsState()
    val pendingSyncCount by viewModel.pendingSyncCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Diagnostics & Health", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val report = AppLogger.exportReport()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Diagnostic Report", report))
                            Toast.makeText(context, "Diagnostic report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Report")
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
            // Backend Health Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Supabase Backend", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        StatusBadge(status = if (isOnline && viewModel.isConfigured) "CONNECTED" else "OFFLINE")
                    }

                    Text(
                        text = viewModel.supabaseUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )

                    if (viewModel.pingResult != null) {
                        val isSuccess = viewModel.pingResult!!.startsWith("Online")
                        Text(
                            text = viewModel.pingResult!!,
                            color = if (isSuccess) SuccessGreen else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = viewModel::pingSupabase,
                            enabled = !viewModel.isPinging,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (viewModel.isPinging) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pinging...")
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Ping")
                            }
                        }

                        OutlinedButton(
                            onClick = viewModel::triggerSync,
                            enabled = !viewModel.isSyncing,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Force Sync")
                        }
                    }

                    if (viewModel.syncResult != null) {
                        Text(
                            text = viewModel.syncResult!!,
                            color = BrandPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Local Database Health Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiagnosticStat(label = "Events", value = eventsCount.toString())
                    DiagnosticStat(label = "Pending Sync", value = pendingSyncCount.toString(), isHighlight = pendingSyncCount > 0)
                    DiagnosticStat(label = "Network", value = if (isOnline) "Online" else "Offline", isSuccess = isOnline)
                }
            }

            // Live Diagnostics Log Feed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Activity & Error Log (${logs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = viewModel::clearLogs) {
                    Text("Clear")
                }
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No log events captured yet.\nPerform scans or sync to view live diagnostics.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        LogEntryItem(log = log)
                    }
                }
            }
        }
    }
}

@Composable
fun DiagnosticStat(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isSuccess: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                isHighlight -> MaterialTheme.colorScheme.error
                isSuccess -> SuccessGreen
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LogEntryItem(log: LogEntry) {
    var isExpanded by remember { mutableStateOf(false) }

    val badgeColor = when (log.level) {
        LogLevel.INFO -> MaterialTheme.colorScheme.primary
        LogLevel.SUCCESS -> SuccessGreen
        LogLevel.WARNING -> WarningAmber
        LogLevel.ERROR -> MaterialTheme.colorScheme.error
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(badgeColor, CircleShape)
                    )
                    Text(
                        text = "[${log.tag}]",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeColor
                    )
                }
                Text(
                    text = log.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            AnimatedVisibility(visible = isExpanded && !log.details.isNullOrBlank()) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = log.details ?: "",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}
