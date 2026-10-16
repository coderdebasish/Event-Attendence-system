package com.eventattendance.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.config.SupabaseConfig
import com.eventattendance.app.core.designsystem.PrimaryActionButton
import com.eventattendance.app.core.designsystem.StatusBadge
import com.eventattendance.app.core.designsystem.SuccessGreen
import com.eventattendance.app.data.local.UserPreferences
import com.eventattendance.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    val operatorName = authRepository.operatorNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Operator")

    val isAdmin = authRepository.isAdminFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    var customUrl by mutableStateOf("")
        private set

    var customKey by mutableStateOf("")
        private set

    var isConfigured by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val url = preferences.customSupabaseUrlFlow.first() ?: ""
            val key = preferences.customSupabaseAnonKeyFlow.first() ?: ""
            customUrl = url
            customKey = key
            isConfigured = SupabaseConfig.isConfigured(url, key)
        }
    }

    fun onUrlChange(u: String) {
        customUrl = u
        statusMessage = null
    }

    fun onKeyChange(k: String) {
        customKey = k
        statusMessage = null
    }

    fun saveSupabaseSettings() {
        viewModelScope.launch {
            preferences.setCustomSupabaseCredentials(
                url = customUrl.trim().ifEmpty { null },
                anonKey = customKey.trim().ifEmpty { null }
            )
            isConfigured = SupabaseConfig.isConfigured(customUrl, customKey)
            statusMessage = "Configuration saved successfully!"
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val operatorName by viewModel.operatorName.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Connection", fontWeight = FontWeight.Bold) },
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
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = operatorName ?: "Operator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAdmin) "Role: Administrator" else "Role: Attendance Operator",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = if (isAdmin) "ADMIN" else "OPERATOR")
                }
            }

            // Supabase Backend Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Supabase Backend Connection",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(status = if (viewModel.isConfigured) "ONLINE" else "SETUP_NEEDED")
                    }

                    Text(
                        text = "You can customize the Supabase URL and publishable anon key to connect to your project instance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = viewModel.customUrl,
                        onValueChange = viewModel::onUrlChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Supabase Project URL") },
                        placeholder = { Text("https://your-project.supabase.co") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.customKey,
                        onValueChange = viewModel::onKeyChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Supabase Anon Key") },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (viewModel.statusMessage != null) {
                        Text(
                            text = viewModel.statusMessage!!,
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    PrimaryActionButton(
                        text = "Save Credentials",
                        onClick = viewModel::saveSupabaseSettings,
                        icon = Icons.Default.Save
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Logout Action
            OutlinedButton(
                onClick = { viewModel.logout(onLogout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Text("Switch User / Log Out")
                }
            }
        }
    }
}
