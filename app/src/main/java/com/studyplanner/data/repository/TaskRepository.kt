package com.studyplanner.data.repository

import com.studyplanner.data.local.dao.TaskDao
import com.studyplanner.data.model.Task
import com.studyplanner.data.model.TaskWithSubject
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    val allTasksWithSubject: Flow<List<TaskWithSubject>> = taskDao.getAllTasksWithSubject()
    val pendingTasks: Flow<List<TaskWithSubject>> = taskDao.getPendingTasks()
    val completedTaskCount: Flow<Int> = taskDao.getCompletedTaskCount()
    val totalTaskCount: Flow<Int> = taskDao.getTotalTaskCount()

    fun getTasksBySubject(subjectId: Long): Flow<List<TaskWithSubject>> =
        taskDao.getTasksBySubject(subjectId)

    fun getTasksDueToday(): Flow<List<TaskWithSubject>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val end = cal.timeInMillis
        return taskDao.getTasksDueToday(start, end)
    }

    fun getUpcomingTasks(daysAhead: Int = 7): Flow<List<TaskWithSubject>> {
        val cal = Calendar.getInstance()
        val from = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, daysAhead)
        val to = cal.timeInMillis
        return taskDao.getUpcomingTasks(from, to)
    }

    fun getTaskById(id: Long): Flow<Task?> = taskDao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task)

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun setTaskCompleted(taskId: Long, completed: Boolean) =
        taskDao.setTaskCompleted(taskId, completed)
}
