package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.data.LearnedRuleEntity

class SettingsViewFactory(private val context: Context) {
    fun create(
        durationMinutes: Int,
        reminderMinutes: Int,
        rules: List<LearnedRuleEntity>,
        onSave: (Int, Int) -> Unit,
        onDeleteRule: (Long) -> Unit
    ): View {
        val root = LinearLayout(context).apply {
            id = R.id.screen_settings
            orientation = LinearLayout.VERTICAL
            setPadding(26, 22, 26, 22)
        }

        root.addView(TextView(context).apply {
            text = "설정"
            textSize = 27f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        val duration = numberField(
            id = R.id.settings_duration,
            label = "기본 일정 길이(분)",
            value = durationMinutes
        )
        val reminder = numberField(
            id = R.id.settings_reminder,
            label = "기본 알림(분 전)",
            value = reminderMinutes
        )
        root.addView(duration.label)
        root.addView(duration.input)
        root.addView(reminder.label)
        root.addView(reminder.input)

        val error = TextView(context).apply {
            id = R.id.settings_error
            visibility = View.GONE
            setTextColor(Color.rgb(185, 28, 28))
            setPadding(0, 7, 0, 4)
        }
        root.addView(error)

        root.addView(Button(context).apply {
            id = R.id.settings_save
            text = "저장"
            isAllCaps = false
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
        }, fullWidthParams())

        root.addView(TextView(context).apply {
            text = "저장 전 확인: 항상 켜짐"
            textSize = 15f
            setTextColor(Color.rgb(35, 31, 58))
            setPadding(0, 16, 0, 6)
        })
        root.addView(TextView(context).apply {
            text = "AI 사용: 꺼짐 (오프라인 기본 모드)"
            textSize = 15f
            setTextColor(Color.rgb(35, 31, 58))
            setPadding(0, 0, 0, 16)
        })

        root.addView(TextView(context).apply {
            text = "기억한 분류 규칙"
            textSize = 18f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        })

        val ruleList = LinearLayout(context).apply {
            id = R.id.settings_rule_list
            orientation = LinearLayout.VERTICAL
        }
        if (rules.isEmpty()) {
            ruleList.addView(TextView(context).apply {
                text = "기억한 규칙이 없습니다."
                setTextColor(Color.rgb(92, 88, 112))
            })
        } else {
            rules.forEach { rule ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(10, 6, 4, 6)
                    setBackgroundColor(Color.WHITE)
                }
                row.addView(TextView(context).apply {
                    text = "${rule.normalizedKeyword} · ${typeLabel(rule.targetTypeName)}"
                    textSize = 14f
                    setTextColor(Color.rgb(35, 31, 58))
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                row.addView(Button(context).apply {
                    text = "삭제"
                    isAllCaps = false
                    setOnClickListener { onDeleteRule(rule.id) }
                })
                ruleList.addView(
                    row,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 6 }
                )
            }
        }
        root.addView(ruleList)

        return root
    }

    private fun numberField(id: Int, label: String, value: Int): NumberField {
        return NumberField(
            label = TextView(context).apply {
                text = label
                textSize = 13f
                setTextColor(Color.rgb(92, 88, 112))
                setPadding(0, 10, 0, 3)
            },
            input = EditText(context).apply {
                this.id = id
                setText(value.toString())
                inputType = InputType.TYPE_CLASS_NUMBER
                setSingleLine(true)
            }
        )
    }

    private fun typeLabel(typeName: String): String = when (typeName) {
        "EVENT" -> "일정"
        "TASK" -> "할 일"
        "NOTE" -> "메모"
        else -> "잘못된 규칙"
    }

    private fun fullWidthParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private data class NumberField(
        val label: TextView,
        val input: EditText
    )

    private companion object {
        const val MIN_EVENT_DURATION = 15
        const val MAX_EVENT_DURATION = 480
        const val MIN_REMINDER = 0
        const val MAX_REMINDER = 1440
    }
}
