package com.eventattendance.app.core.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val details: String? = null
)

object AppLogger {
    private const val TAG = "EventAttendance"
    private const val MAX_ENTRIES = 100
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()

    fun i(tag: String, message: String) {
        Log.i(TAG, "[$tag] $message")
        addEntry(LogLevel.INFO, tag, message)
    }

    fun s(tag: String, message: String) {
        Log.i(TAG, "[$tag] SUCCESS: $message")
        addEntry(LogLevel.SUCCESS, tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(TAG, "[$tag] WARN: $message")
        addEntry(LogLevel.WARNING, tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val details = throwable?.stackTraceToString()
        Log.e(TAG, "[$tag] ERROR: $message", throwable)
        addEntry(LogLevel.ERROR, tag, message, details)
    }

    private fun addEntry(level: LogLevel, tag: String, message: String, details: String? = null) {
        val entry = LogEntry(
            timestamp = timeFormat.format(Date()),
            level = level,
            tag = tag,
            message = message,
            details = details
        )
        val current = _logs.value.toMutableList()
        current.add(0, entry) // Newest first
        if (current.size > MAX_ENTRIES) {
            current.removeAt(current.size - 1)
        }
        _logs.value = current
    }

    fun clear() {
        _logs.value = emptyList()
    }

    fun exportReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== Event Attendance System Diagnostic Report ===")
        sb.appendLine("Generated at: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        sb.appendLine("Total Entries: ${_logs.value.size}")
        sb.appendLine("--------------------------------------------------")
        _logs.value.reversed().forEach { entry ->
            sb.appendLine("[${entry.timestamp}] [${entry.level}] [${entry.tag}] ${entry.message}")
            if (!entry.details.isNullOrBlank()) {
                sb.appendLine("   Details: ${entry.details.take(300)}...")
            }
        }
        sb.appendLine("==================================================")
        return sb.toString()
    }
}
