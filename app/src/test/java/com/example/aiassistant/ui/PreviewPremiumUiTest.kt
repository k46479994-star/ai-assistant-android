package com.example.aiassistant.ui

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import com.example.aiassistant.classification.ClassificationResult
import com.example.aiassistant.classification.Confidence
import com.example.aiassistant.classification.InputType
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PreviewPremiumUiTest {
    @Test
    fun previewUsesPremiumCardsAndMaterialActions() {
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        val context = ContextThemeWrapper(appContext, R.style.Theme_AiAssistant)
        val result = ClassificationResult(
            originalText = "내일 병원",
            suggestedType = InputType.EVENT,
            confidence = Confidence.HIGH,
            title = "병원",
            eventDate = LocalDate.now().plusDays(1),
            eventStartTime = null,
            eventEndTime = null,
            taskDueDate = null,
            reminderMinutes = 30,
            missingFields = emptySet(),
            matchedRules = emptyList()
        )

        val view = PreviewViewFactory(context).create(
            result = result,
            onCancel = {},
            onSave = { _, _ -> },
            onTypeChanged = {}
        )

        assertEquals(PremiumColors.Background, view.solidColor)
        assertTrue(countViews(view, MaterialCardView::class.java) >= 2)
        assertNotNull(view.findViewById<MaterialButton>(R.id.preview_cancel))
        assertNotNull(view.findViewById<MaterialButton>(R.id.preview_save))
        assertTrue(view.findViewById<MaterialButton>(R.id.preview_save).contentDescription.toString().contains("저장"))
    }

    private fun countViews(view: View, targetClass: Class<out View>): Int {
        var count = if (targetClass.isInstance(view)) 1 else 0
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                count += countViews(view.getChildAt(index), targetClass)
            }
        }
        return count
    }
}
