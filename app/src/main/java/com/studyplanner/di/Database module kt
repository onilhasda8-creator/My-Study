package com.studyplanner.di

import android.content.Context
import androidx.room.Room
import com.studyplanner.data.local.dao.StudySessionDao
import com.studyplanner.data.local.dao.SubjectDao
import com.studyplanner.data.local.dao.TaskDao
import com.studyplanner.data.local.database.StudyPlannerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyPlannerDatabase =
        Room.databaseBuilder(
            context,
            StudyPlannerDatabase::class.java,
            "study_planner.db"
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideSubjectDao(db: StudyPlannerDatabase): SubjectDao = db.subjectDao()

    @Provides
    fun provideTaskDao(db: StudyPlannerDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideStudySessionDao(db: StudyPlannerDatabase): StudySessionDao = db.studySessionDao()
}
