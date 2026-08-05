package com.example.aiassistant.ui

import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import com.example.aiassistant.R
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
class MainActivityNavigationTest {
    @Test
    fun startsOnHomeAndQuickInputOccupiesFullHost() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        assertNotNull(activity.findViewById<View>(R.id.screen_home))

        activity.findViewById<View>(R.id.nav_quick_input).performClick()

        val quick = activity.findViewById<View>(R.id.screen_quick_input)
        assertNotNull(quick)
        assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            quick.layoutParams.height
        )
    }

    @Test
    fun premiumShellMovesSelectedNavigationStateAndExposesSettingsLabel() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val home = activity.findViewById<MaterialButton>(R.id.nav_home)
        val tasks = activity.findViewById<MaterialButton>(R.id.nav_tasks)

        assertEquals(PremiumColors.Primary, home.currentTextColor)
        assertTrue(home.isSelected)
        assertFalse(tasks.isSelected)
        assertEquals("홈", home.contentDescription.toString())

        tasks.performClick()

        assertFalse(home.isSelected)
        assertTrue(tasks.isSelected)
        assertEquals(PremiumColors.Primary, tasks.currentTextColor)
        assertEquals(
            ColorStateList.valueOf(PremiumColors.SurfaceMuted),
            tasks.backgroundTintList
        )

        val settings = activity.findViewById<View>(R.id.home_settings)
        assertEquals("설정 열기", settings.contentDescription.toString())
    }
}
