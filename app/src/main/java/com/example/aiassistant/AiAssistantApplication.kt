package com.example.aiassistant

import android.app.Application

class AiAssistantApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(this)
    }
}
