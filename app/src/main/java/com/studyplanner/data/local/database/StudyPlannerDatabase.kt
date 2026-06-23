package com.studyplanner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.studyplanner.data.local.dao.StudySessionDao
import com.studyplanner.data.local.dao.SubjectDao
import com.studyplanner.data.local.dao.TaskDao
import com.studyplanner.data.model.StudySession
import com.studyplanner.data.model.Subject
import com.studyplanner.data.model.Task

@Database(
    entities = [Subject::class, Task::class, StudySession::class],
    version = 1,
    exportSchema = true
)
abstract class StudyPlannerDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun taskDao(): TaskDao
    abstract fun studySessionDao(): StudySessionDao
}
