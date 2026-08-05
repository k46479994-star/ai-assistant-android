package com.example.aiassistant.classification

import org.junit.Assert.assertEquals
import org.junit.Test

class KeywordCandidateExtractorTest {
    @Test
    fun normalizesActionSuffixesAndRemovesNumbers() {
        val result = KeywordCandidateExtractor().extract("교재 3장 복습하기", emptyList())

        assertEquals(listOf("교재", "복습"), result)
    }

    @Test
    fun returnsDistinctTwoToTwentyCharacterTokens() {
        val result = KeywordCandidateExtractor().extract("병원 병원 예약", emptyList())

        assertEquals(listOf("병원", "예약"), result)
    }
}
