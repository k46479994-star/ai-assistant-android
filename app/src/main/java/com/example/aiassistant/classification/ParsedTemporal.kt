package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime

data class ParsedTemporal(
    val date: LocalDate?,
    val time: LocalTime?,
    val dueDate: LocalDate?,
    val reminderMinutes: Int?,
    val hasDateToken: Boolean,
    val hasTimeToken: Boolean,
    val hasDeadlineToken: Boolean,
    val consumedRanges: List<IntRange>,
    val isPast: Boolean
)
