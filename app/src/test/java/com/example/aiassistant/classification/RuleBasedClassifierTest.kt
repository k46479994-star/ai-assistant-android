package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBasedClassifierTest {
    private val classifier = RuleBasedClassifier()
    private val now = ZonedDateTime.parse("2026-08-05T14:00:00+09:00[Asia/Seoul]")

    @Test
    fun classifiesDatedTimedHospitalAsEvent() {
        val result = classifier.classify("내일 오후 3시 병원", now, emptyList())

        assertEquals(InputType.EVENT, result.suggestedType)
        assertEquals(Confidence.HIGH, result.confidence)
        assertEquals("병원", result.title)
    }

    @Test
    fun classifiesDeadlineSubmissionAsTask() {
        val result = classifier.classify("금요일까지 보고서 제출", now, emptyList())

        assertEquals(InputType.TASK, result.suggestedType)
        assertEquals(LocalDate.of(2026, 8, 7), result.taskDueDate)
    }

    @Test
    fun classifiesShoppingAndReviewAsTasks() {
        assertEquals(
            InputType.TASK,
            classifier.classify("우유 사기", now, emptyList()).suggestedType
        )
        assertEquals(
            InputType.TASK,
            classifier.classify("교재 3장 복습하기", now, emptyList()).suggestedType
        )
    }

    @Test
    fun classifiesIdeaAsNote() {
        assertEquals(
            InputType.NOTE,
            classifier.classify(
                "프로젝트 아이디어: 발표 순서를 바꾸기",
                now,
                emptyList()
            ).suggestedType
        )
    }

    @Test
    fun lowConfidenceHospitalResearchIsAmbiguous() {
        val result = classifier.classify("병원 알아보기", now, emptyList())

        assertEquals(InputType.AMBIGUOUS, result.suggestedType)
        assertEquals(Confidence.LOW, result.confidence)
        assertTrue(result.missingFields.contains(RequiredField.TYPE))
    }

    @Test
    fun learnedRuleRequiresExactNormalizedToken() {
        val rule = LearnedRule("복습", InputType.NOTE)

        assertEquals(
            InputType.NOTE,
            classifier.classify("교재 복습하기", now, listOf(rule)).suggestedType
        )
        assertNotEquals(
            InputType.NOTE,
            classifier.classify("복습장 사기", now, listOf(rule)).suggestedType
        )
    }
}
