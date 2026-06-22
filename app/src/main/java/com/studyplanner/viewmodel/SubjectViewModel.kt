package com.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyplanner.data.model.Subject
import com.studyplanner.data.repository.StudySessionRepository
import com.studyplanner.data.repository.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectUiState(
    val subjects: List<Subject> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubjectViewModel @Inject constructor(
    private val subjectRepository: SubjectRepository,
    private val sessionRepository: StudySessionRepository
) : ViewModel() {

    val uiState: StateFlow<SubjectUiState> = subjectRepository.allSubjects
        .map { SubjectUiState(subjects = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SubjectUiState(isLoading = true)
        )

    fun getTotalMinutesForSubject(subjectId: Long): Flow<Int> =
        sessionRepository.getTotalMinutesBySubject(subjectId).map { it ?: 0 }

    fun addSubject(name: String, colorHex: String) {
        viewModelScope.launch {
            subjectRepository.insertSubject(Subject(name = name, colorHex = colorHex))
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            subjectRepository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectRepository.deleteSubject(subject)
        }
    }
}
