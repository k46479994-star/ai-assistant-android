package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.data.NoteEntity

class HomeViewFactory(private val context: Context) {
    fun create(
        openTaskCount: Int,
        latestNotes: List<NoteEntity>,
        onQuickInput: () -> Unit,
        onSettings: () -> Unit
    ): View {
        val root = LinearLayout(context).apply {
            id = R.id.screen_home
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }

        root.addView(title("좋은 하루예요"))
        root.addView(subtitle("AI나 인터넷 연결 없이도 오늘의 일을 정리할 수 있습니다."))
        root.addView(summaryCard("오늘 마감 할 일 ${openTaskCount}개"))

        root.addView(TextView(context).apply {
            text = "최근 메모"
            textSize = 18f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, 18, 0, 8)
        })

        if (latestNotes.isEmpty()) {
            root.addView(summaryCard("아직 저장된 메모가 없습니다."))
        } else {
            latestNotes.take(3).forEach { note ->
                root.addView(summaryCard("${note.title}\n${note.body.take(80)}"))
            }
        }

        root.addView(Button(context).apply {
            id = R.id.home_quick_input
            text = "빠른 입력"
            isAllCaps = false
            textSize = 17f
            setOnClickListener { onQuickInput() }
        }, fullWidthParams(topMargin = 20))

        root.addView(Button(context).apply {
            id = R.id.home_settings
            text = "설정"
            isAllCaps = false
            setOnClickListener { onSettings() }
        }, fullWidthParams(topMargin = 8))

        return root
    }

    private fun title(text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 28f
        setTextColor(Color.rgb(35, 31, 58))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun subtitle(text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 14f
        setTextColor(Color.rgb(92, 88, 112))
        setPadding(0, 7, 0, 16)
    }

    private fun summaryCard(text: String): TextView = TextView(context).apply {
        this.text = text
        textSize = 16f
        gravity = Gravity.START
        setTextColor(Color.rgb(35, 31, 58))
        setPadding(22, 18, 22, 18)
        setBackgroundColor(Color.WHITE)
        layoutParams = fullWidthParams(bottomMargin = 10)
    }

    private fun fullWidthParams(
        topMargin: Int = 0,
        bottomMargin: Int = 0
    ): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        this.topMargin = topMargin
        this.bottomMargin = bottomMargin
    }
}
