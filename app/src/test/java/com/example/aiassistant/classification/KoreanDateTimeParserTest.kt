package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanDateTimeParserTest {
    private val parser = KoreanDateTimeParser()
    private val now = ZonedDateTime.of(2026, 8, 5, 14, 0, 0, 0, ZoneId.of("Asia/Seoul"))

    @Test
    fun parsesTomorrowAfternoonAndReminder() {
        val result = parser.parse("내일 오후 3시 병원 30분 전에 알려줘", now)
        assertEquals(LocalDate.of(2026, 8, 6), result.date)
        assertEquals(LocalTime.of(15, 0), result.time)
        assertEquals(30, result.reminderMinutes)
        assertFalse(result.isPast)
    }

    @Test
    fun parsesDeadlineAsDueDate() {
        val result = parser.parse("금요일까지 보고서 제출", now)
        assertEquals(LocalDate.of(2026, 8, 7), result.dueDate)
        assertNull(result.time)
    }

    @Test
    fun sameWeekdayWithoutTimeMeansToday() {
        val result = parser.parse("수요일 회의", now)
        assertEquals(LocalDate.of(2026, 8, 5), result.date)
    }

    @Test
    fun sameWeekdayWithPastTimeMovesToNextWeek() {
        val result = parser.parse("수요일 오전 9시 회의", now)
        assertEquals(LocalDate.of(2026, 8, 12), result.date)
    }

    @Test
    fun explicitPastDateIsBlocked() {
        val result = parser.parse("2026-08-01 회의", now)
        assertTrue(result.isPast)
    }

    @Test
    fun parsesNoonMidnightAndClockFormats() {
        assertEquals(LocalTime.NOON, parser.parse("내일 정오 회의", now).time)
        assertEquals(LocalTime.MIDNIGHT, parser.parse("내일 자정 출발", now).time)
        assertEquals(LocalTime.of(15, 30), parser.parse("내일 15:30 회의", now).time)
    }
}
