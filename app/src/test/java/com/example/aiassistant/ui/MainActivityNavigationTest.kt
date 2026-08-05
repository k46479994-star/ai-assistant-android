package com.example.aiassistant.ui

import android.view.View
import android.view.ViewGroup
import com.example.aiassistant.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
}
