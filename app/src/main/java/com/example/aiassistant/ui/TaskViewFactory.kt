package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.data.TaskEntity
import java.time.LocalDate

class TaskViewFactory(private val context: Context) {
    fun create(
        tasks: List<TaskEntity>,
        onToggle: (Long, Boolean) -> Unit,
        onAdd: () -> Unit
    ): View {
        val root = LinearLayout(context).apply {
            id = R.id.screen_tasks
            orientation = LinearLayout.VERTICAL
            setPadding(24, 22, 24, 20)
        }

        val heading = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heading.addView(TextView(context).apply {
            text = "할 일"
            textSize = 27f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        heading.addView(Button(context).apply {
            id = R.id.task_add
            text = "추가"
            isAllCaps = false
            setOnClickListener { onAdd() }
        })
        root.addView(heading)

        val filters = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val openFilter = Button(context).apply {
            id = R.id.task_filter_open
            text = "진행 중"
            isAllCaps = false
        }
        val doneFilter = Button(context).apply {
            id = R.id.task_filter_done
            text = "완료"
            isAllCaps = false
        }
        filters.addView(openFilter, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        filters.addView(doneFilter, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(filters)

        val list = LinearLayout(context).apply {
            id = R.id.task_list
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

        fun render(completed: Boolean) {
            list.removeAllViews()
            val visibleTasks = tasks.filter { it.isCompleted == completed }
            if (visibleTasks.isEmpty()) {
                list.addView(TextView(context).apply {
                    text = if (completed) "완료된 할 일이 없습니다." else "진행 중인 할 일이 없습니다."
                    setPadding(8, 24, 8, 8)
                    setTextColor(Color.rgb(92, 88, 112))
                })
                return
            }

            visibleTasks.forEach { task ->
                val card = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(12, 8, 12, 8)
                    setBackgroundColor(Color.WHITE)
                }
                card.addView(CheckBox(context).apply {
                    text = task.title
                    textSize = 16f
                    isChecked = task.isCompleted
                    setOnCheckedChangeListener { _, checked ->
                        onToggle(task.id, checked)
                    }
                })
                task.dueDateEpochDay?.let { epochDay ->
                    card.addView(TextView(context).apply {
                        text = "마감 ${LocalDate.ofEpochDay(epochDay)}"
                        textSize = 13f
                        setTextColor(Color.rgb(92, 88, 112))
                        setPadding(48, 0, 0, 6)
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

        openFilter.setOnClickListener { render(false) }
        doneFilter.setOnClickListener { render(true) }
        render(false)
        return root
    }
}
