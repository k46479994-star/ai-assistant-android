package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime

sealed interface ItemDraft

data class EventDraft(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val reminderMinutes: Int
) : ItemDraft

data class TaskDraft(
    val title: String,
    val originalText: String,
    val dueDate: LocalDate?
) : ItemDraft

data class NoteDraft(
    val title: String,
    val body: String
) : ItemDraft

sealed interface DraftValidationResult {
    data class Valid(val draft: ItemDraft) : DraftValidationResult

    data class Invalid(
        val fields: Set<RequiredField>,
        val message: String
    ) : DraftValidationResult
}
