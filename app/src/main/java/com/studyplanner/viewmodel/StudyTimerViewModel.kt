package com.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyplanner.data.model.StudySession
import com.studyplanner.data.model.Subject
import com.studyplanner.data.repository.StudySessionRepository
import com.studyplanner.data.repository.SubjectRepository
import com.studyplanner.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val selectedSubject: Subject? = null,
    val subjects: List<Subject> = emptyList(),
    val lastSavedSession: StudySession? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class StudyTimerViewModel @Inject constructor(
    private val sessionRepository: StudySessionRepository,
    private val subjectRepository: SubjectRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            subjectRepository.allSubjects.collect { subjects ->
                _timerState.update { state ->
                    // Auto-select first subject if nothing selected yet
                    val selected = state.selectedSubject
                        ?: subjects.firstOrNull()
                    state.copy(subjects = subjects, selectedSubject = selected)
                }
            }
        }
    }

    fun selectSubject(subject: Subject) {
        if (_timerState.value.isRunning) return
        _timerState.update { it.copy(selectedSubject = subject) }
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        if (_timerState.value.selectedSubject == null) {
            _timerState.update { it.copy(errorMessage = "Please select a subject first.") }
            return
        }
        _timerState.update { it.copy(isRunning = true, errorMessage = null, lastSavedSession = null) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _timerState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.update { it.copy(isRunning = false) }
    }

    fun saveSession() {
        val state   = _timerState.value
        val subject = state.selectedSubject ?: return
        val minutes = state.elapsedSeconds / 60
        if (minutes < 1) {
            _timerState.update { it.copy(errorMessage = "Session too short — study for at least 1 minute.") }
            return
        }

        viewModelScope.launch {
            val session = StudySession(subjectId = subject.id, durationMinutes = minutes)
            val id      = sessionRepository.insertSession(session)
            notificationHelper.showSessionSavedNotification(subject.name, minutes)
            _timerState.update {
                it.copy(
                    lastSavedSession = session.copy(id = id),
                    elapsedSeconds   = 0,
                    isRunning        = false,
                    errorMessage     = null
                )
            }
        }
    }

    fun resetTimer() {
        stopTimer()
        _timerState.update { it.copy(elapsedSeconds = 0, lastSavedSession = null, errorMessage = null) }
    }

    fun clearError() {
        _timerState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
