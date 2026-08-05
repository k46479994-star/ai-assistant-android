package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.aiassistant.R

class CalendarViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(onNewEvent: (String) -> Unit): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_calendar
            orientation = LinearLayout.VERTICAL
            setPadding(
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(16),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(28)
            )
        }

        content.addView(TextView(themedContext).apply {
            text = "일정"
            textSize = 28f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        })
        content.addView(premiumBodyText(
            themedContext,
            "저장 전 내용을 확인하고 설치된 캘린더 앱에서 최종 등록합니다."
        ).apply { setPadding(0, themedContext.dp(6), 0, themedContext.dp(16)) })

        content.addView(premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(
                    themedContext.dp(22),
                    themedContext.dp(30),
                    themedContext.dp(22),
                    themedContext.dp(28)
                )
                addView(TextView(themedContext).apply {
                    text = "📅"
                    textSize = 34f
                    gravity = Gravity.CENTER
                })
                addView(premiumSectionTitle(themedContext, "아직 일정이 없습니다").apply {
                    gravity = Gravity.CENTER
                    setPadding(0, themedContext.dp(10), 0, 0)
                })
                addView(premiumBodyText(
                    themedContext,
                    "새 일정을 추가하면 날짜와 시간을 한눈에 확인할 수 있어요."
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, themedContext.dp(7), 0, themedContext.dp(18))
                })
                addView(premiumPrimaryButton(themedContext, "＋ 새 일정 만들기").apply {
                    id = R.id.calendar_new_event
                    contentDescription = "새 일정 만들기"
                    setOnClickListener { onNewEvent("일정 추가: ") }
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ))
            })
        })

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
}
