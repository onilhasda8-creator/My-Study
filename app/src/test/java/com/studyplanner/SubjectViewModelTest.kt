package com.studyplanner

import com.studyplanner.data.model.Subject
import com.studyplanner.data.repository.StudySessionRepository
import com.studyplanner.data.repository.SubjectRepository
import com.studyplanner.viewmodel.SubjectUiState
import com.studyplanner.viewmodel.SubjectViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class SubjectViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var subjectRepository: SubjectRepository
    private lateinit var sessionRepository: StudySessionRepository
    private lateinit var viewModel: SubjectViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        subjectRepository = mock()
        sessionRepository = mock()
        val subjects = listOf(
            Subject(id = 1, name = "Maths",   colorHex = "#4A45C0"),
            Subject(id = 2, name = "Biology", colorHex = "#006B5F")
        )
        whenever(subjectRepository.allSubjects).thenReturn(flowOf(subjects))
        whenever(subjectRepository.subjectCount).thenReturn(flowOf(2))
        whenever(sessionRepository.getTotalMinutesBySubject(any())).thenReturn(flowOf(60))
        viewModel = SubjectViewModel(subjectRepository, sessionRepository)
    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun uiState_emitsSubjectList() = runTest {
        val state = viewModel.uiState.value
        assertEquals(2, state.subjects.size)
        assertEquals("Maths", state.subjects[0].name)
    }

    @Test
    fun addSubject_callsRepository() = runTest {
        whenever(subjectRepository.insertSubject(any())).thenReturn(3L)
        viewModel.addSubject("History", "#7B4F00")
        verify(subjectRepository).insertSubject(
            argThat { name == "History" && colorHex == "#7B4F00" }
        )
    }

    @Test
    fun deleteSubject_callsRepository() = runTest {
        val subject = Subject(id = 1, name = "Physics", colorHex = "#4A45C0")
        viewModel.deleteSubject(subject)
        verify(subjectRepository).deleteSubject(subject)
    }
}
