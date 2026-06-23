package com.studyplanner.data.local.dao

import androidx.room.*
import com.studyplanner.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getSessionsBySubject(subjectId: Long): Flow<List<StudySession>>

    @Query("""
        SELECT * FROM study_sessions
        WHERE date >= :startMs AND date < :endMs
        ORDER BY date ASC
    """)
    fun getSessionsInRange(startMs: Long, endMs: Long): Flow<List<StudySession>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE subjectId = :subjectId")
    fun getTotalMinutesBySubject(subjectId: Long): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE date >= :startMs AND date < :endMs")
    fun getTotalMinutesInRange(startMs: Long, endMs: Long): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long

    @Delete
    suspend fun deleteSession(session: StudySession)
}
