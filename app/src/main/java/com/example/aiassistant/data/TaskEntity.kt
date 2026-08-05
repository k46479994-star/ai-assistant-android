package com.example.aiassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val originalText: String,
    val dueDateEpochDay: Long?,
    val isCompleted: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)
