package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ContextThemeWrapper
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
import com.google.android.material.button.MaterialButton
import java.time.LocalDate

class PreviewViewFactory(private val context: Context) {
    private val themedContext: Context = ContextThemeWrapper(context, R.style.Theme_AiAssistant)
    private val concreteTypeOptions = listOf(
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
        val root = PreviewView(themedContext).apply {
            id = R.id.screen_preview
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(PremiumColors.Background)
            setPadding(
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(18),
                themedContext.dp(PremiumDimens.ScreenPaddingDp),
                themedContext.dp(28)
            )
        }

        root.addView(TextView(themedContext).apply {
            text = "저장 전 확인"
            textSize = 28f
            setTextColor(PremiumColors.TextPrimary)
            setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(premiumBodyText(
            themedContext,
            "분류 결과와 저장될 내용을 확인하거나 수정해 주세요."
        ).apply { setPadding(0, themedContext.dp(5), 0, themedContext.dp(16)) })

        val summaryCard = premiumCard(themedContext).apply {
            addView(LinearLayout(themedContext).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18),
                    themedContext.dp(18)
                )
                addView(premiumSectionTitle(themedContext, "입력 내용"))
                addView(TextView(themedContext).apply {
                    text = result.originalText
                    textSize = 16f
                    setTextColor(PremiumColors.TextPrimary)
                    setPadding(0, themedContext.dp(8), 0, 0)
                })
            })
        }
        root.addView(summaryCard)

        val form = LinearLayout(themedContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                themedContext.dp(18),
                themedContext.dp(18),
                themedContext.dp(18),
                themedContext.dp(18)
            )
            addView(premiumSectionTitle(themedContext, "저장 정보"))
        }
        val formCard = premiumCard(themedContext).apply { addView(form) }
        root.addView(
            formCard,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = themedContext.dp(12) }
        )

        val typeOptions = if (result.suggestedType == ClassifiedInputType.AMBIGUOUS) {
            listOf(TypeOption("유형 선택", ClassifiedInputType.AMBIGUOUS)) + concreteTypeOptions
        } else {
            concreteTypeOptions
        }
        val spinner = Spinner(themedContext).apply {
            id = R.id.preview_type
            adapter = ArrayAdapter(
                themedContext,
                android.R.layout.simple_spinner_item,
                typeOptions.map { it.label }
            ).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }
        addLabeledView(form, "분류", spinner)

        val titleInput = EditText(themedContext).apply {
            id = R.id.preview_title
            setText(result.title)
            hint = "제목"
            setSingleLine(true)
        }
        addLabeledView(form, "제목", titleInput)

        val dateContainer = LinearLayout(themedContext).apply {
            id = R.id.preview_task_fields
            orientation = LinearLayout.VERTICAL
        }
        val dateInput = EditText(themedContext).apply {
            id = R.id.preview_date
            setText((result.taskDueDate ?: result.eventDate)?.toString().orEmpty())
            hint = "yyyy-MM-dd"
            setSingleLine(true)
        }
        addLabeledView(dateContainer, "날짜 또는 마감일", dateInput)
        form.addView(dateContainer)

        val eventFields = LinearLayout(themedContext).apply {
            id = R.id.preview_event_fields
            orientation = LinearLayout.VERTICAL
        }
        val timeInput = EditText(themedContext).apply {
            id = R.id.preview_time
            setText(result.eventStartTime?.toString().orEmpty())
            hint = "HH:mm"
            setSingleLine(true)
        }
        val endTimeInput = EditText(themedContext).apply {
            id = R.id.preview_end_time
            setText(result.eventEndTime?.toString().orEmpty())
            hint = "비워두면 1시간 뒤"
            setSingleLine(true)
        }
        val reminderInput = EditText(themedContext).apply {
            id = R.id.preview_reminder
            setText(result.reminderMinutes?.toString().orEmpty())
            hint = "분 전"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }
        addLabeledView(eventFields, "시작 시간", timeInput)
        addLabeledView(eventFields, "종료 시간", endTimeInput)
        addLabeledView(eventFields, "알림(분 전)", reminderInput)
        form.addView(eventFields)

        val remember = CheckBox(themedContext).apply {
            id = R.id.preview_remember
            text = "이 표현을 기억"
            visibility = View.GONE
            setPadding(0, themedContext.dp(8), 0, 0)
        }
        form.addView(remember)

        val keywordContainer = LinearLayout(themedContext).apply {
            id = R.id.preview_keyword_container
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.START
            visibility = View.GONE
        }
        form.addView(keywordContainer)

        val error = TextView(themedContext).apply {
            id = R.id.preview_error
            visibility = View.GONE
            setTextColor(PremiumColors.Error)
            setPadding(0, themedContext.dp(10), 0, 0)
        }
        form.addView(error)

        val actions = LinearLayout(themedContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        val cancel = premiumSecondaryButton(themedContext, "취소").apply {
            id = R.id.preview_cancel
            contentDescription = "저장 취소"
            setOnClickListener { onCancel() }
        }
        val save = premiumPrimaryButton(themedContext, "저장").apply {
            id = R.id.preview_save
            contentDescription = "확인한 내용 저장"
        }
        actions.addView(
            cancel,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginEnd = themedContext.dp(6) }
        )
        actions.addView(
            save,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginStart = themedContext.dp(6) }
        )
        root.addView(
            actions,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = themedContext.dp(14) }
        )
        root.attachControls(save, error)

        val candidateExtractor = KeywordCandidateExtractor()
        val candidates = candidateExtractor
            .extract(result.title, emptyList())
            .ifEmpty { candidateExtractor.extract(result.originalText, emptyList()) }
            .take(MAX_KEYWORD_CANDIDATES)
        var selectedKeyword: String? = null
        val keywordButtons = mutableListOf<Button>()
        candidates.forEach { keyword ->
            val button = premiumSecondaryButton(themedContext, keyword).apply {
                textSize = 12f
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
        } ?: ClassifiedInputType.AMBIGUOUS
        var currentType = initialType
        var latestValidation: DraftValidationResult? = null

        fun updateTypeSpecificViews() {
            val hasDate = currentType == ClassifiedInputType.EVENT ||
                currentType == ClassifiedInputType.TASK
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
            save.contentDescription = if (currentType == ClassifiedInputType.EVENT) {
                "캘린더에서 일정 저장 확인"
            } else {
                "확인한 내용 저장"
            }

            val changed = currentType != ClassifiedInputType.AMBIGUOUS &&
                currentType != result.suggestedType
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
                is DraftValidationResult.Invalid -> root.setFormValidity(false, validation.message)
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
        container.addView(TextView(themedContext).apply {
            text = label
            textSize = 13f
            setTextColor(PremiumColors.TextSecondary)
            setPadding(0, themedContext.dp(10), 0, themedContext.dp(4))
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
