package com.example.aiassistant.ui

import com.example.aiassistant.classification.DraftValidationResult
import com.example.aiassistant.classification.EventDraft
import com.example.aiassistant.classification.InputType
import com.example.aiassistant.classification.NoteDraft
import com.example.aiassistant.classification.RequiredField
import com.example.aiassistant.classification.TaskDraft
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreviewFormStateTest {
    private val today = LocalDate.of(2026, 8, 5)

    @Test
    fun eventWithoutTimeCannotBeSaved() {
        val state = PreviewFormState(
            type = InputType.EVENT,
            title = "회의",
            originalText = "금요일 오전 회의",
            dateText = "2026-08-07",
            timeText = "",
            reminderText = "30",
            today = today
        )

        val result = state.toDraft()

        assertTrue(result is DraftValidationResult.Invalid)
        assertTrue(
            (result as DraftValidationResult.Invalid)
                .fields.contains(RequiredField.EVENT_TIME)
        )
    }

    @Test
    fun taskAndNoteProduceTypedDrafts() {
        val task = PreviewFormState(
            type = InputType.TASK,
            title = "보고서 제출",
            originalText = "금요일까지 보고서 제출",
            dateText = "2026-08-07",
            today = today
        ).toDraft()
        val note = PreviewFormState(
            type = InputType.NOTE,
            title = "프로젝트 아이디어",
            originalText = "발표 순서를 바꾸기",
            today = today
        ).toDraft()

        assertTrue((task as DraftValidationResult.Valid).draft is TaskDraft)
        assertTrue((note as DraftValidationResult.Valid).draft is NoteDraft)
    }

    @Test
    fun eventDefaultsToOneHourAndClampsReminder() {
        val result = PreviewFormState(
            type = InputType.EVENT,
            title = "병원",
            originalText = "내일 오후 3시 병원",
            dateText = "2026-08-06",
            timeText = "15:00",
            reminderText = "9999",
            today = today
        ).toDraft() as DraftValidationResult.Valid
        val event = result.draft as EventDraft

        assertEquals(LocalTime.of(16, 0), event.endTime)
        assertEquals(1440, event.reminderMinutes)
    }
}
