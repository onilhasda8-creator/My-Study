package com.studyplanner.data.repository

import com.studyplanner.data.local.dao.SubjectDao
import com.studyplanner.data.model.Subject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepository @Inject constructor(
    private val subjectDao: SubjectDao
) {
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()

    fun getSubjectById(id: Long): Flow<Subject?> = subjectDao.getSubjectById(id)

    val subjectCount: Flow<Int> = subjectDao.getSubjectCount()

    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)

    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)

    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)
}
