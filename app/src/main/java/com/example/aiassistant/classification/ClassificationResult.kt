package com.example.aiassistant.classification

import java.time.LocalDate
import java.time.LocalTime

data class ClassificationResult(
    val originalText: String,
    val suggestedType: InputType,
    val confidence: Confidence,
    val title: String,
    val eventDate: LocalDate?,
    val eventStartTime: LocalTime?,
    val eventEndTime: LocalTime?,
    val taskDueDate: LocalDate?,
    val reminderMinutes: Int?,
    val missingFields: Set<RequiredField>,
    val matchedRules: List<String>,
    val isPastDate: Boolean = false
)
