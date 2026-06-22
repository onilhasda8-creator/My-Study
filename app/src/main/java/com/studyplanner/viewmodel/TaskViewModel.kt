package com.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyplanner.data.model.Task
import com.studyplanner.data.model.TaskWithSubject
import com.studyplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TaskFilter { ALL, PENDING, COMPLETED }

data class TaskUiState(
    val tasks: List<TaskWithSubject> = emptyList(),
    val filter: TaskFilter = TaskFilter.ALL,
    val selectedSubjectId: Long? = null,
    val isLoading: Boolean = false,
    val completedCount: Int = 0,
    val totalCount: Int = 0
)

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    private val _selectedSubjectId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TaskUiState> = combine(
        _filter,
        _selectedSubjectId,
        taskRepository.allTasksWithSubject,
        taskRepository.completedTaskCount,
        taskRepository.totalTaskCount
    ) { filter, subjectId, allTasks, completedCount, totalCount ->
        val filtered = allTasks
            .filter { twSubject ->
                when (filter) {
                    TaskFilter.ALL -> true
                    TaskFilter.PENDING -> !twSubject.task.completed
                    TaskFilter.COMPLETED -> twSubject.task.completed
                }
            }
            .filter { twSubject ->
                subjectId == null || twSubject.task.subjectId == subjectId
            }
        TaskUiState(
            tasks = filtered,
            filter = filter,
            selectedSubjectId = subjectId,
            completedCount = completedCount,
            totalCount = totalCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskUiState(isLoading = true)
    )

    val todayTasks: StateFlow<List<TaskWithSubject>> = taskRepository.getTasksDueToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val upcomingTasks: StateFlow<List<TaskWithSubject>> = taskRepository.getUpcomingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setFilter(filter: TaskFilter) { _filter.value = filter }

    fun setSubjectFilter(subjectId: Long?) { _selectedSubjectId.value = subjectId }

    fun addTask(title: String, description: String, subjectId: Long, dueDate: Long?) {
        viewModelScope.launch {
            taskRepository.insertTask(
                Task(title = title, description = description, subjectId = subjectId, dueDate = dueDate)
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { taskRepository.updateTask(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { taskRepository.deleteTask(task) }
    }

    fun toggleTaskCompleted(taskId: Long, completed: Boolean) {
        viewModelScope.launch { taskRepository.setTaskCompleted(taskId, !completed) }
    }
}
