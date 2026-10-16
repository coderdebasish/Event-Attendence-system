package com.eventattendance.app.domain.usecase

import com.eventattendance.app.data.model.EventStudent

data class SearchResultState(
    val query: String,
    val filteredStudents: List<EventStudent>,
    val exactUniqueMatch: EventStudent? = null
) {
    val isExactUniqueMatch: Boolean get() = exactUniqueMatch != null
}

class SearchStudentsUseCase {

    fun execute(query: String, roster: List<EventStudent>): SearchResultState {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return SearchResultState(
                query = query,
                filteredStudents = roster,
                exactUniqueMatch = null
            )
        }

        // 1. Filter candidates locally matching enrollment number or student name
        val filtered = roster.filter { student ->
            student.enrollmentNo.contains(trimmed, ignoreCase = true) ||
            student.name.contains(trimmed, ignoreCase = true)
        }

        // 2. Check for exact enrollment-number match (Strongest identifier)
        val exactEnrollmentMatches = roster.filter { student ->
            student.enrollmentNo.equals(trimmed, ignoreCase = true)
        }

        if (exactEnrollmentMatches.size == 1) {
            return SearchResultState(
                query = query,
                filteredStudents = filtered,
                exactUniqueMatch = exactEnrollmentMatches.first()
            )
        }

        // 3. Check for exact full-name match (Case & whitespace normalized)
        val exactNameMatches = roster.filter { student ->
            student.name.trim().equals(trimmed, ignoreCase = true)
        }

        // If exactly one candidate has this exact full name -> trigger auto-navigation
        // If multiple students share the exact same name -> do not auto-open, show candidates
        if (exactNameMatches.size == 1) {
            return SearchResultState(
                query = query,
                filteredStudents = filtered,
                exactUniqueMatch = exactNameMatches.first()
            )
        }

        // Partial match or ambiguous duplicate name: display list without auto-navigating
        return SearchResultState(
            query = query,
            filteredStudents = filtered,
            exactUniqueMatch = null
        )
    }
}
