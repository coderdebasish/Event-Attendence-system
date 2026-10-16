package com.eventattendance.app.data.repository

import com.eventattendance.app.data.model.AttendanceOutcome
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.model.EventStudent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AttendanceStateTest {

    @Test
    fun eventAttendanceAllowed_whenOpen_returnsTrue() {
        val openEvent = Event(
            id = "e1",
            title = "Open Event",
            eventDate = "2026-10-17T10:00:00Z",
            status = "open",
            allowAttendance = true
        )
        assertThat(openEvent.isAttendanceAllowed).isTrue()
    }

    @Test
    fun eventAttendanceAllowed_whenClosed_returnsFalse() {
        val closedEvent = Event(
            id = "e2",
            title = "Closed Event",
            eventDate = "2026-10-17T10:00:00Z",
            status = "closed",
            allowAttendance = true
        )
        assertThat(closedEvent.isAttendanceAllowed).isFalse()
    }

    @Test
    fun eventAttendanceAllowed_whenAllowAttendanceDisabled_returnsFalse() {
        val disabledEvent = Event(
            id = "e3",
            title = "Disabled Event",
            eventDate = "2026-10-17T10:00:00Z",
            status = "open",
            allowAttendance = false
        )
        assertThat(disabledEvent.isAttendanceAllowed).isFalse()
    }

    @Test
    fun attendanceOutcome_alreadyMarked_preservesOriginalMetadata() {
        val outcome = AttendanceOutcome.AlreadyMarked(
            attendanceId = "att-123",
            studentName = "Aarav Sharma",
            enrollmentNo = "12025052002292",
            markedAt = "2026-10-17T09:30:00Z",
            operatorName = "Operator 1"
        )

        assertThat(outcome.enrollmentNo).isEqualTo("12025052002292")
        assertThat(outcome.operatorName).isEqualTo("Operator 1")
        assertThat(outcome.markedAt).isEqualTo("2026-10-17T09:30:00Z")
    }

    @Test
    fun attendanceOutcome_studentNotRegistered_identifiesEnrollment() {
        val outcome = AttendanceOutcome.StudentNotRegistered(
            enrollmentNo = "99999999999999"
        )

        assertThat(outcome.enrollmentNo).isEqualTo("99999999999999")
        assertThat(outcome.message).contains("not registered")
    }
}
