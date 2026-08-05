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

class NoteViewFactory(private val context: Context) {
    fun create(
        notes: List<NoteEntity>,
        onAdd: () -> Unit
    ): View {
        val root = LinearLayout(context).apply {
            id = R.id.screen_notes
            orientation = LinearLayout.VERTICAL
            setPadding(24, 22, 24, 20)
        }

        val heading = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heading.addView(TextView(context).apply {
            text = "메모"
            textSize = 27f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        heading.addView(Button(context).apply {
            id = R.id.note_add
            text = "추가"
            isAllCaps = false
            setOnClickListener { onAdd() }
        })
        root.addView(heading)

        val list = LinearLayout(context).apply {
            id = R.id.note_list
            orientation = LinearLayout.VERTICAL
        }
        root.addView(
            list,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val sorted = notes.sortedByDescending { it.createdAtEpochMillis }
        if (sorted.isEmpty()) {
            list.addView(TextView(context).apply {
                text = "저장된 메모가 없습니다."
                setTextColor(Color.rgb(92, 88, 112))
                setPadding(8, 24, 8, 8)
            })
        } else {
            sorted.forEach { note ->
                val preview = note.body.take(BODY_PREVIEW_LENGTH)
                val card = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(20, 16, 20, 16)
                    setBackgroundColor(Color.WHITE)
                    addView(TextView(context).apply {
                        text = note.title
                        textSize = 17f
                        setTextColor(Color.rgb(35, 31, 58))
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                    })
                    addView(TextView(context).apply {
                        text = if (note.body.length > BODY_PREVIEW_LENGTH) "$preview…" else preview
                        textSize = 14f
                        setTextColor(Color.rgb(92, 88, 112))
                        setPadding(0, 7, 0, 0)
                    })
                }
                list.addView(
                    card,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 8 }
                )
            }
        }

        return root
    }

    private companion object {
        const val BODY_PREVIEW_LENGTH = 80
    }
}
