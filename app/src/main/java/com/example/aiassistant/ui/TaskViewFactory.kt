package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.data.TaskEntity
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class TaskViewFactory(private val context: Context) {
    private val themedContext = ContextThemeWrapper(context, R.style.Theme_AiAssistant)

    fun create(tasks: List<TaskEntity>, onToggle: (Long, Boolean) -> Unit, onAdd: () -> Unit): View {
        val content = LinearLayout(themedContext).apply {
            id = R.id.screen_tasks
            orientation = LinearLayout.VERTICAL
            setPadding(themedContext.dp(20), themedContext.dp(16), themedContext.dp(20), themedContext.dp(28))
        }
        val heading = LinearLayout(themedContext).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        heading.addView(TextView(themedContext).apply {
            text = "할 일"; textSize = 28f; setTextColor(PremiumColors.TextPrimary); setTypeface(typeface, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        heading.addView(premiumPrimaryButton(themedContext, "추가").apply {
            id = R.id.task_add; contentDescription = "새 할 일 추가"; setOnClickListener { onAdd() }
        })
        content.addView(heading)
        content.addView(premiumBodyText(themedContext, "해야 할 일을 한눈에 정리하고 완료 상태를 관리하세요.").apply {
            setPadding(0, themedContext.dp(6), 0, themedContext.dp(14))
        })

        val filters = LinearLayout(themedContext).apply { orientation = LinearLayout.HORIZONTAL }
        val openFilter = premiumPrimaryButton(themedContext, "진행 중").apply { id = R.id.task_filter_open }
        val doneFilter = premiumSecondaryButton(themedContext, "완료").apply { id = R.id.task_filter_done }
        filters.addView(openFilter, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = themedContext.dp(6) })
        filters.addView(doneFilter, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = themedContext.dp(6) })
        content.addView(filters)

        val list = LinearLayout(themedContext).apply { id = R.id.task_list; orientation = LinearLayout.VERTICAL }
        content.addView(list, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = themedContext.dp(16) })

        fun render(completed: Boolean) {
            list.removeAllViews()
            openFilter.backgroundTintList = android.content.res.ColorStateList.valueOf(if (!completed) PremiumColors.Primary else PremiumColors.SurfaceMuted)
            openFilter.setTextColor(if (!completed) PremiumColors.OnPrimary else PremiumColors.Primary)
            doneFilter.backgroundTintList = android.content.res.ColorStateList.valueOf(if (completed) PremiumColors.Primary else PremiumColors.SurfaceMuted)
            doneFilter.setTextColor(if (completed) PremiumColors.OnPrimary else PremiumColors.Primary)
            val visible = tasks.filter { it.isCompleted == completed }
            if (visible.isEmpty()) {
                list.addView(premiumCard(themedContext).apply {
                    addView(LinearLayout(themedContext).apply {
                        orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                        setPadding(themedContext.dp(22), themedContext.dp(30), themedContext.dp(22), themedContext.dp(28))
                        addView(TextView(themedContext).apply { text = if (completed) "✓" else "☑"; textSize = 42f; gravity = Gravity.CENTER; setTextColor(PremiumColors.Primary) })
                        addView(premiumSectionTitle(themedContext, if (completed) "완료된 할 일이 없습니다." else "진행 중인 할 일이 없습니다.").apply {
                            gravity = Gravity.CENTER; setPadding(0, themedContext.dp(10), 0, themedContext.dp(6))
                        })
                        addView(premiumBodyText(themedContext, if (completed) "완료한 항목이 여기에 표시됩니다." else "새로운 할 일을 추가해 보세요!").apply { gravity = Gravity.CENTER })
                        if (!completed) addView(premiumPrimaryButton(themedContext, "+ 할 일 추가").apply { setOnClickListener { onAdd() } }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = themedContext.dp(18) })
                    })
                })
                return
            }
            visible.forEachIndexed { index, task ->
                list.addView(premiumCard(themedContext).apply {
                    addView(LinearLayout(themedContext).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(themedContext.dp(16), themedContext.dp(12), themedContext.dp(16), themedContext.dp(12))
                        addView(CheckBox(themedContext).apply {
                            text = task.title; textSize = 16f; isChecked = task.isCompleted; setTextColor(PremiumColors.TextPrimary)
                            buttonTintList = android.content.res.ColorStateList.valueOf(PremiumColors.Primary)
                            setOnCheckedChangeListener { _, checked -> onToggle(task.id, checked) }
                        })
                        task.dueDateEpochDay?.let { addView(premiumBodyText(themedContext, "마감 ${LocalDate.ofEpochDay(it)}").apply { setPadding(themedContext.dp(44), 0, 0, 0) }) }
                    })
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { if (index > 0) topMargin = themedContext.dp(10) })
            }
        }
        openFilter.setOnClickListener { render(false) }
        doneFilter.setOnClickListener { render(true) }
        render(false)
        return object : ScrollView(themedContext) { override fun getSolidColor() = PremiumColors.Background }.apply {
            setBackgroundColor(PremiumColors.Background); isFillViewport = true
            addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
    }
}