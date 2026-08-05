package com.example.aiassistant.classification

import java.time.LocalTime
import java.time.ZonedDateTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class OfflineInputProcessorTest {
    @Test
    fun appliesSavedDurationAndReminderToEvents() = runTest {
        val classifier = RuleBasedClassifier()
        val rules = FakeLearnedRuleStore(emptyList())
        val settings = FakeSettingsStore(duration = 90, reminder = 15)
        val processor = OfflineInputProcessor(classifier, rules, settings)
        val now = ZonedDateTime.parse("2026-08-05T14:00:00+09:00[Asia/Seoul]")

        val result = processor.process("내일 오후 3시 병원", now)

        assertEquals(LocalTime.of(16, 30), result.eventEndTime)
        assertEquals(15, result.reminderMinutes)
    }

    private class FakeLearnedRuleStore(
        private val rules: List<LearnedRule>
    ) : LearnedRuleReader {
        override suspend fun getValidRules(): List<LearnedRule> = rules
    }

    private class FakeSettingsStore(
        private val duration: Int,
        private val reminder: Int
    ) : UserSettings {
        override fun defaultEventDurationMinutes(): Int = duration
        override fun defaultReminderMinutes(): Int = reminder
    }
}
