package com.example.aiassistant.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import com.example.aiassistant.data.LearnedRuleEntity
import com.example.aiassistant.data.NoteEntity
import com.example.aiassistant.data.TaskEntity
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineScreensTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun homeWorksWithoutAiOrNetworkState() {
        val view = HomeViewFactory(context).create(
            openTaskCount = 2,
            latestNotes = listOf(noteEntity()),
            onQuickInput = {},
            onSettings = {}
        )

        assertNotNull(view.findViewById<View>(R.id.screen_home))
        assertTrue(viewText(view).contains("오늘 마감 할 일 2개"))
        assertTrue(viewText(view).contains("아이디어"))
        assertFalse(viewText(view).contains("온라인"))
    }

    @Test
    fun taskCompletionCallbackReceivesIdAndState() {
        var received: Pair<Long, Boolean>? = null
        val task = taskEntity()
        val view = TaskViewFactory(context).create(
            tasks = listOf(task),
            onToggle = { id, done -> received = id to done },
            onAdd = {}
        )

        firstCheckBox(view).performClick()

        assertEquals(task.id to true, received)
    }

    @Test
    fun noteScreenShowsNewestContentAndAddCallback() {
        var addClicked = false
        val longBody = "가".repeat(100)
        val view = NoteViewFactory(context).create(
            notes = listOf(noteEntity(body = longBody)),
            onAdd = { addClicked = true }
        )

        assertTrue(viewText(view).contains("가".repeat(80)))
        findButton(view, "추가").performClick()
        assertTrue(addClicked)
    }

    @Test
    fun calendarNewEventPrefillsQuickInput() {
        var prefill: String? = null
        val view = CalendarViewFactory(context).create { prefill = it }

        findButton(view, "새 일정").performClick()

        assertEquals("일정 추가: ", prefill)
        assertTrue(viewText(view).contains("캘린더 앱에서 최종 확인"))
    }

    @Test
    fun settingsSaveDefaultsAndDeleteRules() {
        var saved: Pair<Int, Int>? = null
        var deletedId: Long? = null
        val invalidRule = LearnedRuleEntity(
            id = 9,
            normalizedKeyword = "잘못된규칙",
            targetTypeName = "BROKEN",
            createdAtEpochMillis = 1,
            lastUsedAtEpochMillis = 1
        )
        val view = SettingsViewFactory(context).create(
            durationMinutes = 60,
            reminderMinutes = 30,
            rules = listOf(invalidRule),
            onSave = { duration, reminder -> saved = duration to reminder },
            onDeleteRule = { deletedId = it }
        )
        view.findViewById<EditText>(R.id.settings_duration).setText("90")
        view.findViewById<EditText>(R.id.settings_reminder).setText("15")

        view.findViewById<Button>(R.id.settings_save).performClick()
        findButton(view, "삭제").performClick()

        assertEquals(90 to 15, saved)
        assertEquals(9L, deletedId)
        assertTrue(viewText(view).contains("저장 전 확인: 항상 켜짐"))
        assertTrue(viewText(view).contains("AI 사용: 꺼짐 (오프라인 기본 모드)"))
        assertTrue(viewText(view).contains("잘못된 규칙"))
    }

    private fun taskEntity(): TaskEntity = TaskEntity(
        id = 7,
        title = "보고서 제출",
        originalText = "금요일까지 보고서 제출",
        dueDateEpochDay = LocalDate.of(2026, 8, 7).toEpochDay(),
        isCompleted = false,
        createdAtEpochMillis = 1,
        updatedAtEpochMillis = 1
    )

    private fun noteEntity(body: String = "본문"): NoteEntity = NoteEntity(
        id = 1,
        title = "아이디어",
        body = body,
        createdAtEpochMillis = 2,
        updatedAtEpochMillis = 2
    )

    private fun firstCheckBox(view: View): CheckBox {
        if (view is CheckBox) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                try {
                    return firstCheckBox(view.getChildAt(index))
                } catch (_: NoSuchElementException) {
                    Unit
                }
            }
        }
        throw NoSuchElementException("CheckBox not found")
    }

    private fun findButton(view: View, text: String): Button {
        if (view is Button && view.text.toString() == text) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                try {
                    return findButton(view.getChildAt(index), text)
                } catch (_: NoSuchElementException) {
                    Unit
                }
            }
        }
        throw NoSuchElementException("Button '$text' not found")
    }

    private fun viewText(view: View): String {
        val own = if (view is android.widget.TextView) view.text.toString() else ""
        if (view !is ViewGroup) return own
        return buildString {
            append(own)
            for (index in 0 until view.childCount) {
                append(' ')
                append(viewText(view.getChildAt(index)))
            }
        }
    }
}
