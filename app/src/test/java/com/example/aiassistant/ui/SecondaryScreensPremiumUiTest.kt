package com.example.aiassistant.ui

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SecondaryScreensPremiumUiTest {
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val context = ContextThemeWrapper(appContext, R.style.Theme_AiAssistant)

    @Test
    fun calendarUsesPremiumEmptyStateAndPrimaryAction() {
        val view = CalendarViewFactory(context).create {}
        assertEquals(PremiumColors.Background, view.solidColor)
        assertTrue(countViews(view, MaterialCardView::class.java) >= 1)
        assertNotNull(view.findViewById<MaterialButton>(R.id.calendar_new_event))
    }

    @Test
    fun notesUsePremiumEmptyStateAndAddAction() {
        val view = NoteViewFactory(context).create(emptyList()) {}
        assertEquals(PremiumColors.Background, view.solidColor)
        assertTrue(countViews(view, MaterialCardView::class.java) >= 1)
        assertNotNull(view.findViewById<MaterialButton>(R.id.note_add))
    }

    @Test
    fun settingsUsePremiumCardsAndSaveAction() {
        val view = SettingsViewFactory(context).create(
            durationMinutes = 60,
            reminderMinutes = 30,
            rules = emptyList(),
            onSave = { _, _ -> },
            onDeleteRule = {}
        )
        assertEquals(PremiumColors.Background, view.solidColor)
        assertTrue(countViews(view, MaterialCardView::class.java) >= 3)
        assertNotNull(view.findViewById<MaterialButton>(R.id.settings_save))
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
