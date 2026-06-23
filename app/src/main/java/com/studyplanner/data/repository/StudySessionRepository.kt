package com.studyplanner.data.repository

import com.studyplanner.data.local.dao.StudySessionDao
import com.studyplanner.data.model.StudySession
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudySessionRepository @Inject constructor(
    private val sessionDao: StudySessionDao
) {
    val allSessions: Flow<List<StudySession>> = sessionDao.getAllSessions()

    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsBySubject(subjectId)

    fun getTotalMinutesBySubject(subjectId: Long): Flow<Int?> =
        sessionDao.getTotalMinutesBySubject(subjectId)

    fun getWeeklyMinutes(): Flow<Int?> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val start = cal.timeInMillis
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        val end = cal.timeInMillis
        return sessionDao.getTotalMinutesInRange(start, end)
    }

    fun getMonthlyMinutes(): Flow<Int?> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        return sessionDao.getTotalMinutesInRange(start, end)
    }

    fun getSessionsInRange(startMs: Long, endMs: Long): Flow<List<StudySession>> =
        sessionDao.getSessionsInRange(startMs, endMs)

    suspend fun insertSession(session: StudySession): Long = sessionDao.insertSession(session)

    suspend fun deleteSession(session: StudySession) = sessionDao.deleteSession(session)
}
