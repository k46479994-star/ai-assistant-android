package com.example.aiassistant.ui

import com.example.aiassistant.classification.InputType

data class RememberSelection(
    val normalizedKeyword: String,
    val targetType: InputType
)
