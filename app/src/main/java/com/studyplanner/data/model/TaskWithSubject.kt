package com.studyplanner.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubject(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "subjectId",
        entityColumn = "id"
    )
    val subject: Subject?
)
