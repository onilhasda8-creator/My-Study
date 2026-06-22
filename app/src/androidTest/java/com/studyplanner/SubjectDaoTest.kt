package com.studyplanner

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studyplanner.data.local.database.StudyPlannerDatabase
import com.studyplanner.data.local.dao.SubjectDao
import com.studyplanner.data.model.Subject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubjectDaoTest {

    private lateinit var database: StudyPlannerDatabase
    private lateinit var subjectDao: SubjectDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudyPlannerDatabase::class.java
        ).allowMainThreadQueries().build()
        subjectDao = database.subjectDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndReadSubject() = runTest {
        val subject = Subject(name = "Mathematics", colorHex = "#4A45C0")
        subjectDao.insertSubject(subject)
        val subjects = subjectDao.getAllSubjects().first()
        assertEquals(1, subjects.size)
        assertEquals("Mathematics", subjects[0].name)
    }

    @Test
    fun deleteSubject_removesFromDb() = runTest {
        val id = subjectDao.insertSubject(Subject(name = "Physics", colorHex = "#006B5F"))
        val inserted = subjectDao.getAllSubjects().first().first()
        subjectDao.deleteSubject(inserted)
        val subjects = subjectDao.getAllSubjects().first()
        assertTrue(subjects.isEmpty())
    }

    @Test
    fun updateSubject_persists() = runTest {
        subjectDao.insertSubject(Subject(name = "Chemistry", colorHex = "#9B3620"))
        val subject = subjectDao.getAllSubjects().first().first()
        subjectDao.updateSubject(subject.copy(name = "Organic Chemistry"))
        val updated = subjectDao.getAllSubjects().first().first()
        assertEquals("Organic Chemistry", updated.name)
    }

    @Test
    fun multipleSubjects_orderedAlphabetically() = runTest {
        subjectDao.insertSubject(Subject(name = "Zoology",   colorHex = "#4A45C0"))
        subjectDao.insertSubject(Subject(name = "Biology",   colorHex = "#006B5F"))
        subjectDao.insertSubject(Subject(name = "Art",       colorHex = "#9B3620"))
        val subjects = subjectDao.getAllSubjects().first()
        assertEquals("Art", subjects[0].name)
        assertEquals("Biology", subjects[1].name)
        assertEquals("Zoology", subjects[2].name)
    }
}
