package com.example.aiassistant

import android.view.ViewGroup
import android.widget.LinearLayout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityLayoutTest {
    @Test
    fun aiTabIsAttachedWithFullHeight() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val decor = activity.findViewById<ViewGroup>(android.R.id.content)
        val root = decor.getChildAt(0) as LinearLayout
        val contentHost = root.getChildAt(0) as LinearLayout
        val bottomNavigation = root.getChildAt(1) as LinearLayout

        bottomNavigation.getChildAt(1).performClick()

        assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            contentHost.getChildAt(0).layoutParams.height
        )
    }
}
