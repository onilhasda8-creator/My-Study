package com.studyplanner

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studyplanner.data.local.database.StudyPlannerDatabase
import com.studyplanner.data.local.dao.StudySessionDao
import com.studyplanner.data.local.dao.SubjectDao
import com.studyplanner.data.model.StudySession
import com.studyplanner.data.model.Subject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudySessionDaoTest {

    private lateinit var database: StudyPlannerDatabase
    private lateinit var sessionDao: StudySessionDao
    private lateinit var subjectDao: SubjectDao
    private var subjectId: Long = 0

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudyPlannerDatabase::class.java
        ).allowMainThreadQueries().build()
        subjectDao = database.subjectDao()
        sessionDao = database.studySessionDao()
        subjectId = subjectDao.insertSubject(Subject(name = "Physics", colorHex = "#006B5F"))
    }

    @After
    fun teardown() { database.close() }

    @Test
    fun insertAndReadSession() = runTest {
        sessionDao.insertSession(StudySession(subjectId = subjectId, durationMinutes = 45))
        val sessions = sessionDao.getAllSessions().first()
        assertEquals(1, sessions.size)
        assertEquals(45, sessions[0].durationMinutes)
    }

    @Test
    fun totalMinutesBySubject_aggregatesCorrectly() = runTest {
        sessionDao.insertSession(StudySession(subjectId = subjectId, durationMinutes = 30))
        sessionDao.insertSession(StudySession(subjectId = subjectId, durationMinutes = 60))
        val total = sessionDao.getTotalMinutesBySubject(subjectId).first()
        assertEquals(90, total)
    }

    @Test
    fun sessionsInRange_filtersCorrectly() = runTest {
        val now = System.currentTimeMillis()
        sessionDao.insertSession(StudySession(subjectId = subjectId, durationMinutes = 20, date = now - 1000))
        sessionDao.insertSession(StudySession(subjectId = subjectId, durationMinutes = 25, date = now + 100_000))
        val range = sessionDao.getSessionsInRange(now - 5000, now + 50_000).first()
        assertEquals(1, range.size)
        assertEquals(20, range[0].durationMinutes)
    }
}
