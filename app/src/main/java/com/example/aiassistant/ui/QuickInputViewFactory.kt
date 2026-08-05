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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class QuickInputViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(
        initialText: String = "",
        onSubmit: (String) -> Unit
    ): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_quick_input
            orientation = LinearLayout.VERTICAL
            setPadding(
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(18),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(28)
            )
        }

        content.addView(TextView(themedContext).apply {
            text = "빠른 입력"
            textSize = 28f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        })
        content.addView(premiumBodyText(
            themedContext,
            "AI나 인터넷 없이도 일정·할 일·메모를 자동으로 분류합니다."
        ).apply {
            setPadding(0, themedContext.dp(6), 0, themedContext.dp(18))
        })

        val input = EditText(themedContext).apply {
            id = R.id.quick_input_text
            hint = "예: 내일 오후 3시 병원, 30분 전에 알려줘"
            minLines = 5
            maxLines = 9
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(
                themedContext.dp(18),
                themedContext.dp(16),
                themedContext.dp(18),
                themedContext.dp(16)
            )
            setTextColor(PremiumColors.TextPrimary)
            setHintTextColor(PremiumColors.TextSecondary)
            setBackgroundColor(PremiumColors.Surface)
            setText(initialText)
            if (initialText.isNotEmpty()) setSelection(initialText.length)
        }

        val error = TextView(themedContext).apply {
            id = R.id.quick_input_error
            visibility = View.GONE
            setTextColor(PremiumColors.Error)
            setPadding(
                themedContext.dp(4),
                themedContext.dp(10),
                themedContext.dp(4),
                0
            )
        }

        content.addView(inputCard(input, error))
        content.addView(exampleCard(input, error), sectionParams(top = 14))

        val submit = premiumPrimaryButton(themedContext, "분류 결과 확인").apply {
            id = R.id.quick_input_submit
            contentDescription = "입력 내용을 일정, 할 일 또는 메모로 분류하기"
            setOnClickListener {
                val rawText = input.text.toString()
                val message = when {
                    rawText.isBlank() -> "내용을 입력해 주세요"
                    rawText.length > MAX_INPUT_LENGTH -> "입력은 500자까지 가능합니다"
                    else -> null
                }

                if (message != null) {
                    error.text = message
                    error.visibility = View.VISIBLE
                } else {
                    error.visibility = View.GONE
                    onSubmit(rawText.trim())
                }
            }
        }
        content.addView(submit, sectionParams(top = 16))

        return PremiumQuickInputScrollView(themedContext).apply {
            setBackgroundColor(PremiumColors.Background)
            isFillViewport = true
            clipToPadding = false
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun inputCard(input: EditText, error: TextView): MaterialCardView =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )
                addView(premiumSectionTitle(themedContext, "무엇을 기록할까요?"))
                addView(premiumBodyText(
                    themedContext,
                    "날짜, 시간, 마감, 알림 표현을 함께 적어도 됩니다."
                ).apply {
                    setPadding(0, themedContext.dp(5), 0, themedContext.dp(12))
                })
                addView(
                    input,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                )
                addView(error)
            })
        }

    private fun exampleCard(input: EditText, error: TextView): MaterialCardView =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                id = R.id.quick_input_examples
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )
                addView(premiumSectionTitle(themedContext, "입력 예시"))
                addView(premiumBodyText(
                    themedContext,
                    "예시를 누르면 입력창에 바로 채워집니다."
                ).apply {
                    setPadding(0, themedContext.dp(5), 0, themedContext.dp(10))
                })
                listOf(
                    "내일 오후 3시 병원",
                    "금요일까지 보고서 제출",
                    "프로젝트 아이디어: 발표 순서를 바꾸기"
                ).forEachIndexed { index, example ->
                    addView(
                        premiumSecondaryButton(themedContext, example).apply {
                            textSize = 13f
                            gravity = Gravity.START or Gravity.CENTER_VERTICAL
                            contentDescription = "예시 입력: $example"
                            setOnClickListener {
                                input.setText(example)
                                input.setSelection(example.length)
                                error.visibility = View.GONE
                            }
                        },
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            if (index > 0) topMargin = themedContext.dp(8)
                        }
                    )
                }
            })
        }

    private fun sectionParams(top: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = themedContext.dp(top) }

    private class PremiumQuickInputScrollView(context: Context) : ScrollView(context) {
        override fun getSolidColor(): Int = PremiumColors.Background
    }

    private companion object {
        const val MAX_INPUT_LENGTH = 500
    }
}
