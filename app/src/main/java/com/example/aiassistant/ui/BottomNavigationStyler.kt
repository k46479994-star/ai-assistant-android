package com.example.aiassistant.ui

import android.app.Activity
import android.app.Application
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.example.aiassistant.R
import com.google.android.material.button.MaterialButton

object BottomNavigationStyle {
    const val UnselectedTextColor: Int = 0xFF4B5563.toInt()
    const val LabelTextSizeSp: Float = 12f
    const val ItemHeightDp: Int = 60
    const val InputButtonSizeDp: Int = 72
    const val BottomPaddingDp: Int = 24
    const val TopCornerRadiusDp: Int = 24
}

class BottomNavigationStyler : Application.ActivityLifecycleCallbacks {
    override fun onActivityResumed(activity: Activity) {
        style(activity)
    }

    internal fun style(activity: Activity) {
        val mainActivity = activity as? MainActivity ?: return
        val items = listOf(
            NavSpec(R.id.nav_home, "⌂\n홈", AppScreen.HOME),
            NavSpec(R.id.nav_quick_input, "＋\n입력", AppScreen.QUICK_INPUT),
            NavSpec(R.id.nav_calendar, "▣\n일정", AppScreen.CALENDAR),
            NavSpec(R.id.nav_tasks, "✓\n할 일", AppScreen.TASKS),
            NavSpec(R.id.nav_notes, "▤\n메모", AppScreen.NOTES)
        )
        val buttons = items.mapNotNull { spec ->
            mainActivity.findViewById<MaterialButton>(spec.id)?.let { spec to it }
        }
        if (buttons.size != items.size) return

        val parent = buttons.first().second.parent as? LinearLayout ?: return
        styleContainer(parent)

        buttons.forEach { (spec, button) ->
            if (spec.screen == AppScreen.QUICK_INPUT) {
                styleInputButton(mainActivity, button)
            } else {
                styleRegularButton(mainActivity, button)
            }
            button.text = spec.label
            button.gravity = Gravity.CENTER
            button.contentDescription = when (spec.screen) {
                AppScreen.HOME -> "홈"
                AppScreen.QUICK_INPUT -> "빠른 입력"
                AppScreen.CALENDAR -> "일정"
                AppScreen.TASKS -> "할 일"
                AppScreen.NOTES -> "메모"
                else -> spec.label
            }
            button.setOnClickListener {
                mainActivity.navigate(spec.screen)
                button.post { style(mainActivity) }
            }
        }
    }

    private fun styleContainer(parent: LinearLayout) {
        val context = parent.context
        val radius = context.dp(BottomNavigationStyle.TopCornerRadiusDp).toFloat()
        parent.setPadding(
            context.dp(10),
            context.dp(8),
            context.dp(10),
            context.dp(BottomNavigationStyle.BottomPaddingDp)
        )
        parent.minimumHeight = context.dp(96)
        parent.elevation = context.dp(14).toFloat()
        parent.clipToPadding = false
        parent.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(PremiumColors.Surface)
            cornerRadii = floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f)
            setStroke(context.dp(1), PremiumColors.Divider)
        }
    }

    private fun styleRegularButton(activity: MainActivity, button: MaterialButton) {
        val selectedState = intArrayOf(android.R.attr.state_selected)
        val defaultState = intArrayOf()
        button.textSize = BottomNavigationStyle.LabelTextSizeSp
        button.minWidth = 0
        button.minHeight = activity.dp(BottomNavigationStyle.ItemHeightDp)
        button.cornerRadius = activity.dp(18)
        button.insetTop = 0
        button.insetBottom = 0
        button.setPadding(activity.dp(3), activity.dp(5), activity.dp(3), activity.dp(5))
        button.setTextColor(
            ColorStateList(
                arrayOf(selectedState, defaultState),
                intArrayOf(PremiumColors.Primary, BottomNavigationStyle.UnselectedTextColor)
            )
        )
        button.backgroundTintList = ColorStateList(
            arrayOf(selectedState, defaultState),
            intArrayOf(PremiumColors.SurfaceMuted, PremiumColors.Surface)
        )
        button.setTypeface(button.typeface, if (button.isSelected) Typeface.BOLD else Typeface.NORMAL)
        button.layoutParams = LinearLayout.LayoutParams(
            0,
            activity.dp(BottomNavigationStyle.ItemHeightDp),
            1f
        ).apply {
            marginStart = activity.dp(2)
            marginEnd = activity.dp(2)
        }
    }

    private fun styleInputButton(activity: MainActivity, button: MaterialButton) {
        button.textSize = BottomNavigationStyle.LabelTextSizeSp
        button.minWidth = activity.dp(BottomNavigationStyle.InputButtonSizeDp)
        button.minHeight = activity.dp(BottomNavigationStyle.InputButtonSizeDp)
        button.cornerRadius = activity.dp(BottomNavigationStyle.InputButtonSizeDp / 2)
        button.insetTop = 0
        button.insetBottom = 0
        button.setPadding(activity.dp(4), activity.dp(5), activity.dp(4), activity.dp(5))
        button.setTextColor(PremiumColors.Surface)
        button.backgroundTintList = ColorStateList.valueOf(PremiumColors.Primary)
        button.setTypeface(button.typeface, Typeface.BOLD)
        button.elevation = activity.dp(8).toFloat()
        button.layoutParams = LinearLayout.LayoutParams(
            activity.dp(BottomNavigationStyle.InputButtonSizeDp),
            activity.dp(BottomNavigationStyle.InputButtonSizeDp)
        ).apply {
            marginStart = activity.dp(8)
            marginEnd = activity.dp(8)
        }
    }

    private data class NavSpec(
        val id: Int,
        val label: String,
        val screen: AppScreen
    )

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
