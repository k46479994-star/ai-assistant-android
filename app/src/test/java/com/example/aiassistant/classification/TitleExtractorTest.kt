package com.example.aiassistant.classification

import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TitleExtractorTest {
    @Test
    fun removesDateTimeReminderAndCommandWords() {
        val parser = KoreanDateTimeParser()
        val now = ZonedDateTime.parse("2026-08-05T14:00:00+09:00[Asia/Seoul]")
        val text = "내일 오후 3시 병원 30분 전에 알려줘"
        val parsed = parser.parse(text, now)

        assertEquals(
            "병원",
            TitleExtractor().extract(text, parsed.consumedRanges, InputType.EVENT)
        )
    }

    @Test
    fun noteTitleUsesFirstThirtyCharacters() {
        val text = "프로젝트 아이디어: 발표 순서를 바꾸고 마지막에 질의응답을 배치한다"

        assertEquals(
            text.take(30),
            TitleExtractor().extract(text, emptyList(), InputType.NOTE)
        )
    }
}
