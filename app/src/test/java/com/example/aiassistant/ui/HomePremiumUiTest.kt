package com.example.aiassistant.ui

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import com.example.aiassistant.data.NoteEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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
class HomePremiumUiTest {
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val context = ContextThemeWrapper(appContext, R.style.Theme_AiAssistant)

    @Test
    fun homeUsesPremiumScrollableCardsWithoutFakeOnlineData() {
        var quickInputClicked = false
        var settingsClicked = false
        val view = HomeViewFactory(context).create(
            openTaskCount = 2,
            latestNotes = listOf(
                NoteEntity(
                    id = 1,
                    title = "아이디어",
                    body = "발표 순서를 바꾸기",
                    createdAtEpochMillis = 1,
                    updatedAtEpochMillis = 1
                )
            ),
            onQuickInput = { quickInputClicked = true },
            onSettings = { settingsClicked = true }
        )

        assertTrue(view is ScrollView)
        assertEquals(PremiumColors.Background, view.solidColor)
        assertTrue(countViews<MaterialCardView>(view) >= 3)

        val quickInput = view.findViewById<MaterialButton>(R.id.home_quick_input)
        val settings = view.findViewById<MaterialButton>(R.id.home_settings)
        assertNotNull(quickInput)
        assertNotNull(settings)
        assertTrue(quickInput.text.toString().contains("빠른 입력"))
        assertTrue(quickInput.contentDescription.toString().contains("일정"))

        val text = viewText(view)
        assertTrue(text.contains("오늘 할 일"))
        assertTrue(text.contains("2개"))
        assertTrue(text.contains("최근 메모"))
        assertTrue(text.contains("아이디어"))
        assertFalse(text.contains("날씨"))
        assertFalse(text.contains("AI 추천"))
        assertFalse(text.contains("온라인"))

        quickInput.performClick()
        settings.performClick()
        assertTrue(quickInputClicked)
        assertTrue(settingsClicked)
    }

    private inline fun <reified T : View> countViews(view: View): Int {
        var count = if (view is T) 1 else 0
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                count += countViews<T>(view.getChildAt(index))
            }
        }
        return count
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
