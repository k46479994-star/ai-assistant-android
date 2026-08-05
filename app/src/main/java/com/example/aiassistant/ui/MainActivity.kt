package com.example.aiassistant.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.aiassistant.R

class MainActivity : AppCompatActivity() {
    private lateinit var contentHost: FrameLayout
    private lateinit var settingsButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createRoot())
        navigate(AppScreen.HOME)
    }

    fun navigate(screen: AppScreen) {
        settingsButton.visibility = if (screen == AppScreen.HOME) {
            View.VISIBLE
        } else {
            View.GONE
        }

        val screenView = createScreen(screen)
        contentHost.removeAllViews()
        contentHost.addView(
            screenView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun createRoot(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(Color.rgb(247, 247, 252))

        addView(
            createTopBar(),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        contentHost = FrameLayout(this@MainActivity).apply {
            id = R.id.content_host
        }
        addView(
            contentHost,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        addView(
            createBottomNavigation(),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun createTopBar(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(24, 24, 16, 12)
        setBackgroundColor(Color.WHITE)

        addView(
            TextView(this@MainActivity).apply {
                text = getString(R.string.app_name)
                textSize = 22f
                setTextColor(Color.rgb(35, 31, 58))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        settingsButton = Button(this@MainActivity).apply {
            text = "설정"
            isAllCaps = false
            setOnClickListener { navigate(AppScreen.SETTINGS) }
        }
        addView(settingsButton)
    }

    private fun createBottomNavigation(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(6, 8, 6, 12)
        setBackgroundColor(Color.WHITE)

        val destinations = listOf(
            NavigationItem(R.id.nav_home, "홈", AppScreen.HOME),
            NavigationItem(R.id.nav_quick_input, "빠른 입력", AppScreen.QUICK_INPUT),
            NavigationItem(R.id.nav_calendar, "일정", AppScreen.CALENDAR),
            NavigationItem(R.id.nav_tasks, "할 일", AppScreen.TASKS),
            NavigationItem(R.id.nav_notes, "메모", AppScreen.NOTES)
        )

        destinations.forEach { item ->
            addView(
                Button(this@MainActivity).apply {
                    id = item.id
                    text = item.label
                    textSize = 11f
                    isAllCaps = false
                    setOnClickListener { navigate(item.screen) }
                },
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )
        }
    }

    private fun createScreen(screen: AppScreen): View {
        val (viewId, label) = when (screen) {
            AppScreen.HOME -> R.id.screen_home to "홈"
            AppScreen.QUICK_INPUT -> R.id.screen_quick_input to "빠른 입력"
            AppScreen.PREVIEW -> R.id.screen_preview to "미리보기"
            AppScreen.CALENDAR -> R.id.screen_calendar to "일정"
            AppScreen.TASKS -> R.id.screen_tasks to "할 일"
            AppScreen.NOTES -> R.id.screen_notes to "메모"
            AppScreen.SETTINGS -> R.id.screen_settings to "설정"
        }

        return TextView(this).apply {
            id = viewId
            text = label
            gravity = Gravity.CENTER
            textSize = 24f
            setTextColor(Color.rgb(35, 31, 58))
        }
    }

    private data class NavigationItem(
        val id: Int,
        val label: String,
        val screen: AppScreen
    )
}
