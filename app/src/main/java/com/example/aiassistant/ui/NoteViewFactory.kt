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
import com.example.aiassistant.data.NoteEntity

class NoteViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(notes: List<NoteEntity>, onAdd: () -> Unit): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_notes
            orientation = LinearLayout.VERTICAL
            setPadding(
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(16),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(28)
            )
        }

        val heading = LinearLayout(themedContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heading.addView(TextView(themedContext).apply {
            text = "메모"
            textSize = 28f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        heading.addView(premiumSecondaryButton(themedContext, "＋ 추가").apply {
            id = R.id.note_add
            contentDescription = "새 메모 추가"
            setOnClickListener { onAdd() }
        })
        content.addView(heading)
        content.addView(premiumBodyText(
            themedContext,
            "아이디어와 중요한 내용을 기기에 안전하게 저장합니다."
        ).apply { setPadding(0, themedContext.dp(6), 0, themedContext.dp(16)) })

        val list = LinearLayout(themedContext).apply {
            id = R.id.note_list
            orientation = LinearLayout.VERTICAL
        }
        content.addView(list)

        val sorted = notes.sortedByDescending { it.createdAtEpochMillis }
        if (sorted.isEmpty()) {
            list.addView(premiumCard(themedContext).apply {
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
                        text = "📝"
                        textSize = 34f
                        gravity = Gravity.CENTER
                    })
                    addView(premiumSectionTitle(themedContext, "저장된 메모가 없습니다").apply {
                        gravity = Gravity.CENTER
                        setPadding(0, themedContext.dp(10), 0, 0)
                    })
                    addView(premiumBodyText(
                        themedContext,
                        "새 메모를 추가해 생각과 기록을 모아보세요."
                    ).apply {
                        gravity = Gravity.CENTER
                        setPadding(0, themedContext.dp(7), 0, themedContext.dp(18))
                    })
                    addView(premiumPrimaryButton(themedContext, "첫 메모 만들기").apply {
                        setOnClickListener { onAdd() }
                    }, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ))
                })
            })
        } else {
            sorted.forEachIndexed { index, note ->
                val preview = note.body.take(BODY_PREVIEW_LENGTH)
                list.addView(premiumCard(themedContext).apply {
                    addView(LinearLayout(themedContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(
                            themedContext.dp(18),
                            themedContext.dp(16),
                            themedContext.dp(18),
                            themedContext.dp(16)
                        )
                        addView(premiumSectionTitle(themedContext, note.title))
                        addView(premiumBodyText(
                            themedContext,
                            if (note.body.length > BODY_PREVIEW_LENGTH) "$preview…" else preview
                        ).apply { setPadding(0, themedContext.dp(6), 0, 0) })
                    })
                }, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { if (index > 0) topMargin = themedContext.dp(12) })
            }
        }

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

    private companion object {
        const val BODY_PREVIEW_LENGTH = 80
    }
}
