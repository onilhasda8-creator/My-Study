package com.studyplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorHex: String = "#6750A4", // Material 3 primary default
    val createdAt: Long = System.currentTimeMillis()
)
