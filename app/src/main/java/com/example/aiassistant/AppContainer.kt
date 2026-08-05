package com.example.aiassistant

import android.content.Context
import androidx.room.Room
import com.example.aiassistant.calendar.CalendarGateway
import com.example.aiassistant.classification.LocalInputClassifier
import com.example.aiassistant.classification.OfflineInputProcessor
import com.example.aiassistant.classification.RuleBasedClassifier
import com.example.aiassistant.data.AppDatabase
import com.example.aiassistant.data.LearnedRuleStore
import com.example.aiassistant.data.NoteRepository
import com.example.aiassistant.data.SettingsStore
import com.example.aiassistant.data.TaskRepository

class AppContainer(context: Context) {
    private val applicationContext = context.applicationContext

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    val learnedRuleStore: LearnedRuleStore by lazy {
        LearnedRuleStore(database.learnedRuleDao())
    }

    val settingsStore: SettingsStore by lazy {
        SettingsStore(applicationContext)
    }

    val classifier: LocalInputClassifier by lazy {
        RuleBasedClassifier()
    }

    val offlineInputProcessor: OfflineInputProcessor by lazy {
        OfflineInputProcessor(
            classifier = classifier,
            learnedRuleReader = learnedRuleStore,
            settings = settingsStore
        )
    }

    val calendarGateway: CalendarGateway by lazy {
        CalendarGateway()
    }

    private companion object {
        const val DATABASE_NAME = "ai_assistant.db"
    }
}
