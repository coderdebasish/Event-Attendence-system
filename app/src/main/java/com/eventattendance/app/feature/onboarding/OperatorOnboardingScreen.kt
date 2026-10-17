package com.eventattendance.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eventattendance.app.core.designsystem.BrandAccent
import com.eventattendance.app.core.designsystem.BrandPrimary
import com.eventattendance.app.core.designsystem.PrimaryActionButton
import com.eventattendance.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var operatorName by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onNameChange(name: String) {
        operatorName = name
        errorMessage = null
    }

    fun submitOperator(onSuccess: () -> Unit) {
        val trimmed = operatorName.trim()
        if (trimmed.length < 2) {
            errorMessage = "Please enter a valid operator name (at least 2 characters)."
            return
        }

        viewModelScope.launch {
            isLoading = true
            val result = authRepository.loginAsOperator(trimmed)
            isLoading = false
            if (result.isSuccess) {
                onSuccess()
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to set up operator."
            }
        }
    }
}

@Composable
fun OperatorOnboardingScreen(
    onNavigateToEvents: () -> Unit,
    onNavigateToAdminLogin: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(BrandPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = BrandPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Operator Onboarding",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter your display name to begin scanning and marking attendance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    OutlinedTextField(
                        value = viewModel.operatorName,
                        onValueChange = viewModel::onNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Operator Name") },
                        placeholder = { Text("e.g. Alex Sharma") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        isError = viewModel.errorMessage != null,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (viewModel.errorMessage != null) {
                        Text(
                            text = viewModel.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    PrimaryActionButton(
                        text = "Continue to Events",
                        onClick = { viewModel.submitOperator(onNavigateToEvents) },
                        isLoading = viewModel.isLoading,
                        icon = Icons.Default.ArrowForward
                    )
                }
            }
        }
    }
}
