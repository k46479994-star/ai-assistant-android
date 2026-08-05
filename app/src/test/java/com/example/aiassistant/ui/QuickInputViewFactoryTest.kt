package com.example.aiassistant.ui

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class QuickInputViewFactoryTest {
    @Test
    fun blankAndOversizedInputStayOnDeviceAndDoNotSubmit() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val submitted = mutableListOf<String>()
        val view = QuickInputViewFactory(context).create { submitted += it }
        val input = view.findViewById<EditText>(R.id.quick_input_text)
        val submit = view.findViewById<Button>(R.id.quick_input_submit)
        val error = view.findViewById<TextView>(R.id.quick_input_error)

        submit.performClick()
        assertEquals("내용을 입력해 주세요", error.text.toString())
        assertTrue(submitted.isEmpty())

        input.setText("가".repeat(501))
        submit.performClick()
        assertEquals("입력은 500자까지 가능합니다", error.text.toString())
        assertTrue(submitted.isEmpty())
    }
}
