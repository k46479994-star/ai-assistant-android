package com.example.aiassistant.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aiassistant.AiAssistantApplication
import com.example.aiassistant.R
import com.example.aiassistant.classification.ClassificationResult
import com.example.aiassistant.classification.EventDraft
import com.example.aiassistant.classification.ItemDraft
import com.example.aiassistant.classification.NoteDraft
import com.example.aiassistant.classification.TaskDraft
import java.time.ZonedDateTime
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val container by lazy {
        (application as AiAssistantApplication).container
    }

    private lateinit var contentHost: FrameLayout
    private lateinit var settingsButton: Button
    private var activeResult: ClassificationResult? = null
    private var activePreview: PreviewView? = null

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
        if (screen != AppScreen.PREVIEW) {
            activePreview = null
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

    private fun createScreen(screen: AppScreen): View = when (screen) {
        AppScreen.HOME -> placeholder(R.id.screen_home, "홈")
        AppScreen.QUICK_INPUT -> QuickInputViewFactory(this).create(::classifyInput)
        AppScreen.PREVIEW -> createPreviewScreen()
        AppScreen.CALENDAR -> placeholder(
            R.id.screen_calendar,
            "일정\n빠른 입력에서 일정을 만든 뒤 캘린더 앱에서 확인합니다."
        )
        AppScreen.TASKS -> placeholder(R.id.screen_tasks, "할 일")
        AppScreen.NOTES -> placeholder(R.id.screen_notes, "메모")
        AppScreen.SETTINGS -> placeholder(
            R.id.screen_settings,
            "설정\n기본 일정 60분 · 기본 알림 30분 전\nAI 사용은 기본적으로 꺼져 있습니다."
        )
    }

    private fun classifyInput(text: String) {
        lifecycleScope.launch {
            try {
                activeResult = container.offlineInputProcessor.process(
                    text = text,
                    now = ZonedDateTime.now()
                )
                navigate(AppScreen.PREVIEW)
            } catch (exception: IllegalArgumentException) {
                Toast.makeText(
                    this@MainActivity,
                    exception.message ?: "입력 내용을 확인해 주세요",
                    Toast.LENGTH_LONG
                ).show()
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "분류하지 못했습니다. 입력 내용은 기기 밖으로 전송되지 않았습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun createPreviewScreen(): View {
        val result = activeResult ?: return placeholder(
            R.id.screen_preview,
            "분류할 내용이 없습니다. 빠른 입력에서 내용을 입력해 주세요."
        )

        return PreviewViewFactory(this).create(
            result = result,
            onCancel = {
                activeResult = null
                navigate(AppScreen.QUICK_INPUT)
            },
            onSave = ::saveDraft,
            onTypeChanged = {}
        ).also { activePreview = it }
    }

    private fun saveDraft(
        draft: ItemDraft,
        rememberSelection: RememberSelection?
    ) {
        val preview = activePreview ?: return
        preview.setSaving(true)

        lifecycleScope.launch {
            try {
                val now = System.currentTimeMillis()
                val destination = when (draft) {
                    is EventDraft -> {
                        val launched = container.calendarGateway.launch(
                            this@MainActivity,
                            draft
                        )
                        if (!launched) {
                            preview.setSaving(false)
                            preview.showError("사용 가능한 캘린더 앱이 없습니다")
                            return@launch
                        }
                        AppScreen.CALENDAR
                    }

                    is TaskDraft -> {
                        container.taskRepository.insert(
                            title = draft.title,
                            originalText = draft.originalText,
                            dueDate = draft.dueDate,
                            nowEpochMillis = now
                        )
                        AppScreen.TASKS
                    }

                    is NoteDraft -> {
                        container.noteRepository.insert(
                            title = draft.title,
                            body = draft.body,
                            nowEpochMillis = now
                        )
                        AppScreen.NOTES
                    }
                }

                if (rememberSelection != null) {
                    container.learnedRuleStore.upsert(
                        keyword = rememberSelection.normalizedKeyword,
                        targetType = rememberSelection.targetType,
                        nowEpochMillis = now
                    )
                }

                activeResult = null
                navigate(destination)
            } catch (_: Exception) {
                preview.setSaving(false)
                preview.showError(
                    "저장하지 못했습니다. 입력 내용은 유지됩니다. 다시 시도해 주세요."
                )
            }
        }
    }

    private fun placeholder(id: Int, label: String): View = TextView(this).apply {
        this.id = id
        text = label
        gravity = Gravity.CENTER
        textSize = 22f
        setTextColor(Color.rgb(35, 31, 58))
        setPadding(24, 24, 24, 24)
    }

    private data class NavigationItem(
        val id: Int,
        val label: String,
        val screen: AppScreen
    )
}
