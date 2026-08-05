package com.example.aiassistant.ui

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
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
class QuickInputViewFactoryTest {
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val context = ContextThemeWrapper(appContext, R.style.Theme_AiAssistant)

    @Test
    fun blankAndOversizedInputStayOnDeviceAndDoNotSubmit() {
        val submitted = mutableListOf<String>()
        val view = QuickInputViewFactory(context).create { submitted += it }
        val input = view.findViewById<EditText>(R.id.quick_input_text)
        val submit = view.findViewById<MaterialButton>(R.id.quick_input_submit)
        val error = view.findViewById<TextView>(R.id.quick_input_error)

        submit.performClick()
        assertEquals("내용을 입력해 주세요", error.text.toString())
        assertTrue(submitted.isEmpty())

        input.setText("가".repeat(501))
        submit.performClick()
        assertEquals("입력은 500자까지 가능합니다", error.text.toString())
        assertTrue(submitted.isEmpty())
    }

    @Test
    fun quickInputUsesPremiumScrollableCaptureCard() {
        var submitted: String? = null
        val view = QuickInputViewFactory(context).create(
            initialText = "내일 오후 3시 치과",
            onSubmit = { submitted = it }
        )

        assertTrue(view is ScrollView)
        assertTrue(countViews(view, MaterialCardView::class.java) >= 2)

        val input = view.findViewById<EditText>(R.id.quick_input_text)
        val voice = view.findViewById<MaterialButton>(R.id.quick_input_voice)
        val submit = view.findViewById<MaterialButton>(R.id.quick_input_submit)
        val examples = view.findViewById<View>(R.id.quick_input_examples)
        assertNotNull(input)
        assertNotNull(voice)
        assertNotNull(submit)
        assertNotNull(examples)
        assertEquals("내일 오후 3시 치과", input.text.toString())
        assertTrue(voice.text.toString().contains("음성"))
        assertTrue(voice.contentDescription.toString().contains("마이크"))
        assertEquals(PremiumColors.Primary, submit.backgroundTintList?.defaultColor)
        assertTrue(submit.contentDescription.toString().contains("분류"))

        submit.performClick()
        assertEquals("내일 오후 3시 치과", submitted)
        assertFalse(viewText(view).contains("온라인"))
        assertFalse(viewText(view).contains("AI 추천"))
    }

    private fun countViews(view: View, type: Class<out View>): Int {
        var count = if (type.isInstance(view)) 1 else 0
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) count += countViews(view.getChildAt(index), type)
        }
        return count
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
}
