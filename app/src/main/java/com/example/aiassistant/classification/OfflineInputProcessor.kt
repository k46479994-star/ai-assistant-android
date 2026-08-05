package com.example.aiassistant.classification

import java.time.ZonedDateTime

interface UserSettings {
    fun defaultEventDurationMinutes(): Int
    fun defaultReminderMinutes(): Int
}

interface LearnedRuleReader {
    suspend fun getValidRules(): List<LearnedRule>
}

class OfflineInputProcessor(
    private val classifier: LocalInputClassifier,
    private val learnedRuleReader: LearnedRuleReader,
    private val settings: UserSettings
) {
    suspend fun process(text: String, now: ZonedDateTime): ClassificationResult {
        require(text.isNotBlank()) { "내용을 입력해 주세요" }
        require(text.length <= MAX_INPUT_LENGTH) { "입력은 500자까지 가능합니다" }

        val base = classifier.classify(
            text = text,
            now = now,
            learnedRules = learnedRuleReader.getValidRules()
        )
        if (base.suggestedType != InputType.EVENT || base.eventStartTime == null) {
            return base
        }

        return base.copy(
            eventEndTime = base.eventStartTime.plusMinutes(
                settings.defaultEventDurationMinutes().toLong()
            ),
            reminderMinutes = base.reminderMinutes ?: settings.defaultReminderMinutes()
        )
    }

    private companion object {
        const val MAX_INPUT_LENGTH = 500
    }
}
