package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.aiassistant.R

class CalendarViewFactory(private val context: Context) {
    fun create(onNewEvent: (String) -> Unit): View {
        return LinearLayout(context).apply {
            id = R.id.screen_calendar
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)

            addView(TextView(context).apply {
                text = "일정"
                textSize = 27f
                setTextColor(Color.rgb(35, 31, 58))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            addView(TextView(context).apply {
                text = "일정은 저장 전 이 앱에서 확인하고, 설치된 캘린더 앱에서 최종 확인합니다. 캘린더 권한은 필요하지 않습니다."
                textSize = 15f
                setTextColor(Color.rgb(92, 88, 112))
                setPadding(0, 9, 0, 18)
            })
            addView(Button(context).apply {
                id = R.id.calendar_new_event
                text = "새 일정"
                isAllCaps = false
                setOnClickListener { onNewEvent("일정 추가: ") }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
    }
}
