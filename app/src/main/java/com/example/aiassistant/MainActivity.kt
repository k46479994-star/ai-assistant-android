package com.example.aiassistant

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildScreen())
    }

    private fun buildScreen(): LinearLayout {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(Color.rgb(247, 247, 252))
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 26f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = getString(R.string.online_status)
            textSize = 14f
            setTextColor(Color.rgb(79, 70, 229))
            setPadding(0, 6, 0, 24)
        }
        root.addView(subtitle)

        val chatContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val scroll = ScrollView(this).apply {
            addView(chatContainer)
        }
        root.addView(scroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        ))

        addBubble(chatContainer, getString(R.string.welcome_message), false)
        addBubble(chatContainer, getString(R.string.sample_user_message), true)
        addBubble(chatContainer, getString(R.string.sample_assistant_message), false)

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val input = EditText(this).apply {
            hint = getString(R.string.message_hint)
            setSingleLine(true)
        }
        val send = Button(this).apply {
            text = getString(R.string.send)
            setOnClickListener {
                val message = input.text.toString().trim()
                if (message.isNotEmpty()) {
                    addBubble(chatContainer, message, true)
                    addBubble(chatContainer, getString(R.string.demo_reply), false)
                    input.text.clear()
                    scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
                }
            }
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        inputRow.addView(send)
        root.addView(inputRow)

        return root
    }

    private fun addBubble(container: LinearLayout, text: String, isUser: Boolean) {
        val bubble = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTextColor(if (isUser) Color.WHITE else Color.rgb(35, 31, 58))
            setPadding(24, 18, 24, 18)
            setBackgroundColor(if (isUser) Color.rgb(79, 70, 229) else Color.WHITE)
        }
        val params = LinearLayout.LayoutParams(
            (resources.displayMetrics.widthPixels * 0.78f).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = if (isUser) Gravity.END else Gravity.START
            setMargins(0, 0, 0, 18)
        }
        container.addView(bubble, params)
    }
}
