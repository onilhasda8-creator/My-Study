package com.studyplanner

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.studyplanner.data.local.database.StudyPlannerDatabase
import com.studyplanner.data.local.dao.SubjectDao
import com.studyplanner.data.local.dao.TaskDao
import com.studyplanner.data.model.Subject
import com.studyplanner.data.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: StudyPlannerDatabase
    private lateinit var taskDao: TaskDao
    private lateinit var subjectDao: SubjectDao
    private var subjectId: Long = 0

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StudyPlannerDatabase::class.java
        ).allowMainThreadQueries().build()
        subjectDao = database.subjectDao()
        taskDao = database.taskDao()
        subjectId = subjectDao.insertSubject(Subject(name = "Math", colorHex = "#4A45C0"))
    }

    @After
    fun teardown() { database.close() }

    @Test
    fun insertAndReadTask() = runTest {
        taskDao.insertTask(Task(title = "Homework", subjectId = subjectId, dueDate = null))
        val tasks = taskDao.getAllTasksWithSubject().first()
        assertEquals(1, tasks.size)
        assertEquals("Homework", tasks[0].task.title)
        assertEquals("Math", tasks[0].subject?.name)
    }

    @Test
    fun toggleTaskCompleted() = runTest {
        val taskId = taskDao.insertTask(Task(title = "Essay", subjectId = subjectId, dueDate = null))
        taskDao.setTaskCompleted(taskId, true)
        val tasks = taskDao.getAllTasksWithSubject().first()
        assertTrue(tasks[0].task.completed)
    }

    @Test
    fun pendingTasksFilter() = runTest {
        taskDao.insertTask(Task(title = "Done", subjectId = subjectId, dueDate = null, completed = true))
        taskDao.insertTask(Task(title = "Pending", subjectId = subjectId, dueDate = null))
        val pending = taskDao.getPendingTasks().first()
        assertEquals(1, pending.size)
        assertEquals("Pending", pending[0].task.title)
    }

    @Test
    fun deleteTask_cascadesFromSubject() = runTest {
        taskDao.insertTask(Task(title = "Task A", subjectId = subjectId, dueDate = null))
        val subject = subjectDao.getAllSubjects().first().first()
        subjectDao.deleteSubject(subject) // cascade should remove tasks
        val tasks = taskDao.getAllTasksWithSubject().first()
        assertTrue(tasks.isEmpty())
    }
}
