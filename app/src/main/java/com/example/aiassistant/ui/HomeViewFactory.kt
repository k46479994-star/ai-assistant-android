package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
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
                themedContext.dp(18),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(28)
            )
        }

        content.addView(greeting())
        content.addView(quickInputCard(onQuickInput), sectionParams(top = 18))
        content.addView(todaySummaryCard(openTaskCount), sectionParams(top = 14))
        content.addView(recentNotesCard(latestNotes), sectionParams(top = 14))
        content.addView(settingsButton(onSettings), sectionParams(top = 14))

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
            textSize = 28f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        })
        addView(premiumBodyText(
            themedContext,
            "AI나 인터넷 연결 없이도 오늘의 일을 안전하게 정리할 수 있어요."
        ).apply {
            setPadding(0, themedContext.dp(6), 0, 0)
        })
    }

    private fun quickInputCard(onQuickInput: () -> Unit): MaterialCardView =
        premiumCard(themedContext).apply {
            setCardBackgroundColor(PremiumColors.Primary)
            contentDescription = "일정, 할 일, 메모 빠른 입력"
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(22),
                    themedContext.dp(22),
                    themedContext.dp(22),
                    themedContext.dp(20)
                )
                addView(TextView(themedContext).apply {
                    text = "✨ 무엇을 정리할까요?"
                    textSize = 21f
                    setTextColor(PremiumColors.Surface)
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(themedContext).apply {
                    text = "말하듯 입력하면 일정·할 일·메모로 자동 분류해요."
                    textSize = 14f
                    setTextColor(0xFFEDE9FF.toInt())
                    setPadding(0, themedContext.dp(7), 0, themedContext.dp(16))
                })
                addView(MaterialButton(themedContext).apply {
                    id = R.id.home_quick_input
                    text = "빠른 입력 시작"
                    contentDescription = "일정, 할 일, 메모 빠른 입력 시작"
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
                    setOnClickListener { onQuickInput() }
                }, fullWidthParams())
            })
        }

    private fun todaySummaryCard(openTaskCount: Int): MaterialCardView =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(20),
                    themedContext.dp(20),
                    themedContext.dp(20),
                    themedContext.dp(20)
                )
                addView(premiumSectionTitle(themedContext, "오늘 할 일"))
                addView(TextView(themedContext).apply {
                    text = "${openTaskCount}개"
                    textSize = 32f
                    setTextColor(PremiumColors.Primary)
                    setTypeface(typeface, Typeface.BOLD)
                    setPadding(0, themedContext.dp(8), 0, themedContext.dp(4))
                })
                addView(premiumBodyText(
                    themedContext,
                    if (openTaskCount == 0) {
                        "오늘 마감인 할 일이 없습니다."
                    } else {
                        "오늘 마감인 진행 중 할 일을 먼저 확인해 보세요."
                    }
                ))
            })
        }

    private fun recentNotesCard(latestNotes: List<NoteEntity>): MaterialCardView =
        premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(20),
                    themedContext.dp(20),
                    themedContext.dp(20),
                    themedContext.dp(20)
                )
                addView(premiumSectionTitle(themedContext, "최근 메모"))
                if (latestNotes.isEmpty()) {
                    addView(premiumBodyText(
                        themedContext,
                        "아직 저장된 메모가 없습니다. 빠른 입력으로 첫 메모를 남겨보세요."
                    ).apply { setPadding(0, themedContext.dp(10), 0, 0) })
                } else {
                    latestNotes.take(3).forEachIndexed { index, note ->
                        if (index > 0) {
                            addView(View(themedContext).apply {
                                setBackgroundColor(PremiumColors.Divider)
                            }, LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                themedContext.dp(1)
                            ).apply {
                                topMargin = themedContext.dp(12)
                                bottomMargin = themedContext.dp(12)
                            })
                        }
                        addView(TextView(themedContext).apply {
                            text = note.title
                            textSize = 16f
                            setTextColor(PremiumColors.TextPrimary)
                            setTypeface(typeface, Typeface.BOLD)
                        })
                        addView(premiumBodyText(
                            themedContext,
                            note.body.take(80)
                        ).apply { setPadding(0, themedContext.dp(4), 0, 0) })
                    }
                }
            })
        }

    private fun settingsButton(onSettings: () -> Unit): MaterialButton =
        premiumSecondaryButton(themedContext, "분류 및 기본값 설정").apply {
            id = R.id.home_settings
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
