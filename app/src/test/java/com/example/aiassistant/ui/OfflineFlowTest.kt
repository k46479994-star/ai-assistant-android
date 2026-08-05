package com.example.aiassistant.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.AiAssistantApplication
import com.example.aiassistant.AppContainer
import com.example.aiassistant.R
import com.example.aiassistant.calendar.CalendarGateway
import com.example.aiassistant.classification.EventDraft
import com.example.aiassistant.classification.InputType
import com.example.aiassistant.classification.RuleBasedClassifier
import com.example.aiassistant.data.AppDatabase
import java.time.ZonedDateTime
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OfflineFlowTest {
    private lateinit var application: AiAssistantApplication
    private lateinit var database: AppDatabase
    private lateinit var calendarGateway: RecordingCalendarGateway
    private lateinit var controller: ActivityController<MainActivity>
    private lateinit var activity: MainActivity

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            application as Context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        calendarGateway = RecordingCalendarGateway(launchResult = true)
        application.container = AppContainer(
            context = application,
            databaseOverride = database,
            calendarGatewayOverride = calendarGateway
        )
        controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        activity = controller.get()
        settle()
    }

    @After
    fun tearDown() {
        controller.destroy()
        database.close()
    }

    @Test
    fun taskPreviewSavesOnceAndAppearsInTaskList() {
        openPreview("금요일까지 보고서 제출")
        assertEquals("할 일", typeSpinner().selectedItem.toString())

        val save = activity.findViewById<Button>(R.id.preview_save)
        save.performClick()
        save.performClick()
        settle()

        val tasks = runBlocking { database.taskDao().listAll() }
        assertEquals(1, tasks.size)
        assertEquals("보고서 제출", tasks.single().title)
        activity.findViewById<View>(R.id.nav_tasks).performClick()
        settle()
        assertTrue(viewText(activity.findViewById(R.id.content_host)).contains("보고서 제출"))
    }

    @Test
    fun notePreviewSavesBodyAndAppearsInNotes() {
        val original = "프로젝트 아이디어: 발표 순서를 바꾸기"
        openPreview(original)
        assertEquals("메모", typeSpinner().selectedItem.toString())

        activity.findViewById<Button>(R.id.preview_save).performClick()
        settle()

        val notes = runBlocking { database.noteDao().listLatest(10) }
        assertEquals(1, notes.size)
        assertEquals(original, notes.single().body)
        activity.findViewById<View>(R.id.nav_notes).performClick()
        settle()
        assertTrue(viewText(activity.findViewById(R.id.content_host)).contains("발표 순서를 바꾸기"))
    }

    @Test
    fun ambiguousInputRequiresTypeThenCanRememberExactKeyword() {
        openPreview("병원 알아보기")
        val spinner = typeSpinner()
        val save = activity.findViewById<Button>(R.id.preview_save)

        assertEquals("유형 선택", spinner.selectedItem.toString())
        assertFalse(save.isEnabled)

        spinner.setSelection(positionOf(spinner, "할 일"))
        settle()
        val remember = activity.findViewById<CheckBox>(R.id.preview_remember)
        val keywords = activity.findViewById<LinearLayout>(R.id.preview_keyword_container)
        remember.isChecked = true
        firstButtonWithText(keywords, "병원").performClick()
        save.performClick()
        settle()

        val rules = runBlocking { application.container.learnedRuleStore.getValidRules() }
        assertTrue(rules.any { it.normalizedKeyword == "병원" && it.targetType == InputType.TASK })
        val classified = RuleBasedClassifier().classify(
            "병원 예약",
            ZonedDateTime.now(),
            rules
        )
        assertEquals(InputType.TASK, classified.suggestedType)
    }

    @Test
    fun eventLaunchKeepsPreviewAndCreatesNoRoomRows() {
        openPreview("내일 오후 3시 병원")
        activity.findViewById<Button>(R.id.preview_save).performClick()
        settle()

        assertEquals(1, calendarGateway.launchedEvents.size)
        assertTrue(runBlocking { database.taskDao().listAll() }.isEmpty())
        assertTrue(runBlocking { database.noteDao().listLatest(10) }.isEmpty())
        assertNotNull(activity.findViewById<View>(R.id.screen_preview))

        controller.pause().resume()
        settle()
        assertNotNull(activity.findViewById<View>(R.id.screen_preview))
    }

    @Test
    fun missingCalendarAppKeepsPreviewAndReenablesSave() {
        controller.destroy()
        calendarGateway = RecordingCalendarGateway(launchResult = false)
        application.container = AppContainer(
            context = application,
            databaseOverride = database,
            calendarGatewayOverride = calendarGateway
        )
        controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        activity = controller.get()
        settle()

        openPreview("내일 오후 3시 병원")
        val save = activity.findViewById<Button>(R.id.preview_save)
        save.performClick()
        settle()

        assertNotNull(activity.findViewById<View>(R.id.screen_preview))
        assertTrue(save.isEnabled)
        assertEquals(
            "사용 가능한 캘린더 앱이 없습니다",
            activity.findViewById<TextView>(R.id.preview_error).text.toString()
        )
    }

    private fun openPreview(text: String) {
        activity.findViewById<View>(R.id.nav_quick_input).performClick()
        activity.findViewById<EditText>(R.id.quick_input_text).setText(text)
        activity.findViewById<Button>(R.id.quick_input_submit).performClick()
        settle()
        assertNotNull(activity.findViewById<View>(R.id.screen_preview))
    }

    private fun typeSpinner(): Spinner = activity.findViewById(R.id.preview_type)

    private fun positionOf(spinner: Spinner, label: String): Int {
        for (index in 0 until spinner.adapter.count) {
            if (spinner.adapter.getItem(index).toString() == label) return index
        }
        error("Spinner item not found: $label")
    }

    private fun firstButtonWithText(root: ViewGroup, text: String): Button {
        for (index in 0 until root.childCount) {
            val child = root.getChildAt(index)
            if (child is Button && child.text.toString() == text) return child
            if (child is ViewGroup) {
                try {
                    return firstButtonWithText(child, text)
                } catch (_: NoSuchElementException) {
                    Unit
                }
            }
        }
        throw NoSuchElementException("Button not found: $text")
    }

    private fun settle() {
        repeat(5) {
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            Robolectric.flushBackgroundThreadScheduler()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun viewText(view: View): String {
        val own = if (view is TextView) view.text.toString() else ""
        if (view !is ViewGroup) return own
        return buildString {
            append(own)
            for (index in 0 until view.childCount) {
                append(' ')
                append(viewText(view.getChildAt(index)))
            }
        }
    }

    private class RecordingCalendarGateway(
        private val launchResult: Boolean
    ) : CalendarGateway() {
        val launchedEvents = mutableListOf<EventDraft>()

        override fun launch(activity: Activity, event: EventDraft): Boolean {
            if (launchResult) launchedEvents += event
            return launchResult
        }

        override fun buildInsertIntent(
            event: EventDraft,
            zoneId: java.time.ZoneId
        ): Intent = super.buildInsertIntent(event, zoneId)
    }
}
