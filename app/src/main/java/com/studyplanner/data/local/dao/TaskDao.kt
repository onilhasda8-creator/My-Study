package com.studyplanner.data.local.dao

import androidx.room.*
import com.studyplanner.data.model.Task
import com.studyplanner.data.model.TaskWithSubject
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Transaction
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, createdAt DESC")
    fun getAllTasksWithSubject(): Flow<List<TaskWithSubject>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId ORDER BY dueDate ASC")
    fun getTasksBySubject(subjectId: Long): Flow<List<TaskWithSubject>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY dueDate ASC")
    fun getPendingTasks(): Flow<List<TaskWithSubject>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE completed = 0
          AND dueDate >= :startOfDay
          AND dueDate < :endOfDay
        ORDER BY dueDate ASC
    """)
    fun getTasksDueToday(startOfDay: Long, endOfDay: Long): Flow<List<TaskWithSubject>>

    @Transaction
    @Query("""
        SELECT * FROM tasks
        WHERE completed = 0
          AND dueDate >= :from
          AND dueDate < :to
        ORDER BY dueDate ASC
    """)
    fun getUpcomingTasks(from: Long, to: Long): Flow<List<TaskWithSubject>>

    @Query("SELECT COUNT(*) FROM tasks WHERE completed = 1")
    fun getCompletedTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTaskCount(): Flow<Int>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<Task?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE tasks SET completed = :completed WHERE id = :taskId")
    suspend fun setTaskCompleted(taskId: Long, completed: Boolean)
}
