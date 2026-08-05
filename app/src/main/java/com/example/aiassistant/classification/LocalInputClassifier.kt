package com.example.aiassistant.classification

import java.time.ZonedDateTime

interface LocalInputClassifier {
    fun classify(
        text: String,
        now: ZonedDateTime,
        learnedRules: List<LearnedRule>
    ): ClassificationResult
}
