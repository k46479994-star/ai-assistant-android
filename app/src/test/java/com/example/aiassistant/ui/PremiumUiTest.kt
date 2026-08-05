package com.example.aiassistant.ui

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PremiumUiTest {
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val context = ContextThemeWrapper(appContext, R.style.Theme_AiAssistant)

    @Test
    fun premiumDesignSystemUsesApprovedTokensAndComponents() {
        val designSystemExists = runCatching {
            Class.forName("com.example.aiassistant.ui.PremiumColors")
            Class.forName("com.example.aiassistant.ui.PremiumDimens")
            Class.forName("com.example.aiassistant.ui.PremiumUiKt")
        }.isSuccess

        assertTrue("Premium design-system classes must exist", designSystemExists)

        assertEquals(0xFF7C5CFF.toInt(), PremiumColors.Primary)
        assertEquals(0xFFF8F8FD.toInt(), PremiumColors.Background)

        val card = premiumCard(context)
        assertTrue(card is MaterialCardView)
        assertTrue(card.radius >= context.dp(24).toFloat())

        val button = premiumPrimaryButton(context, "저장")
        assertTrue(button is MaterialButton)
        assertFalse(button.isAllCaps)
        assertEquals("저장", button.text.toString())
    }
}
