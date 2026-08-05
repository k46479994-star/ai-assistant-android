package com.example.aiassistant

import android.app.Application
import com.example.aiassistant.ui.AppThemeStore
import com.example.aiassistant.ui.PremiumColors
import com.example.aiassistant.ui.ThemePalette

class AiAssistantApplication : Application() {
    lateinit var container: AppContainer
        internal set

    override fun onCreate() {
        super.onCreate()
        PremiumColors.apply(ThemePalette.from(AppThemeStore(this).selectedColor()))
        if (!::container.isInitialized) {
            container = AppContainer(this)
        }
    }
}
