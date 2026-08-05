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
import com.example.aiassistant.data.NoteEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HomeViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(
        openTaskCount: Int,
        latestNotes: List<NoteEntity>,
        onQuickInput: () -> Unit,
        onSettings: () -> Unit
    ): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_home
            orientation = LinearLayout.VERTICAL
            setPadding(
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(12),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(24)
            )
        }

        content.addView(greeting())
        content.addView(quickInputCard(onQuickInput), sectionParams(top = 14))
        content.addView(todaySummaryCard(openTaskCount), sectionParams(top = 12))
        content.addView(recentNotesCard(latestNotes), sectionParams(top = 12))
        content.addView(settingsButton(onSettings), sectionParams(top = 12))

        return PremiumHomeScrollView(themedContext).apply {
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

    private fun greeting(): View = LinearLayout(themedContext).apply {
        orientation = LinearLayout.VERTICAL
        addView(TextView(themedContext).apply {
            text = "좋은 하루예요 👋"
            textSize = 25f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        })
        addView(premiumBodyText(
            themedContext,
            "오늘 필요한 내용을 바로 정리해 보세요."
        ).apply {
            setPadding(0, themedContext.dp(4), 0, 0)
        })
    }

    private fun quickInputCard(onQuickInput: () -> Unit): MaterialCardView =
        premiumCard(themedContext).apply {
            setCardBackgroundColor(PremiumColors.Primary)
            contentDescription = "일정, 할 일, 메모 빠른 입력"
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )

                addView(TextView(themedContext).apply {
                    text = "✨ 무엇을 도와드릴까요?"
                    textSize = 19f
                    setTextColor(PremiumColors.Surface)
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(themedContext).apply {
                    text = "말하듯 입력하면 일정·할 일·메모로 분류해요."
                    textSize = 13f
                    setTextColor(0xFFEDE9FF.toInt())
                    setPadding(0, themedContext.dp(5), 0, themedContext.dp(12))
                })

                val input = EditText(themedContext).apply {
                    hint = "예: 내일 오후 3시 병원"
                    setSingleLine(true)
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                    setTextColor(PremiumColors.TextPrimary)
                    setHintTextColor(PremiumColors.TextSecondary)
                    setBackgroundColor(PremiumColors.Surface)
                    setPadding(
                        themedContext.dp(14),
                        themedContext.dp(12),
                        themedContext.dp(14),
                        themedContext.dp(12)
                    )
                }
                addView(input, fullWidthParams())

                addView(MaterialButton(themedContext).apply {
                    id = R.id.home_quick_input
                    text = "바로 분류"
                    contentDescription = "입력 내용을 바로 분류"
                    isAllCaps = false
                    minHeight = themedContext.dp(PremiumDimens.TouchTargetDp)
                    cornerRadius = themedContext.dp(18)
                    setTextColor(PremiumColors.Primary)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(
                        PremiumColors.Surface
                    )
                    setTypeface(typeface, Typeface.BOLD)
                    insetTop = 0
                    insetBottom = 0
                    setOnClickListener {
                        val text = input.text.toString().trim()
                        onQuickInput()
                        if (text.isNotEmpty()) {
                            (context as? MainActivity)
                                ?.findViewById<EditText>(R.id.quick_input_text)
                                ?.apply {
                                    setText(text)
                                    setSelection(text.length)
                                }
                        }
                    }
                }, fullWidthParams().apply { topMargin = themedContext.dp(10) })
            })
        }

    private fun todaySummaryCard(openTaskCount: Int): MaterialCardView =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )
                addView(LinearLayout(themedContext).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(premiumSectionTitle(themedContext, "오늘 할 일"))
                    addView(premiumBodyText(
                        themedContext,
                        if (openTaskCount == 0) {
                            "오늘 마감 할 일 0개 · 여유로운 하루예요."
                        } else {
                            "오늘 마감 할 일 ${openTaskCount}개 · 먼저 확인해 보세요."
                        }
                    ).apply { setPadding(0, themedContext.dp(4), 0, 0) })
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(TextView(themedContext).apply {
                    text = "${openTaskCount}개"
                    textSize = 28f
                    setTextColor(PremiumColors.Primary)
                    setTypeface(typeface, Typeface.BOLD)
                })
            })
        }

    private fun recentNotesCard(latestNotes: List<NoteEntity>): MaterialCardView =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )
                addView(premiumSectionTitle(themedContext, "최근 메모"))
                if (latestNotes.isEmpty()) {
                    addView(premiumBodyText(
                        themedContext,
                        "아직 저장된 메모가 없습니다."
                    ).apply { setPadding(0, themedContext.dp(8), 0, 0) })
                } else {
                    latestNotes.take(2).forEachIndexed { index, note ->
                        if (index > 0) {
                            addView(View(themedContext).apply {
                                setBackgroundColor(PremiumColors.Divider)
                            }, LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                themedContext.dp(1)
                            ).apply {
                                topMargin = themedContext.dp(10)
                                bottomMargin = themedContext.dp(10)
                            })
                        }
                        addView(TextView(themedContext).apply {
                            text = note.title
                            textSize = 15f
                            setTextColor(PremiumColors.TextPrimary)
                            setTypeface(typeface, Typeface.BOLD)
                        })
                        addView(premiumBodyText(
                            themedContext,
                            note.body.take(64)
                        ).apply { setPadding(0, themedContext.dp(3), 0, 0) })
                    }
                }
            })
        }

    private fun settingsButton(onSettings: () -> Unit): MaterialButton =
        premiumSecondaryButton(themedContext, "분류 및 기본값 설정").apply {
            contentDescription = "오프라인 분류 및 기본값 설정 열기"
            setOnClickListener { onSettings() }
        }

    private fun sectionParams(top: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = themedContext.dp(top) }

    private fun fullWidthParams(): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

    private class PremiumHomeScrollView(context: Context) : ScrollView(context) {
        override fun getSolidColor(): Int = PremiumColors.Background
    }
}
