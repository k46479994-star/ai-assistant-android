package com.example.aiassistant.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.data.LearnedRuleEntity
import com.google.android.material.button.MaterialButton

class SettingsViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(
        durationMinutes: Int,
        reminderMinutes: Int,
        rules: List<LearnedRuleEntity>,
        selectedThemeColor: Int = PremiumColors.Primary,
        onSave: (Int, Int) -> Unit,
        onDeleteRule: (Long) -> Unit,
        onThemeSelected: (Int) -> Unit = {}
    ): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_settings
            orientation = LinearLayout.VERTICAL
            setPadding(themedContext.dp(20), themedContext.dp(16), themedContext.dp(20), themedContext.dp(28))
        }
        content.addView(title("설정", 28f))
        content.addView(premiumBodyText(themedContext, "앱 색상과 오프라인 기본값을 관리합니다.").apply {
            setPadding(0, themedContext.dp(6), 0, themedContext.dp(16))
        })

        content.addView(settingsCard("테마 색상") {
            addView(premiumBodyText(themedContext, "추천 색상을 누르거나 원하는 HEX 색상표 값을 직접 입력하세요."))
            val swatches = LinearLayout(themedContext).apply {
                id = R.id.settings_theme_swatches
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, themedContext.dp(12), 0, themedContext.dp(8))
            }
            val colors = listOf(
                0xFF7C5CFF.toInt(), 0xFF1E88E5.toInt(), 0xFF00A86B.toInt(),
                0xFF00A6A6.toInt(), 0xFFFF8F00.toInt(), 0xFFE83E8C.toInt(),
                0xFFEF4444.toInt(), 0xFF111827.toInt()
            )
            var pendingColor = selectedThemeColor
            colors.forEach { color ->
                swatches.addView(MaterialButton(themedContext).apply {
                    text = if (color == selectedThemeColor) "✓" else ""
                    contentDescription = "테마 색상 ${String.format("#%06X", color and 0xFFFFFF)}"
                    minWidth = themedContext.dp(42)
                    minHeight = themedContext.dp(42)
                    cornerRadius = themedContext.dp(21)
                    insetTop = 0
                    insetBottom = 0
                    backgroundTintList = ColorStateList.valueOf(color)
                    setTextColor(ThemePalette.from(color).onPrimary)
                    setOnClickListener { pendingColor = color }
                }, LinearLayout.LayoutParams(0, themedContext.dp(44), 1f).apply {
                    marginStart = themedContext.dp(2)
                    marginEnd = themedContext.dp(2)
                })
            }
            addView(swatches)
            val hexInput = EditText(themedContext).apply {
                id = R.id.settings_theme_hex
                hint = "예: #1E88E5"
                setText(String.format("#%06X", selectedThemeColor and 0xFFFFFF))
                setSingleLine(true)
                inputType = InputType.TYPE_CLASS_TEXT
                setTextColor(PremiumColors.TextPrimary)
                setBackgroundColor(PremiumColors.SurfaceMuted)
                setPadding(themedContext.dp(14), themedContext.dp(12), themedContext.dp(14), themedContext.dp(12))
            }
            addView(hexInput, fullWidthParams())
            val themeError = TextView(themedContext).apply {
                id = R.id.settings_theme_error
                visibility = View.GONE
                setTextColor(PremiumColors.Error)
            }
            addView(themeError)
            addView(premiumPrimaryButton(themedContext, "테마 적용").apply {
                id = R.id.settings_theme_apply
                contentDescription = "선택한 테마 색상 적용"
                setOnClickListener {
                    val typed = AppThemeStore.parseColor(hexInput.text.toString())
                    val color = typed ?: pendingColor
                    if (typed == null && hexInput.text.isNotBlank()) {
                        themeError.text = "HEX 색상은 #RRGGBB 형식으로 입력해 주세요"
                        themeError.visibility = View.VISIBLE
                    } else {
                        themeError.visibility = View.GONE
                        onThemeSelected(color)
                    }
                }
            }, fullWidthParams().apply { topMargin = themedContext.dp(12) })
        })

        val duration = numberField(R.id.settings_duration, "기본 일정 길이(분)", durationMinutes)
        val reminder = numberField(R.id.settings_reminder, "기본 알림(분 전)", reminderMinutes)
        val error = TextView(themedContext).apply {
            id = R.id.settings_error
            visibility = View.GONE
            setTextColor(PremiumColors.Error)
        }
        content.addView(settingsCard("기본 설정") {
            addLabeledField(this, duration)
            addLabeledField(this, reminder)
            addView(error)
            addView(premiumPrimaryButton(themedContext, "설정 저장").apply {
                id = R.id.settings_save
                setOnClickListener {
                    val d = duration.input.text.toString().toIntOrNull()
                    val r = reminder.input.text.toString().toIntOrNull()
                    if (d == null || r == null) {
                        error.text = "시간 설정은 숫자로 입력해 주세요"
                        error.visibility = View.VISIBLE
                    } else {
                        error.visibility = View.GONE
                        onSave(d.coerceIn(15, 480), r.coerceIn(0, 1440))
                    }
                }
            }, fullWidthParams().apply { topMargin = themedContext.dp(14) })
        }, sectionParams())

        content.addView(settingsCard("작동 방식") {
            addView(premiumBodyText(themedContext, "저장 전 확인: 항상 켜짐"))
            addView(premiumBodyText(themedContext, "AI 사용: 꺼짐 (오프라인 기본 모드)").apply {
                setPadding(0, themedContext.dp(8), 0, 0)
            })
        }, sectionParams())

        content.addView(settingsCard("기억한 분류 규칙") {
            val list = LinearLayout(themedContext).apply { id = R.id.settings_rule_list; orientation = LinearLayout.VERTICAL }
            if (rules.isEmpty()) list.addView(premiumBodyText(themedContext, "아직 기억한 규칙이 없습니다."))
            rules.forEach { rule ->
                list.addView(LinearLayout(themedContext).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    addView(TextView(themedContext).apply {
                        text = "${rule.normalizedKeyword} · ${typeLabel(rule.targetTypeName)}"
                        setTextColor(PremiumColors.TextPrimary)
                    }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    addView(premiumSecondaryButton(themedContext, "삭제").apply { setOnClickListener { onDeleteRule(rule.id) } })
                })
            }
            addView(list)
        }, sectionParams())

        return object : ScrollView(themedContext) { override fun getSolidColor() = PremiumColors.Background }.apply {
            setBackgroundColor(PremiumColors.Background)
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }

    private fun title(text: String, size: Float) = TextView(themedContext).apply {
        this.text = text; textSize = size; setTextColor(PremiumColors.TextPrimary); setTypeface(typeface, Typeface.BOLD)
    }
    private fun settingsCard(title: String, block: LinearLayout.() -> Unit) = premiumCard(themedContext).apply {
        addView(LinearLayout(themedContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(themedContext.dp(18), themedContext.dp(18), themedContext.dp(18), themedContext.dp(18))
            addView(premiumSectionTitle(themedContext, title).apply { setPadding(0, 0, 0, themedContext.dp(12)) })
            block()
        })
    }
    private fun numberField(id: Int, label: String, value: Int) = NumberField(label, EditText(themedContext).apply {
        this.id = id; setText(value.toString()); inputType = InputType.TYPE_CLASS_NUMBER; setSingleLine(true)
        setTextColor(PremiumColors.TextPrimary); setBackgroundColor(PremiumColors.SurfaceMuted)
        setPadding(themedContext.dp(14), themedContext.dp(12), themedContext.dp(14), themedContext.dp(12))
    })
    private fun addLabeledField(container: LinearLayout, field: NumberField) {
        container.addView(premiumBodyText(themedContext, field.label).apply { setPadding(0, themedContext.dp(6), 0, themedContext.dp(5)) })
        container.addView(field.input, fullWidthParams())
    }
    private fun typeLabel(type: String) = when (type) { "EVENT" -> "일정"; "TASK" -> "할 일"; "NOTE" -> "메모"; else -> "잘못된 규칙" }
    private fun fullWidthParams() = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    private fun sectionParams() = fullWidthParams().apply { topMargin = themedContext.dp(14) }
    private data class NumberField(val label: String, val input: EditText)
}