package com.example.aiassistant.ui

import android.content.Context
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

class SettingsViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(
        durationMinutes: Int,
        reminderMinutes: Int,
        rules: List<LearnedRuleEntity>,
        onSave: (Int, Int) -> Unit,
        onDeleteRule: (Long) -> Unit
    ): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_settings
            orientation = LinearLayout.VERTICAL
            setPadding(
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(16),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(28)
            )
        }

        content.addView(TextView(themedContext).apply {
            text = "설정"
            textSize = 28f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        })
        content.addView(premiumBodyText(
            themedContext,
            "오프라인 분류와 일정 기본값을 관리합니다."
        ).apply { setPadding(0, themedContext.dp(6), 0, themedContext.dp(16)) })

        val duration = numberField(R.id.settings_duration, "기본 일정 길이(분)", durationMinutes)
        val reminder = numberField(R.id.settings_reminder, "기본 알림(분 전)", reminderMinutes)
        val error = TextView(themedContext).apply {
            id = R.id.settings_error
            visibility = View.GONE
            setTextColor(PremiumColors.Error)
            setPadding(0, themedContext.dp(8), 0, 0)
        }

        content.addView(settingsCard("기본 설정") {
            addLabeledField(this, duration)
            addLabeledField(this, reminder)
            addView(error)
            addView(premiumPrimaryButton(themedContext, "설정 저장").apply {
                id = R.id.settings_save
                contentDescription = "기본 일정과 알림 설정 저장"
                setOnClickListener {
                    val durationValue = duration.input.text.toString().toIntOrNull()
                    val reminderValue = reminder.input.text.toString().toIntOrNull()
                    if (durationValue == null || reminderValue == null) {
                        error.text = "시간 설정은 숫자로 입력해 주세요"
                        error.visibility = View.VISIBLE
                    } else {
                        error.visibility = View.GONE
                        onSave(
                            durationValue.coerceIn(MIN_EVENT_DURATION, MAX_EVENT_DURATION),
                            reminderValue.coerceIn(MIN_REMINDER, MAX_REMINDER)
                        )
                    }
                }
            }, fullWidthParams().apply { topMargin = themedContext.dp(14) })
        })

        content.addView(settingsCard("작동 방식") {
            addView(statusRow("저장 전 확인:", "항상 켜짐"))
            addView(statusRow("AI 사용:", "꺼짐 (오프라인 기본 모드)"), rowParams(top = 10))
        }, sectionParams())

        content.addView(settingsCard("기억한 분류 규칙") {
            val ruleList = LinearLayout(themedContext).apply {
                id = R.id.settings_rule_list
                orientation = LinearLayout.VERTICAL
            }
            if (rules.isEmpty()) {
                ruleList.addView(premiumBodyText(themedContext, "아직 기억한 규칙이 없습니다."))
            } else {
                rules.forEachIndexed { index, rule ->
                    ruleList.addView(LinearLayout(themedContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                        setPadding(
                            themedContext.dp(12),
                            themedContext.dp(10),
                            themedContext.dp(8),
                            themedContext.dp(10)
                        )
                        setBackgroundColor(PremiumColors.SurfaceMuted)
                        addView(TextView(themedContext).apply {
                            text = "${rule.normalizedKeyword} · ${typeLabel(rule.targetTypeName)}"
                            textSize = 14f
                            setTextColor(PremiumColors.TextPrimary)
                        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                        addView(premiumSecondaryButton(themedContext, "삭제").apply {
                            textSize = 13f
                            setOnClickListener { onDeleteRule(rule.id) }
                        })
                    }, rowParams(top = if (index == 0) 0 else 8))
                }
            }
            addView(ruleList)
        }, sectionParams())

        return object : ScrollView(themedContext) {
            override fun getSolidColor(): Int = PremiumColors.Background
        }.apply {
            setBackgroundColor(PremiumColors.Background)
            isFillViewport = true
            addView(content, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
    }

    private fun settingsCard(title: String, block: LinearLayout.() -> Unit) =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )
                addView(premiumSectionTitle(themedContext, title).apply {
                    setPadding(0, 0, 0, themedContext.dp(12))
                })
                block()
            })
        }

    private fun numberField(id: Int, label: String, value: Int): NumberField = NumberField(
        label = label,
        input = EditText(themedContext).apply {
            this.id = id
            setText(value.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
            setTextColor(PremiumColors.TextPrimary)
            setHintTextColor(PremiumColors.TextSecondary)
            setBackgroundColor(PremiumColors.SurfaceMuted)
            setPadding(
                themedContext.dp(14),
                themedContext.dp(12),
                themedContext.dp(14),
                themedContext.dp(12)
            )
        }
    )

    private fun addLabeledField(container: LinearLayout, field: NumberField) {
        container.addView(premiumBodyText(themedContext, field.label).apply {
            setPadding(0, themedContext.dp(6), 0, themedContext.dp(5))
        })
        container.addView(field.input, fullWidthParams())
    }

    private fun statusRow(label: String, value: String) = LinearLayout(themedContext).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(TextView(themedContext).apply {
            text = label
            textSize = 15f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        addView(premiumBodyText(themedContext, value))
    }

    private fun typeLabel(typeName: String): String = when (typeName) {
        "EVENT" -> "일정"
        "TASK" -> "할 일"
        "NOTE" -> "메모"
        else -> "잘못된 규칙"
    }

    private fun fullWidthParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun sectionParams() = fullWidthParams().apply {
        topMargin = themedContext.dp(14)
    }

    private fun rowParams(top: Int = 0) = fullWidthParams().apply {
        topMargin = themedContext.dp(top)
    }

    private data class NumberField(val label: String, val input: EditText)

    private companion object {
        const val MIN_EVENT_DURATION = 15
        const val MAX_EVENT_DURATION = 480
        const val MIN_REMINDER = 0
        const val MAX_REMINDER = 1440
    }
}
