package com.eventattendance.app.domain.usecase

import com.eventattendance.app.data.model.EventStudent
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class SearchStudentsUseCaseTest {

    private lateinit var useCase: SearchStudentsUseCase
    private lateinit var sampleRoster: List<EventStudent>

    @Before
    fun setUp() {
        useCase = SearchStudentsUseCase()
        sampleRoster = listOf(
            EventStudent(
                id = "1",
                eventId = "ev-1",
                name = "Aarav Sharma",
                enrollmentNo = "2292",
                section = "A",
                rollNo = "101"
            ),
            EventStudent(
                id = "2",
                eventId = "ev-1",
                name = "Ananya Das",
                enrollmentNo = "2294",
                section = "A",
                rollNo = "102"
            ),
            EventStudent(
                id = "3",
                eventId = "ev-1",
                name = "Arjun Roy",
                enrollmentNo = "2301",
                section = "B",
                rollNo = "201"
            ),
            EventStudent(
                id = "4",
                eventId = "ev-1",
                name = "Rohan Verma",
                enrollmentNo = "001234", // Leading zeroes test
                section = "C",
                rollNo = "301"
            ),
            EventStudent(
                id = "5",
                eventId = "ev-1",
                name = "John Doe",
                enrollmentNo = "9001",
                section = "D",
                rollNo = "401"
            ),
            EventStudent(
                id = "6",
                eventId = "ev-1",
                name = "John Doe", // Duplicate name test
                enrollmentNo = "9002",
                section = "D",
                rollNo = "402"
            )
        )
    }

    @Test
    fun execute_partialEnrollmentSearch_filtersListWithoutAutoNavigation() {
        // When typing "229", both 2292 and 2294 match, no exact match
        val state = useCase.execute("229", sampleRoster)

        assertThat(state.filteredStudents).hasSize(2)
        assertThat(state.filteredStudents.map { it.enrollmentNo }).containsExactly("2292", "2294")
        assertThat(state.isExactUniqueMatch).isFalse()
        assertThat(state.exactUniqueMatch).isNull()
    }

    @Test
    fun execute_exactUniqueEnrollmentSearch_triggersAutoNavigation() {
        // When typing "2292", exactly one candidate matches
        val state = useCase.execute("2292", sampleRoster)

        assertThat(state.filteredStudents).hasSize(1)
        assertThat(state.isExactUniqueMatch).isTrue()
        assertThat(state.exactUniqueMatch?.name).isEqualTo("Aarav Sharma")
        assertThat(state.exactUniqueMatch?.enrollmentNo).isEqualTo("2292")
    }

    @Test
    fun execute_singlePartialResultRemaining_doesNotTriggerAutoNavigation() {
        // "230" matches only "2301", but "230" != "2301" so it must NOT trigger auto-navigation!
        val state = useCase.execute("230", sampleRoster)

        assertThat(state.filteredStudents).hasSize(1)
        assertThat(state.filteredStudents.first().enrollmentNo).isEqualTo("2301")
        assertThat(state.isExactUniqueMatch).isFalse()
        assertThat(state.exactUniqueMatch).isNull()
    }

    @Test
    fun execute_leadingZeroEnrollment_exactMatchPreserved() {
        val state = useCase.execute("001234", sampleRoster)

        assertThat(state.isExactUniqueMatch).isTrue()
        assertThat(state.exactUniqueMatch?.name).isEqualTo("Rohan Verma")
    }

    @Test
    fun execute_exactUniqueNameSearch_triggersAutoNavigation() {
        val state = useCase.execute("Aarav Sharma", sampleRoster)

        assertThat(state.isExactUniqueMatch).isTrue()
        assertThat(state.exactUniqueMatch?.enrollmentNo).isEqualTo("2292")
    }

    @Test
    fun execute_exactNameSearchCaseInsensitive_triggersAutoNavigation() {
        val state = useCase.execute("  aarav sharma  ", sampleRoster)

        assertThat(state.isExactUniqueMatch).isTrue()
        assertThat(state.exactUniqueMatch?.enrollmentNo).isEqualTo("2292")
    }

    @Test
    fun execute_duplicateNameMatches_doesNotAutoNavigate() {
        // "John Doe" belongs to two different students (9001 and 9002)
        val state = useCase.execute("John Doe", sampleRoster)

        assertThat(state.filteredStudents).hasSize(2)
        assertThat(state.isExactUniqueMatch).isFalse()
        assertThat(state.exactUniqueMatch).isNull()
    }

    @Test
    fun execute_noResults_returnsEmptyFilteredList() {
        val state = useCase.execute("99999", sampleRoster)

        assertThat(state.filteredStudents).isEmpty()
        assertThat(state.isExactUniqueMatch).isFalse()
    }

    @Test
    fun execute_emptyQuery_returnsFullRosterWithoutExactMatch() {
        val state = useCase.execute("", sampleRoster)

        assertThat(state.filteredStudents).hasSize(sampleRoster.size)
        assertThat(state.isExactUniqueMatch).isFalse()
    }
}
