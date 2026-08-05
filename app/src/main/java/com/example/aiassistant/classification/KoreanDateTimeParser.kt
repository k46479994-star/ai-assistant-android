package com.example.aiassistant.classification

import java.time.DateTimeException
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime

class KoreanDateTimeParser {
    private val fullKoreanDate = Regex("(\\d{4})년\\s*(\\d{1,2})월\\s*(\\d{1,2})일")
    private val isoDate = Regex("(\\d{4})-(\\d{1,2})-(\\d{1,2})")
    private val monthDay = Regex("(\\d{1,2})월\\s*(\\d{1,2})일")
    private val relativeDate = Regex("오늘|내일|모레")
    private val weekday = Regex("([월화수목금토일])요일")
    private val amPmTime = Regex("(오전|오후)\\s*(\\d{1,2})시(?:\\s*(\\d{1,2})분)?")
    private val colonTime = Regex("(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)")
    private val plainTime = Regex("(?<!월\\s)(?<!일\\s)(\\d{1,2})시(?:\\s*(\\d{1,2})분)?")
    private val minuteReminder = Regex("(\\d+)\\s*분\\s*전(?:에)?")
    private val hourReminder = Regex("(\\d+)\\s*시간\\s*전(?:에)?")
    private val noon = Regex("정오")
    private val midnight = Regex("자정")
    private val deadlineToken = Regex("까지")

    fun parse(text: String, now: ZonedDateTime): ParsedTemporal {
        val consumed = mutableListOf<IntRange>()
        val parsedTime = parseTime(text, consumed)
        val parsedDate = parseDate(text, now, parsedTime, consumed)
        val reminder = parseReminder(text, consumed)
        val deadlineMatch = if (parsedDate != null) deadlineToken.find(text) else null
        val deadline = deadlineMatch != null
        if (deadlineMatch != null) consumed += deadlineMatch.range
        val dueDate = if (deadline) parsedDate else null
        val isPast = parsedDate?.isBefore(now.toLocalDate()) == true

        return ParsedTemporal(
            date = parsedDate,
            time = parsedTime,
            dueDate = dueDate,
            reminderMinutes = reminder,
            hasDateToken = parsedDate != null,
            hasTimeToken = parsedTime != null,
            hasDeadlineToken = deadline,
            consumedRanges = consumed.distinct().sortedBy { it.first },
            isPast = isPast
        )
    }

    private fun parseTime(text: String, consumed: MutableList<IntRange>): LocalTime? {
        noon.find(text)?.let { match ->
            consumed += match.range
            return LocalTime.NOON
        }
        midnight.find(text)?.let { match ->
            consumed += match.range
            return LocalTime.MIDNIGHT
        }

        for (match in amPmTime.findAll(text)) {
            val period = match.groupValues[1]
            val hour = match.groupValues[2].toIntOrNull() ?: continue
            val minute = match.groupValues[3].ifEmpty { "0" }.toIntOrNull() ?: continue
            if (hour !in 1..12 || minute !in 0..59) continue

            val convertedHour = when (period) {
                "오전" -> if (hour == 12) 0 else hour
                "오후" -> if (hour == 12) 12 else hour + 12
                else -> continue
            }
            consumed += match.range
            return LocalTime.of(convertedHour, minute)
        }

        for (match in colonTime.findAll(text)) {
            val hour = match.groupValues[1].toIntOrNull() ?: continue
            val minute = match.groupValues[2].toIntOrNull() ?: continue
            if (hour !in 0..23 || minute !in 0..59) continue

            consumed += match.range
            return LocalTime.of(hour, minute)
        }

        for (match in plainTime.findAll(text)) {
            val precedingText = text.substring(0, match.range.first).trimEnd()
            val followsAmPm = precedingText.endsWith("오전") || precedingText.endsWith("오후")
            val belongsToHourReminder = text.getOrNull(match.range.last + 1) == '간'
            if (followsAmPm || belongsToHourReminder) continue

            val hour = match.groupValues[1].toIntOrNull() ?: continue
            val minute = match.groupValues[2].ifEmpty { "0" }.toIntOrNull() ?: continue
            if (hour !in 0..23 || minute !in 0..59) continue

            consumed += match.range
            return LocalTime.of(hour, minute)
        }

        return null
    }

    private fun parseDate(
        text: String,
        now: ZonedDateTime,
        parsedTime: LocalTime?,
        consumed: MutableList<IntRange>
    ): LocalDate? {
        fullKoreanDate.find(text)?.let { match ->
            val date = safeDate(
                match.groupValues[1].toIntOrNull(),
                match.groupValues[2].toIntOrNull(),
                match.groupValues[3].toIntOrNull()
            )
            if (date != null) consumed += match.range
            return date
        }

        isoDate.find(text)?.let { match ->
            val date = safeDate(
                match.groupValues[1].toIntOrNull(),
                match.groupValues[2].toIntOrNull(),
                match.groupValues[3].toIntOrNull()
            )
            if (date != null) consumed += match.range
            return date
        }

        monthDay.find(text)?.let { match ->
            val date = safeDate(
                now.year,
                match.groupValues[1].toIntOrNull(),
                match.groupValues[2].toIntOrNull()
            )
            if (date != null) consumed += match.range
            return date
        }

        relativeDate.find(text)?.let { match ->
            val days = when (match.value) {
                "오늘" -> 0L
                "내일" -> 1L
                "모레" -> 2L
                else -> return@let
            }
            consumed += match.range
            return now.toLocalDate().plusDays(days)
        }

        weekday.find(text)?.let { match ->
            val target = koreanWeekday(match.groupValues[1]) ?: return@let
            var daysAhead = (target.value - now.dayOfWeek.value + 7) % 7
            if (daysAhead == 0 && parsedTime != null && !parsedTime.isAfter(now.toLocalTime())) {
                daysAhead = 7
            }
            consumed += match.range
            return now.toLocalDate().plusDays(daysAhead.toLong())
        }

        return null
    }

    private fun parseReminder(text: String, consumed: MutableList<IntRange>): Int? {
        minuteReminder.find(text)?.let { match ->
            val minutes = match.groupValues[1].toIntOrNull() ?: return@let
            consumed += match.range
            return minutes
        }

        hourReminder.find(text)?.let { match ->
            val hours = match.groupValues[1].toLongOrNull() ?: return@let
            val minutes = hours * 60L
            if (minutes > Int.MAX_VALUE) return@let
            consumed += match.range
            return minutes.toInt()
        }

        return null
    }

    private fun safeDate(year: Int?, month: Int?, day: Int?): LocalDate? {
        if (year == null || month == null || day == null) return null
        return try {
            LocalDate.of(year, month, day)
        } catch (_: DateTimeException) {
            null
        }
    }

    private fun koreanWeekday(value: String): DayOfWeek? = when (value) {
        "월" -> DayOfWeek.MONDAY
        "화" -> DayOfWeek.TUESDAY
        "수" -> DayOfWeek.WEDNESDAY
        "목" -> DayOfWeek.THURSDAY
        "금" -> DayOfWeek.FRIDAY
        "토" -> DayOfWeek.SATURDAY
        "일" -> DayOfWeek.SUNDAY
        else -> null
    }
}
