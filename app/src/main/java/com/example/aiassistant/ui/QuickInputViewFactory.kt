package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.aiassistant.R

class QuickInputViewFactory(private val context: Context) {
    fun create(onSubmit: (String) -> Unit): View {
        val root = LinearLayout(context).apply {
            id = R.id.screen_quick_input
            orientation = LinearLayout.VERTICAL
            setPadding(28, 28, 28, 24)
        }

        root.addView(TextView(context).apply {
            text = "빠른 입력"
            textSize = 27f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        root.addView(TextView(context).apply {
            text = "AI나 인터넷 없이도 일정·할 일·메모를 자동으로 분류합니다."
            textSize = 15f
            setTextColor(Color.rgb(92, 88, 112))
            setPadding(0, 8, 0, 18)
        })

        val input = EditText(context).apply {
            id = R.id.quick_input_text
            hint = "예: 내일 오후 3시 병원, 30분 전에 알려줘"
            minLines = 4
            maxLines = 8
            gravity = Gravity.TOP or Gravity.START
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(20, 18, 20, 18)
            setBackgroundColor(Color.WHITE)
        }
        root.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val error = TextView(context).apply {
            id = R.id.quick_input_error
            visibility = View.GONE
            setTextColor(Color.rgb(185, 28, 28))
            setPadding(4, 10, 4, 4)
        }
        root.addView(error)

        root.addView(TextView(context).apply {
            text = "입력 예시"
            textSize = 14f
            setTextColor(Color.rgb(92, 88, 112))
            setPadding(0, 16, 0, 6)
        })

        val examples = LinearLayout(context).apply {
            id = R.id.quick_input_examples
            orientation = LinearLayout.VERTICAL
        }
        listOf(
            "내일 오후 3시 병원",
            "금요일까지 보고서 제출",
            "프로젝트 아이디어: 발표 순서를 바꾸기"
        ).forEach { example ->
            examples.addView(Button(context).apply {
                text = example
                textSize = 13f
                isAllCaps = false
                gravity = Gravity.START or Gravity.CENTER_VERTICAL
                setOnClickListener {
                    input.setText(example)
                    input.setSelection(example.length)
                    error.visibility = View.GONE
                }
            })
        }
        root.addView(examples)

        val submit = Button(context).apply {
            id = R.id.quick_input_submit
            text = "분류하기"
            isAllCaps = false
            setOnClickListener {
                val rawText = input.text.toString()
                val message = when {
                    rawText.isBlank() -> "내용을 입력해 주세요"
                    rawText.length > MAX_INPUT_LENGTH -> "입력은 500자까지 가능합니다"
                    else -> null
                }

                if (message != null) {
                    error.text = message
                    error.visibility = View.VISIBLE
                } else {
                    error.visibility = View.GONE
                    onSubmit(rawText.trim())
                }
            }
        }
        root.addView(
            submit,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 18 }
        )

        return root
    }

    private companion object {
        const val MAX_INPUT_LENGTH = 500
    }
}
