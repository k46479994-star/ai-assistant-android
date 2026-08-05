package com.example.aiassistant.ui

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.data.NoteEntity
import com.google.android.material.button.MaterialButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HomeInlineCaptureTest {
    @Test
    fun homeOffersCompactInlineCaptureAndCarriesTextToQuickInput() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val view = HomeViewFactory(activity).create(
            openTaskCount = 2,
            latestNotes = emptyList<NoteEntity>(),
            onQuickInput = { activity.navigate(AppScreen.QUICK_INPUT) },
            onSettings = {}
        )

        val input = findFirst(view, EditText::class.java)
        val submit = findButtons(view).firstOrNull {
            it.contentDescription?.toString()?.contains("바로 분류") == true
        }
        assertNotNull(input)
        assertNotNull(submit)

        requireNotNull(input).setText("내일 오후 3시 병원")
        requireNotNull(submit).performClick()

        val quickInput = activity.findViewById<EditText>(R.id.quick_input_text)
        assertEquals("내일 오후 3시 병원", quickInput.text.toString())

        val text = collectText(view)
        assertTrue(text.contains("오늘 할 일"))
        assertTrue(text.contains("2개"))
        assertFalse(text.contains("AI 추천"))
        assertFalse(text.contains("날씨"))
    }

    private fun <T : View> findFirst(view: View, type: Class<T>): T? {
        if (type.isInstance(view)) return type.cast(view)
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                val found = findFirst(view.getChildAt(index), type)
                if (found != null) return found
            }
        }
        return null
    }

    private fun findButtons(view: View): List<MaterialButton> {
        val result = mutableListOf<MaterialButton>()
        if (view is MaterialButton) result += view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) result += findButtons(view.getChildAt(index))
        }
        return result
    }

    private fun collectText(view: View): String {
        val own = if (view is TextView) view.text.toString() else ""
        if (view !is ViewGroup) return own
        return buildString {
            append(own)
            for (index in 0 until view.childCount) {
                append(' ')
                append(collectText(view.getChildAt(index)))
            }
        }
    }
}
