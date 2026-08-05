package com.example.aiassistant.ui

import com.example.aiassistant.classification.DraftValidationResult
import com.example.aiassistant.classification.EventDraft
import com.example.aiassistant.classification.InputType
import com.example.aiassistant.classification.NoteDraft
import com.example.aiassistant.classification.RequiredField
import com.example.aiassistant.classification.TaskDraft
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime

data class PreviewFormState(
    val type: InputType,
    val title: String,
    val originalText: String,
    val dateText: String = "",
    val timeText: String = "",
    val endTimeText: String = "",
    val reminderText: String = "",
    val today: LocalDate = LocalDate.now()
) {
    fun toDraft(): DraftValidationResult {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return DraftValidationResult.Invalid(
                fields = setOf(RequiredField.TITLE),
                message = "제목을 입력해 주세요"
            )
        }

        return when (type) {
            InputType.EVENT -> eventDraft(trimmedTitle)
            InputType.TASK -> taskDraft(trimmedTitle)
            InputType.NOTE -> DraftValidationResult.Valid(
                NoteDraft(trimmedTitle, originalText.trim())
            )
            InputType.AMBIGUOUS -> DraftValidationResult.Invalid(
                fields = setOf(RequiredField.TYPE),
                message = "저장할 유형을 선택해 주세요"
            )
        }
    }

    private fun eventDraft(title: String): DraftValidationResult {
        val invalidFields = linkedSetOf<RequiredField>()
        val date = parseDate(dateText).also {
            if (it == null || it.isBefore(today)) {
                invalidFields += RequiredField.EVENT_DATE
            }
        }
        val startTime = parseTime(timeText).also {
            if (it == null) invalidFields += RequiredField.EVENT_TIME
        }
        val endTime = when {
            startTime == null -> null
            endTimeText.isBlank() -> startTime.plusHours(1)
            else -> parseTime(endTimeText).also {
                if (it == null || !it.isAfter(startTime)) {
                    invalidFields += RequiredField.EVENT_TIME
                }
            }
        }

        if (invalidFields.isNotEmpty() || date == null || startTime == null || endTime == null) {
            return DraftValidationResult.Invalid(
                fields = invalidFields,
                message = "일정의 날짜와 시간을 확인해 주세요"
            )
        }

        val reminder = reminderText.trim().toIntOrNull() ?: DEFAULT_REMINDER_MINUTES
        return DraftValidationResult.Valid(
            EventDraft(
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                reminderMinutes = reminder.coerceIn(0, MAX_REMINDER_MINUTES)
            )
        )
    }

    private fun taskDraft(title: String): DraftValidationResult {
        val dueDate = if (dateText.isBlank()) null else parseDate(dateText)
        if (dateText.isNotBlank() && dueDate == null) {
            return DraftValidationResult.Invalid(
                fields = emptySet(),
                message = "마감 날짜는 yyyy-MM-dd 형식으로 입력해 주세요"
            )
        }
        return DraftValidationResult.Valid(
            TaskDraft(
                title = title,
                originalText = originalText,
                dueDate = dueDate
            )
        )
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value.trim())
    } catch (_: DateTimeException) {
        null
    }

    private fun parseTime(value: String): LocalTime? = try {
        LocalTime.parse(value.trim())
    } catch (_: DateTimeException) {
        null
    }

    private companion object {
        const val DEFAULT_REMINDER_MINUTES = 30
        const val MAX_REMINDER_MINUTES = 1440
    }
}
