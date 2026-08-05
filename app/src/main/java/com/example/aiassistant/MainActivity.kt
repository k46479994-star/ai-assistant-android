package com.example.aiassistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

enum class Tab { HOME, CHAT, CALENDAR, TODO, SETTINGS }

class MainActivity : AppCompatActivity() {
    private lateinit var contentContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildRoot())
        showTab(Tab.HOME)
    }

    private fun buildRoot(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(247, 247, 252))
            contentContainer = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }
            addView(contentContainer, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ))
            addView(buildBottomNavigation())
        }
    }

    private fun buildBottomNavigation(): View {
        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 12)
            setBackgroundColor(Color.WHITE)
        }
        listOf(
            "홈" to Tab.HOME,
            "AI" to Tab.CHAT,
            "일정" to Tab.CALENDAR,
            "할 일" to Tab.TODO,
            "설정" to Tab.SETTINGS
        ).forEach { (label, tab) ->
            bar.addView(Button(this).apply {
                text = label
                textSize = 12f
                isAllCaps = false
                setOnClickListener { showTab(tab) }
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }
        return bar
    }

    private fun showTab(tab: Tab) {
        contentContainer.removeAllViews()
        val view = when (tab) {
            Tab.HOME -> buildHomeView()
            Tab.CHAT -> buildChatView()
            Tab.CALENDAR -> buildCalendarView()
            Tab.TODO -> buildTodoView()
            Tab.SETTINGS -> buildSettingsView()
        }
        contentContainer.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun buildHomeView(): View {
        val page = pageLayout()
        page.addView(title("좋은 오후입니다"))
        page.addView(subtitle("오늘도 필요한 일을 빠르게 정리해 드릴게요."))
        page.addView(card("오늘 일정", "오후 3:00  병원 방문\n오후 6:00  운동"))
        page.addView(card("오늘 할 일", "장보기\n과제 제출\n부모님께 전화"))
        page.addView(card("AI 추천", "병원 일정 30분 전에 출발 알림을 준비해 보세요."))
        page.addView(Button(this).apply {
            text = "AI에게 물어보기"
            isAllCaps = false
            setOnClickListener { showTab(Tab.CHAT) }
        })
        return page
    }

    private fun buildChatView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
        }
        root.addView(title(getString(R.string.app_name)))
        root.addView(subtitle(getString(R.string.online_status)))

        val chatContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(chatContainer) }
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
            isAllCaps = false
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

    private fun buildCalendarView(): View {
        val page = pageLayout()
        page.addView(title("일정"))
        page.addView(subtitle("Google 캘린더 앱에서 최종 확인 후 저장됩니다."))
        page.addView(card("다음 일정", "오늘 오후 3:00  병원 방문\n오늘 오후 6:00  운동"))
        page.addView(Button(this).apply {
            text = "병원 일정 추가"
            isAllCaps = false
            setOnClickListener { openCalendarInsert() }
        })
        return page
    }

    private fun buildTodoView(): View {
        val page = pageLayout()
        page.addView(title("할 일"))
        listOf("장보기", "과제 제출", "부모님께 전화").forEach { item ->
            page.addView(CheckBox(this).apply {
                text = item
                textSize = 17f
                setPadding(4, 12, 4, 12)
            })
        }
        return page
    }

    private fun buildSettingsView(): View {
        val page = pageLayout()
        page.addView(title("설정"))
        page.addView(card("앱 정보", "버전 0.2\n개인정보는 현재 기기 안에서만 처리됩니다."))
        page.addView(card("AI 연결", "현재 버전은 데모 응답을 사용합니다."))
        return page
    }

    private fun openCalendarInsert() {
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "병원 방문")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.timeInMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, start.timeInMillis + 60 * 60 * 1000)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "사용 가능한 캘린더 앱이 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    private fun pageLayout(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 32, 24, 24)
    }

    private fun title(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 28f
        setTextColor(Color.rgb(35, 31, 58))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 0, 0, 8)
    }

    private fun subtitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(Color.rgb(92, 88, 112))
        setPadding(0, 0, 0, 20)
    }

    private fun card(title: String, body: String): View = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 20, 24, 20)
        setBackgroundColor(Color.WHITE)
        addView(TextView(this@MainActivity).apply {
            text = title
            textSize = 18f
            setTextColor(Color.rgb(35, 31, 58))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        addView(TextView(this@MainActivity).apply {
            text = body
            textSize = 16f
            setTextColor(Color.rgb(65, 61, 84))
            setPadding(0, 10, 0, 0)
        })
    }.also {
        it.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 18) }
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
