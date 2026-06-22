package com.studyplanner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyplanner.data.model.StudySession
import com.studyplanner.data.model.Subject
import com.studyplanner.data.repository.StudySessionRepository
import com.studyplanner.data.repository.SubjectRepository
import com.studyplanner.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Calendar
import javax.inject.Inject

data class SubjectStudyData(
    val subject: Subject,
    val totalMinutes: Int
)

data class DailyStudyData(
    val dayLabel: String,
    val minutes: Int
)

data class StatisticsUiState(
    val weeklyMinutes: Int = 0,
    val monthlyMinutes: Int = 0,
    val completedTaskCount: Int = 0,
    val totalTaskCount: Int = 0,
    val subjectStudyData: List<SubjectStudyData> = emptyList(),
    val weeklyDailyData: List<DailyStudyData> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val sessionRepository: StudySessionRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        sessionRepository.getWeeklyMinutes(),
        sessionRepository.getMonthlyMinutes(),
        taskRepository.completedTaskCount,
        taskRepository.totalTaskCount,
        subjectRepository.allSubjects
    ) { weeklyMins, monthlyMins, completedCount, totalCount, subjects ->
        StatisticsUiState(
            weeklyMinutes = weeklyMins ?: 0,
            monthlyMinutes = monthlyMins ?: 0,
            completedTaskCount = completedCount,
            totalTaskCount = totalCount,
            subjectStudyData = emptyList(), // populated below via secondary flow
            weeklyDailyData = buildWeeklyLabels()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatisticsUiState()
    )

    // Per-subject aggregation (requires coroutine join on all subject flows)
    val subjectStudyData: StateFlow<List<SubjectStudyData>> =
        subjectRepository.allSubjects.flatMapLatest { subjects ->
            if (subjects.isEmpty()) flowOf(emptyList())
            else combine(subjects.map { subject ->
                sessionRepository.getTotalMinutesBySubject(subject.id)
                    .map { mins -> SubjectStudyData(subject, mins ?: 0) }
            }) { it.toList().filter { d -> d.totalMinutes > 0 } }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Weekly sessions breakdown by day
    val weeklyDailyData: StateFlow<List<DailyStudyData>> =
        sessionRepository.getSessionsInRange(weekStart(), weekEnd())
            .map { sessions -> aggregateByDay(sessions) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = buildWeeklyLabels()
            )

    private fun weekStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal.timeInMillis
    }

    private fun weekEnd(): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = weekStart()
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return cal.timeInMillis
    }

    private fun buildWeeklyLabels(): List<DailyStudyData> {
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        return days.map { DailyStudyData(it, 0) }
    }

    private fun aggregateByDay(sessions: List<StudySession>): List<DailyStudyData> {
        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val minutesByDay = IntArray(7)
        val cal = Calendar.getInstance()
        sessions.forEach { session ->
            cal.timeInMillis = session.date
            val dow = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
            minutesByDay[dow] += session.durationMinutes
        }
        return days.mapIndexed { idx, label -> DailyStudyData(label, minutesByDay[idx]) }
    }
}
