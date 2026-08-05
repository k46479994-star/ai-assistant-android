package com.example.aiassistant.data

import android.content.Context
import com.example.aiassistant.classification.UserSettings

class SettingsStore(context: Context) : UserSettings {
    private val preferences = context.getSharedPreferences(
        FILE_NAME,
        Context.MODE_PRIVATE
    )

    override fun defaultEventDurationMinutes(): Int =
        preferences.getInt(KEY_EVENT_DURATION_MINUTES, DEFAULT_EVENT_DURATION_MINUTES)
            .coerceIn(MIN_EVENT_DURATION_MINUTES, MAX_EVENT_DURATION_MINUTES)

    override fun defaultReminderMinutes(): Int =
        preferences.getInt(KEY_REMINDER_MINUTES, DEFAULT_REMINDER_MINUTES)
            .coerceIn(MIN_REMINDER_MINUTES, MAX_REMINDER_MINUTES)

    fun saveDefaults(eventDurationMinutes: Int, reminderMinutes: Int) {
        preferences.edit()
            .putInt(
                KEY_EVENT_DURATION_MINUTES,
                eventDurationMinutes.coerceIn(
                    MIN_EVENT_DURATION_MINUTES,
                    MAX_EVENT_DURATION_MINUTES
                )
            )
            .putInt(
                KEY_REMINDER_MINUTES,
                reminderMinutes.coerceIn(MIN_REMINDER_MINUTES, MAX_REMINDER_MINUTES)
            )
            .apply()
    }

    companion object {
        private const val FILE_NAME = "offline_core_settings"
        private const val KEY_EVENT_DURATION_MINUTES = "event_duration_minutes"
        private const val KEY_REMINDER_MINUTES = "reminder_minutes"

        private const val DEFAULT_EVENT_DURATION_MINUTES = 60
        private const val DEFAULT_REMINDER_MINUTES = 30
        private const val MIN_EVENT_DURATION_MINUTES = 15
        private const val MAX_EVENT_DURATION_MINUTES = 480
        private const val MIN_REMINDER_MINUTES = 0
        private const val MAX_REMINDER_MINUTES = 1440
    }
}
