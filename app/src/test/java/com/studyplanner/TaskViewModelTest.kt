package com.studyplanner

import com.studyplanner.data.model.Subject
import com.studyplanner.data.model.Task
import com.studyplanner.data.model.TaskWithSubject
import com.studyplanner.data.repository.TaskRepository
import com.studyplanner.viewmodel.TaskFilter
import com.studyplanner.viewmodel.TaskViewModel
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
class TaskViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var taskRepository: TaskRepository
    private lateinit var viewModel: TaskViewModel

    private val subject1 = Subject(id = 1, name = "Maths",   colorHex = "#4A45C0")
    private val subject2 = Subject(id = 2, name = "Biology", colorHex = "#006B5F")

    private val pendingTask = Task(
        id = 1, title = "Homework", subjectId = 1, dueDate = null, completed = false
    )
    private val completedTask = Task(
        id = 2, title = "Essay", subjectId = 2, dueDate = null, completed = true
    )

    private val allTasks = listOf(
        TaskWithSubject(pendingTask,   subject1),
        TaskWithSubject(completedTask, subject2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        taskRepository = mock()
        whenever(taskRepository.allTasksWithSubject).thenReturn(flowOf(allTasks))
        whenever(taskRepository.pendingTasks).thenReturn(flowOf(listOf(TaskWithSubject(pendingTask, subject1))))
        whenever(taskRepository.completedTaskCount).thenReturn(flowOf(1))
        whenever(taskRepository.totalTaskCount).thenReturn(flowOf(2))
        whenever(taskRepository.getTasksDueToday()).thenReturn(flowOf(emptyList()))
        whenever(taskRepository.getUpcomingTasks(any())).thenReturn(flowOf(emptyList()))
        viewModel = TaskViewModel(taskRepository)
    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun initial_state_shows_all_tasks() = runTest {
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertEquals(TaskFilter.ALL, state.filter)
    }

    @Test
    fun filter_pending_shows_only_incomplete_tasks() = runTest {
        viewModel.setFilter(TaskFilter.PENDING)
        val state = viewModel.uiState.value
        assertTrue(state.tasks.all { !it.task.completed })
        assertEquals(1, state.tasks.size)
    }

    @Test
    fun filter_completed_shows_only_complete_tasks() = runTest {
        viewModel.setFilter(TaskFilter.COMPLETED)
        val state = viewModel.uiState.value
        assertTrue(state.tasks.all { it.task.completed })
        assertEquals(1, state.tasks.size)
    }

    @Test
    fun filter_by_subject_narrows_list() = runTest {
        viewModel.setSubjectFilter(1L)
        val state = viewModel.uiState.value
        assertTrue(state.tasks.all { it.task.subjectId == 1L })
        assertEquals(1, state.tasks.size)
    }

    @Test
    fun clear_subject_filter_restores_all() = runTest {
        viewModel.setSubjectFilter(1L)
        viewModel.setSubjectFilter(null)
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
    }

    @Test
    fun add_task_calls_repository() = runTest {
        whenever(taskRepository.insertTask(any())).thenReturn(3L)
        viewModel.addTask("New Task", "desc", 1L, null)
        verify(taskRepository).insertTask(
            argThat { title == "New Task" && subjectId == 1L && !completed }
        )
    }

    @Test
    fun toggle_complete_calls_repository_with_inverted_value() = runTest {
        viewModel.toggleTaskCompleted(taskId = 1L, completed = false)
        verify(taskRepository).setTaskCompleted(1L, true)

        viewModel.toggleTaskCompleted(taskId = 2L, completed = true)
        verify(taskRepository).setTaskCompleted(2L, false)
    }

    @Test
    fun delete_task_calls_repository() = runTest {
        viewModel.deleteTask(pendingTask)
        verify(taskRepository).deleteTask(pendingTask)
    }

    @Test
    fun completed_and_total_counts_exposed_correctly() = runTest {
        val state = viewModel.uiState.value
        assertEquals(1, state.completedCount)
        assertEquals(2, state.totalCount)
    }
}
