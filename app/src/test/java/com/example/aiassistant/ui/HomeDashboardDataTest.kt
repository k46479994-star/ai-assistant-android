package com.example.aiassistant.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.data.NoteEntity
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeDashboardDataTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun homeShowsCalendarTaskAndRecentNoteSections() {
        val view = HomeViewFactory(context).create(
            openTaskCount = 3,
            latestNotes = listOf(
                NoteEntity(1, "첫 메모", "본문 1", 3, 3),
                NoteEntity(2, "둘째 메모", "본문 2", 2, 2),
                NoteEntity(3, "숨겨질 메모", "본문 3", 1, 1)
            ),
            onQuickInput = {},
            onSettings = {}
        )

        val text = collectText(view)
        assertTrue(text.contains("오늘 일정"))
        assertTrue(text.contains("캘린더에서 확인"))
        assertTrue(text.contains("오늘 마감 할 일 3개"))
        assertTrue(text.contains("첫 메모"))
        assertTrue(text.contains("둘째 메모"))
        assertTrue(!text.contains("숨겨질 메모"))
    }

    private fun collectText(view: View): String {
        val own = if (view is TextView) view.text.toString() else ""
        if (view !is ViewGroup) return own
        return buildString {
            append(own)
            repeat(view.childCount) { index ->
                append(' ')
                append(collectText(view.getChildAt(index)))
            }
        }
    }
}
