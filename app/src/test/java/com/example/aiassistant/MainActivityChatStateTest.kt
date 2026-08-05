package com.example.aiassistant

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainActivityChatStateTest {
    @Test
    fun sentMessageSurvivesTabSwitch() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val decor = activity.findViewById<ViewGroup>(android.R.id.content)
        val root = decor.getChildAt(0) as LinearLayout
        val contentHost = root.getChildAt(0) as LinearLayout
        val bottomNavigation = root.getChildAt(1) as LinearLayout

        bottomNavigation.getChildAt(1).performClick()
        val chatRoot = contentHost.getChildAt(0) as LinearLayout
        val inputRow = chatRoot.getChildAt(chatRoot.childCount - 1) as LinearLayout
        val input = inputRow.getChildAt(0) as EditText
        val send = inputRow.getChildAt(1) as Button
        input.setText("기억할 메시지")
        send.performClick()

        bottomNavigation.getChildAt(0).performClick()
        bottomNavigation.getChildAt(1).performClick()

        assertTrue(containsExactText(contentHost.getChildAt(0), "기억할 메시지"))
    }

    private fun containsExactText(view: View, expected: String): Boolean {
        if (view is TextView && view.text.toString() == expected) return true
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                if (containsExactText(view.getChildAt(index), expected)) return true
            }
        }
        return false
    }
}
