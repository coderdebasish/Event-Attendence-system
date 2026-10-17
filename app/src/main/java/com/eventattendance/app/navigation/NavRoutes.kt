package com.eventattendance.app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object AdminLogin : Screen("admin_login")
    object EventSelection : Screen("event_selection")
    object AttendanceWorkspace : Screen("workspace/{eventId}") {
        fun createRoute(eventId: String) = "workspace/$eventId"
    }
    object StudentProfile : Screen("student/{eventId}/{enrollmentNo}") {
        fun createRoute(eventId: String, enrollmentNo: String) = "student/$eventId/$enrollmentNo"
    }
    object ContinuousScanner : Screen("scanner/{eventId}") {
        fun createRoute(eventId: String) = "scanner/$eventId"
    }
    object AdminDashboard : Screen("admin_dashboard/{eventId}") {
        fun createRoute(eventId: String) = "admin_dashboard/$eventId"
    }
    object EventCreateEdit : Screen("event_create_edit?eventId={eventId}") {
        fun createRoute(eventId: String? = null) = if (eventId != null) "event_create_edit?eventId=$eventId" else "event_create_edit"
    }
    object SpreadsheetImport : Screen("spreadsheet_import/{eventId}") {
        fun createRoute(eventId: String) = "spreadsheet_import/$eventId"
    }
    object RosterManagement : Screen("roster/{eventId}") {
        fun createRoute(eventId: String) = "roster/$eventId"
    }
    object AttendanceMonitoring : Screen("attendance_monitoring/{eventId}") {
        fun createRoute(eventId: String) = "attendance_monitoring/$eventId"
    }
    object AuditLog : Screen("audit_log/{eventId}") {
        fun createRoute(eventId: String) = "audit_log/$eventId"
    }
    object UserManagement : Screen("user_management")
    object Settings : Screen("settings")
    object Diagnostics : Screen("diagnostics")
}
