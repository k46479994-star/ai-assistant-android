package com.example.aiassistant.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
import com.google.android.material.button.MaterialButton
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val container by lazy {
        (application as AiAssistantApplication).container
    }

    private lateinit var contentHost: FrameLayout
    private lateinit var settingsButton: MaterialButton
    private val navigationButtons = linkedMapOf<AppScreen, MaterialButton>()
    private var activeResult: ClassificationResult? = null
    private var activePreview: PreviewView? = null
    private var currentScreen: AppScreen = AppScreen.HOME
    private var navigationVersion: Long = 0
    private var pendingQuickInputText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createRoot())
        navigate(AppScreen.HOME)
    }

    fun navigate(screen: AppScreen) {
        currentScreen = screen
        val version = ++navigationVersion
        settingsButton.visibility = if (screen == AppScreen.HOME) {
            View.VISIBLE
        } else {
            View.GONE
        }
        updateNavigationState(screen)
        if (screen != AppScreen.PREVIEW) {
            activePreview = null
        }

        when (screen) {
            AppScreen.HOME -> loadHome(version)
            AppScreen.QUICK_INPUT -> attach(createQuickInputScreen())
            AppScreen.PREVIEW -> attach(createPreviewScreen())
            AppScreen.CALENDAR -> attach(
                CalendarViewFactory(this).create { prefill ->
                    openQuickInput(prefill)
                }
            )
            AppScreen.TASKS -> loadTasks(version)
            AppScreen.NOTES -> loadNotes(version)
            AppScreen.SETTINGS -> loadSettings(version)
        }
    }

    private fun createRoot(): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(PremiumColors.Background)

        addView(
            createTopBar(),
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        contentHost = FrameLayout(this@MainActivity).apply {
            id = R.id.content_host
            setBackgroundColor(PremiumColors.Background)
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
        setPadding(dp(20), dp(18), dp(16), dp(12))
        setBackgroundColor(PremiumColors.Surface)

        addView(
            LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                addView(TextView(this@MainActivity).apply {
                    text = "오프라인 개인 비서"
                    textSize = 12f
                    setTextColor(PremiumColors.Primary)
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(this@MainActivity).apply {
                    text = getString(R.string.app_name)
                    textSize = 23f
                    setTextColor(PremiumColors.TextPrimary)
                    setTypeface(typeface, Typeface.BOLD)
                })
            },
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        settingsButton = premiumSecondaryButton(this@MainActivity, "⚙").apply {
            id = R.id.home_settings
            contentDescription = "설정 열기"
            textSize = 19f
            minWidth = dp(PremiumDimens.TouchTargetDp)
            setPadding(0, 0, 0, 0)
            setOnClickListener { navigate(AppScreen.SETTINGS) }
        }
        addView(
            settingsButton,
            LinearLayout.LayoutParams(
                dp(PremiumDimens.TouchTargetDp),
                dp(PremiumDimens.TouchTargetDp)
            )
        )
    }

    private fun createBottomNavigation(): View = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(dp(8), dp(8), dp(8), dp(12))
        setBackgroundColor(PremiumColors.Surface)
        elevation = dp(8).toFloat()

        val destinations = listOf(
            NavigationItem(R.id.nav_home, "⌂\n홈", "홈", AppScreen.HOME),
            NavigationItem(R.id.nav_quick_input, "＋\n입력", "빠른 입력", AppScreen.QUICK_INPUT),
            NavigationItem(R.id.nav_calendar, "▣\n일정", "일정", AppScreen.CALENDAR),
            NavigationItem(R.id.nav_tasks, "✓\n할 일", "할 일", AppScreen.TASKS),
            NavigationItem(R.id.nav_notes, "▤\n메모", "메모", AppScreen.NOTES)
        )

        destinations.forEach { item ->
            val button = MaterialButton(this@MainActivity).apply {
                id = item.id
                text = item.visualLabel
                contentDescription = item.accessibilityLabel
                textSize = 11f
                isAllCaps = false
                minHeight = dp(56)
                minWidth = 0
                cornerRadius = dp(18)
                insetTop = 0
                insetBottom = 0
                setPadding(dp(2), dp(4), dp(2), dp(4))
                setOnClickListener { navigate(item.screen) }
            }
            navigationButtons[item.screen] = button
            addView(
                button,
                LinearLayout.LayoutParams(
                    0,
                    dp(60),
                    1f
                ).apply {
                    marginStart = dp(2)
                    marginEnd = dp(2)
                }
            )
        }
    }

    private fun updateNavigationState(screen: AppScreen) {
        val selectedScreen = when (screen) {
            AppScreen.PREVIEW -> AppScreen.QUICK_INPUT
            AppScreen.SETTINGS -> null
            else -> screen
        }
        navigationButtons.forEach { (destination, button) ->
            val selected = destination == selectedScreen
            button.isSelected = selected
            button.setTextColor(
                if (selected) PremiumColors.Primary else PremiumColors.TextSecondary
            )
            button.backgroundTintList = ColorStateList.valueOf(
                if (selected) PremiumColors.SurfaceMuted else PremiumColors.Surface
            )
            button.setTypeface(button.typeface, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
    }

    private fun createQuickInputScreen(): View {
        val initialText = pendingQuickInputText
        pendingQuickInputText = ""
        return QuickInputViewFactory(this).create(
            initialText = initialText,
            onSubmit = ::classifyInput
        )
    }

    private fun openQuickInput(prefill: String = "") {
        pendingQuickInputText = prefill
        navigate(AppScreen.QUICK_INPUT)
    }

    private fun loadHome(version: Long) {
        attach(loadingView(R.id.screen_home, "홈을 불러오는 중입니다."))
        lifecycleScope.launch {
            try {
                val count = container.taskRepository.countOpenDueOn(LocalDate.now())
                val notes = container.noteRepository.listLatest(3)
                attachIfCurrent(
                    AppScreen.HOME,
                    version,
                    HomeViewFactory(this@MainActivity).create(
                        openTaskCount = count,
                        latestNotes = notes,
                        onQuickInput = { openQuickInput() },
                        onSettings = { navigate(AppScreen.SETTINGS) }
                    )
                )
            } catch (_: Exception) {
                attachIfCurrent(
                    AppScreen.HOME,
                    version,
                    loadingView(R.id.screen_home, "홈 정보를 불러오지 못했습니다.")
                )
            }
        }
    }

    private fun loadTasks(version: Long) {
        attach(loadingView(R.id.screen_tasks, "할 일을 불러오는 중입니다."))
        lifecycleScope.launch {
            try {
                val tasks = container.taskRepository.listAll()
                attachIfCurrent(
                    AppScreen.TASKS,
                    version,
                    TaskViewFactory(this@MainActivity).create(
                        tasks = tasks,
                        onToggle = ::toggleTask,
                        onAdd = { openQuickInput() }
                    )
                )
            } catch (_: Exception) {
                attachIfCurrent(
                    AppScreen.TASKS,
                    version,
                    loadingView(R.id.screen_tasks, "할 일을 불러오지 못했습니다.")
                )
            }
        }
    }

    private fun toggleTask(id: Long, completed: Boolean) {
        lifecycleScope.launch {
            try {
                container.taskRepository.setCompleted(
                    id = id,
                    completed = completed,
                    updatedAtEpochMillis = System.currentTimeMillis()
                )
                if (currentScreen == AppScreen.TASKS) navigate(AppScreen.TASKS)
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "할 일 상태를 변경하지 못했습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadNotes(version: Long) {
        attach(loadingView(R.id.screen_notes, "메모를 불러오는 중입니다."))
        lifecycleScope.launch {
            try {
                val notes = container.noteRepository.listLatest(100)
                attachIfCurrent(
                    AppScreen.NOTES,
                    version,
                    NoteViewFactory(this@MainActivity).create(
                        notes = notes,
                        onAdd = { openQuickInput() }
                    )
                )
            } catch (_: Exception) {
                attachIfCurrent(
                    AppScreen.NOTES,
                    version,
                    loadingView(R.id.screen_notes, "메모를 불러오지 못했습니다.")
                )
            }
        }
    }

    private fun loadSettings(version: Long) {
        attach(loadingView(R.id.screen_settings, "설정을 불러오는 중입니다."))
        lifecycleScope.launch {
            try {
                val rules = container.learnedRuleStore.listAll()
                attachIfCurrent(
                    AppScreen.SETTINGS,
                    version,
                    SettingsViewFactory(this@MainActivity).create(
                        durationMinutes = container.settingsStore
                            .defaultEventDurationMinutes(),
                        reminderMinutes = container.settingsStore
                            .defaultReminderMinutes(),
                        rules = rules,
                        onSave = { duration, reminder ->
                            container.settingsStore.saveDefaults(duration, reminder)
                            Toast.makeText(
                                this@MainActivity,
                                "설정을 저장했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            if (currentScreen == AppScreen.SETTINGS) {
                                navigate(AppScreen.SETTINGS)
                            }
                        },
                        onDeleteRule = ::deleteLearnedRule
                    )
                )
            } catch (_: Exception) {
                attachIfCurrent(
                    AppScreen.SETTINGS,
                    version,
                    loadingView(R.id.screen_settings, "설정을 불러오지 못했습니다.")
                )
            }
        }
    }

    private fun deleteLearnedRule(id: Long) {
        lifecycleScope.launch {
            try {
                container.learnedRuleStore.delete(id)
                if (currentScreen == AppScreen.SETTINGS) navigate(AppScreen.SETTINGS)
            } catch (_: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "규칙을 삭제하지 못했습니다.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
        val result = activeResult ?: return loadingView(
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
                if (draft is EventDraft) {
                    val launched = container.calendarGateway.launch(
                        this@MainActivity,
                        draft
                    )
                    if (!launched) {
                        preview.setSaving(false)
                        preview.showError("사용 가능한 캘린더 앱이 없습니다")
                        return@launch
                    }
                    if (rememberSelection != null) {
                        container.learnedRuleStore.upsert(
                            keyword = rememberSelection.normalizedKeyword,
                            targetType = rememberSelection.targetType,
                            nowEpochMillis = now
                        )
                    }
                    preview.setSaving(false)
                    return@launch
                }

                val destination = when (draft) {
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

                    is EventDraft -> error("Event draft handled above")
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

    private fun attachIfCurrent(
        screen: AppScreen,
        version: Long,
        view: View
    ) {
        if (currentScreen == screen && navigationVersion == version) {
            attach(view)
        }
    }

    private fun attach(view: View) {
        contentHost.removeAllViews()
        contentHost.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun loadingView(id: Int, label: String): View = TextView(this).apply {
        this.id = id
        text = label
        gravity = Gravity.CENTER
        textSize = 17f
        setTextColor(PremiumColors.TextSecondary)
        setPadding(dp(24), dp(24), dp(24), dp(24))
    }

    private data class NavigationItem(
        val id: Int,
        val visualLabel: String,
        val accessibilityLabel: String,
        val screen: AppScreen
    )
}
