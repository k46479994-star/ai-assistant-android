package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.example.aiassistant.R
import com.example.aiassistant.classification.ClassificationResult
import com.example.aiassistant.classification.DraftValidationResult
import com.example.aiassistant.classification.InputType as ClassifiedInputType
import com.example.aiassistant.classification.ItemDraft
import com.example.aiassistant.classification.KeywordCandidateExtractor
import java.time.LocalDate

class PreviewViewFactory(private val context: Context) {
    private val typeOptions = listOf(
        TypeOption("일정", ClassifiedInputType.EVENT),
        TypeOption("할 일", ClassifiedInputType.TASK),
        TypeOption("메모", ClassifiedInputType.NOTE)
    )

    fun create(
        result: ClassificationResult,
        onCancel: () -> Unit,
        onSave: (ItemDraft, RememberSelection?) -> Unit,
        onTypeChanged: (ClassifiedInputType) -> Unit
    ): PreviewView {
        val root = PreviewView(context).apply {
            id = R.id.screen_preview
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }

        root.addView(TextView(context).apply {
            text = "저장 전 확인"
            textSize = 27f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(context).apply {
            text = "분류와 저장될 내용을 확인하거나 수정해 주세요."
            textSize = 14f
            setTextColor(Color.rgb(92, 88, 112))
            setPadding(0, 6, 0, 16)
        })

        val spinner = Spinner(context).apply {
            id = R.id.preview_type
            adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                typeOptions.map { it.label }
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
        addLabeledView(root, "분류", spinner)

        val titleInput = EditText(context).apply {
            id = R.id.preview_title
            setText(result.title)
            hint = "제목"
            setSingleLine(true)
        }
        addLabeledView(root, "제목", titleInput)

        val dateContainer = LinearLayout(context).apply {
            id = R.id.preview_task_fields
            orientation = LinearLayout.VERTICAL
        }
        val dateInput = EditText(context).apply {
            id = R.id.preview_date
            setText((result.taskDueDate ?: result.eventDate)?.toString().orEmpty())
            hint = "yyyy-MM-dd"
            setSingleLine(true)
        }
        addLabeledView(dateContainer, "날짜 또는 마감일", dateInput)
        root.addView(dateContainer)

        val eventFields = LinearLayout(context).apply {
            id = R.id.preview_event_fields
            orientation = LinearLayout.VERTICAL
        }
        val timeInput = EditText(context).apply {
            id = R.id.preview_time
            setText(result.eventStartTime?.toString().orEmpty())
            hint = "HH:mm"
            setSingleLine(true)
        }
        val endTimeInput = EditText(context).apply {
            id = R.id.preview_end_time
            setText(result.eventEndTime?.toString().orEmpty())
            hint = "비워두면 1시간 뒤"
            setSingleLine(true)
        }
        val reminderInput = EditText(context).apply {
            id = R.id.preview_reminder
            setText(result.reminderMinutes?.toString().orEmpty())
            hint = "분 전"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
        addLabeledView(eventFields, "시작 시간", timeInput)
        addLabeledView(eventFields, "종료 시간", endTimeInput)
        addLabeledView(eventFields, "알림(분 전)", reminderInput)
        root.addView(eventFields)

        val remember = CheckBox(context).apply {
            id = R.id.preview_remember
            text = "이 표현을 기억"
            visibility = View.GONE
        }
        root.addView(remember)

        val keywordContainer = LinearLayout(context).apply {
            id = R.id.preview_keyword_container
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            visibility = View.GONE
        }
        root.addView(keywordContainer)

        val error = TextView(context).apply {
            id = R.id.preview_error
            visibility = View.GONE
            setTextColor(Color.rgb(185, 28, 28))
            setPadding(4, 10, 4, 4)
        }
        root.addView(error)

        val actions = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        val cancel = Button(context).apply {
            id = R.id.preview_cancel
            text = "취소"
            isAllCaps = false
            setOnClickListener { onCancel() }
        }
        val save = Button(context).apply {
            id = R.id.preview_save
            text = "저장"
            isAllCaps = false
        }
        actions.addView(
            cancel,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        actions.addView(
            save,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        root.addView(actions)
        root.attachControls(save, error)

        val candidateExtractor = KeywordCandidateExtractor()
        val candidates = candidateExtractor
            .extract(result.title, emptyList())
            .ifEmpty { candidateExtractor.extract(result.originalText, emptyList()) }
            .take(MAX_KEYWORD_CANDIDATES)
        var selectedKeyword: String? = null
        val keywordButtons = mutableListOf<Button>()
        candidates.forEach { keyword ->
            val button = Button(context).apply {
                text = keyword
                textSize = 12f
                isAllCaps = false
                setOnClickListener {
                    selectedKeyword = keyword
                    keywordButtons.forEach { it.alpha = 0.55f }
                    alpha = 1f
                }
            }
            button.alpha = 0.55f
            keywordButtons += button
            keywordContainer.addView(button)
        }

        val initialType = result.suggestedType.takeIf {
            it in typeOptions.map(TypeOption::type)
        } ?: ClassifiedInputType.EVENT
        var currentType = initialType
        var latestValidation: DraftValidationResult? = null

        fun updateTypeSpecificViews() {
            val hasDate = currentType != ClassifiedInputType.NOTE
            dateContainer.visibility = if (hasDate) View.VISIBLE else View.GONE
            eventFields.visibility = if (currentType == ClassifiedInputType.EVENT) {
                View.VISIBLE
            } else {
                View.GONE
            }
            save.text = if (currentType == ClassifiedInputType.EVENT) {
                "캘린더에서 확인"
            } else {
                "저장"
            }

            val changed = currentType != result.suggestedType
            remember.visibility = if (changed) View.VISIBLE else View.GONE
            keywordContainer.visibility = if (changed && candidates.isNotEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if (!changed) {
                remember.isChecked = false
                selectedKeyword = null
                keywordButtons.forEach { it.alpha = 0.55f }
            }
        }

        fun refreshValidation() {
            latestValidation = PreviewFormState(
                type = currentType,
                title = titleInput.text.toString(),
                originalText = result.originalText,
                dateText = dateInput.text.toString(),
                timeText = timeInput.text.toString(),
                endTimeText = endTimeInput.text.toString(),
                reminderText = reminderInput.text.toString(),
                today = LocalDate.now()
            ).toDraft()

            when (val validation = latestValidation) {
                is DraftValidationResult.Valid -> root.setFormValidity(true, null)
                is DraftValidationResult.Invalid -> {
                    root.setFormValidity(false, validation.message)
                }
                null -> root.setFormValidity(false, "저장할 내용을 확인해 주세요")
            }
        }

        val initialPosition = typeOptions.indexOfFirst { it.type == initialType }
            .coerceAtLeast(0)
        spinner.setSelection(initialPosition, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentType = typeOptions[position].type
                selectedKeyword = null
                keywordButtons.forEach { it.alpha = 0.55f }
                updateTypeSpecificViews()
                refreshValidation()
                onTypeChanged(currentType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        listOf(titleInput, dateInput, timeInput, endTimeInput, reminderInput)
            .forEach { editText -> editText.afterTextChanged { refreshValidation() } }

        save.setOnClickListener {
            refreshValidation()
            val validation = latestValidation
            if (validation is DraftValidationResult.Valid) {
                root.setSaving(true)
                val remembered = if (
                    remember.isChecked &&
                    selectedKeyword != null &&
                    currentType != result.suggestedType
                ) {
                    RememberSelection(
                        normalizedKeyword = requireNotNull(selectedKeyword),
                        targetType = currentType
                    )
                } else {
                    null
                }
                onSave(validation.draft, remembered)
            }
        }

        updateTypeSpecificViews()
        refreshValidation()
        return root
    }

    private fun addLabeledView(
        container: LinearLayout,
        label: String,
        view: View
    ) {
        container.addView(TextView(context).apply {
            text = label
            textSize = 13f
            setTextColor(Color.rgb(92, 88, 112))
            setPadding(0, 8, 0, 3)
        })
        container.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun EditText.afterTextChanged(action: () -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit

            override fun afterTextChanged(editable: Editable?) {
                action()
            }
        })
    }

    private data class TypeOption(
        val label: String,
        val type: ClassifiedInputType
    )

    private companion object {
        const val MAX_KEYWORD_CANDIDATES = 5
    }
}
