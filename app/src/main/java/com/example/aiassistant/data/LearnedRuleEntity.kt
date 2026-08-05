package com.example.aiassistant.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "learned_rules",
    indices = [Index(value = ["normalizedKeyword"], unique = true)]
)
data class LearnedRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedKeyword: String,
    val targetTypeName: String,
    val createdAtEpochMillis: Long,
    val lastUsedAtEpochMillis: Long
)
