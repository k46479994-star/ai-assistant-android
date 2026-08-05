package com.example.aiassistant

import android.app.Application

class AiAssistantApplication : Application() {
    lateinit var container: AppContainer
        internal set

    override fun onCreate() {
        super.onCreate()
        if (!::container.isInitialized) {
            container = AppContainer(this)
        }
    }
}
