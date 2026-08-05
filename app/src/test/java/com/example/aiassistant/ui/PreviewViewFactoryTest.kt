package com.example.aiassistant.ui

import android.content.Context
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import com.example.aiassistant.classification.ClassificationResult
import com.example.aiassistant.classification.Confidence
import com.example.aiassistant.classification.InputType
import com.example.aiassistant.classification.ItemDraft
import com.example.aiassistant.classification.RequiredField
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreviewViewFactoryTest {
    @Test
    fun missingEventTimeDisablesSaveAndTypeChangeShowsLearningControls() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var savedDraft: ItemDraft? = null
        var rememberSelection: RememberSelection? = null
        val result = ClassificationResult(
            originalText = "내일 병원",
            suggestedType = InputType.EVENT,
            confidence = Confidence.MEDIUM,
            title = "병원",
            eventDate = LocalDate.now().plusDays(1),
            eventStartTime = null,
            eventEndTime = null,
            taskDueDate = null,
            reminderMinutes = 30,
            missingFields = setOf(RequiredField.EVENT_TIME),
            matchedRules = emptyList()
        )
        val view = PreviewViewFactory(context).create(
            result = result,
            onCancel = {},
            onSave = { draft, remembered ->
                savedDraft = draft
                rememberSelection = remembered
            },
            onTypeChanged = {}
        )
        val save = view.findViewById<Button>(R.id.preview_save)
        val spinner = view.findViewById<Spinner>(R.id.preview_type)
        val remember = view.findViewById<CheckBox>(R.id.preview_remember)
        val keywords = view.findViewById<LinearLayout>(R.id.preview_keyword_container)

        assertFalse(save.isEnabled)

        spinner.setSelection(1)
        Shadows.shadowOf(Looper.getMainLooper()).idle()

        assertTrue(remember.visibility == View.VISIBLE)
        assertTrue(keywords.childCount > 0)
        remember.isChecked = true
        assertTrue(save.isEnabled)
        save.performClick()

        assertTrue(savedDraft != null)
        assertNull(rememberSelection)
    }
}
