package com.eventattendance.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eventattendance.app.feature.admin.*
import com.eventattendance.app.feature.auth.AdminLoginScreen
import com.eventattendance.app.feature.diagnostics.DiagnosticsScreen
import com.eventattendance.app.feature.events.EventCreateEditScreen
import com.eventattendance.app.feature.events.EventSelectionScreen
import com.eventattendance.app.feature.importexport.SpreadsheetImportScreen
import com.eventattendance.app.feature.onboarding.OperatorOnboardingScreen
import com.eventattendance.app.feature.scanner.ContinuousScannerScreen
import com.eventattendance.app.feature.settings.SettingsScreen
import com.eventattendance.app.feature.splash.SplashScreen
import com.eventattendance.app.feature.student.StudentProfileScreen
import com.eventattendance.app.feature.workspace.AttendanceWorkspaceScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToEvents = {
                    navController.navigate(Screen.EventSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OperatorOnboardingScreen(
                onNavigateToEvents = {
                    navController.navigate(Screen.EventSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onNavigateToAdminLogin = {
                    navController.navigate(Screen.AdminLogin.route)
                }
            )
        }

        composable(Screen.AdminLogin.route) {
            AdminLoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Screen.EventSelection.route) {
                        popUpTo(Screen.AdminLogin.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EventSelection.route) {
            EventSelectionScreen(
                onEventSelected = { eventId ->
                    navController.navigate(Screen.AttendanceWorkspace.createRoute(eventId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToDiagnostics = {
                    navController.navigate(Screen.Diagnostics.route)
                }
            )
        }

        composable(
            route = Screen.AttendanceWorkspace.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            AttendanceWorkspaceScreen(
                onNavigateToProfile = { enrollmentNo ->
                    navController.navigate(Screen.StudentProfile.createRoute(eventId, enrollmentNo))
                },
                onNavigateToScanner = {
                    navController.navigate(Screen.ContinuousScanner.createRoute(eventId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.StudentProfile.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("enrollmentNo") { type = NavType.StringType }
            )
        ) {
            StudentProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ContinuousScanner.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            ContinuousScannerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AdminDashboard.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            AdminDashboardScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWorkspace = {
                    navController.navigate(Screen.AttendanceWorkspace.createRoute(eventId))
                },
                onNavigateToImport = {
                    navController.navigate(Screen.SpreadsheetImport.createRoute(eventId))
                },
                onNavigateToRoster = {
                    navController.navigate(Screen.RosterManagement.createRoute(eventId))
                },
                onNavigateToMonitoring = {
                    navController.navigate(Screen.AttendanceMonitoring.createRoute(eventId))
                },
                onNavigateToAuditLogs = {
                    navController.navigate(Screen.AuditLog.createRoute(eventId))
                },
                onNavigateToEditEvent = {
                    navController.navigate(Screen.EventCreateEdit.createRoute(eventId))
                },
                onNavigateToUserManagement = {
                    navController.navigate(Screen.UserManagement.route)
                }
            )
        }

        composable(
            route = Screen.EventCreateEdit.route,
            arguments = listOf(navArgument("eventId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            EventCreateEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SpreadsheetImport.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            SpreadsheetImportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RosterManagement.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            RosterManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onStudentClick = { enrollmentNo ->
                    navController.navigate(Screen.StudentProfile.createRoute(eventId, enrollmentNo))
                }
            )
        }

        composable(
            route = Screen.AttendanceMonitoring.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            AttendanceMonitoringScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AuditLog.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) {
            AuditLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UserManagement.route) {
            UserManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
