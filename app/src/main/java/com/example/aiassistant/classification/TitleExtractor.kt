package com.example.aiassistant.classification

class TitleExtractor {
    private val commandWords = Regex("일정\\s*추가|할\\s*일\\s*추가|메모해|기록해|알려줘|해줘|저장해|추가해")
    private val punctuation = Regex("^[\\s,.:;·-]+|[\\s,.:;·-]+$")
    private val repeatedWhitespace = Regex("\\s+")

    fun extract(text: String, consumedRanges: List<IntRange>, inputType: InputType): String {
        if (inputType == InputType.NOTE) {
            return text.lineSequence().firstOrNull().orEmpty().trim().take(30)
        }

        val masked = maskRanges(text, consumedRanges)
        val withoutCommands = commandWords.replace(masked, " ")
        val compact = repeatedWhitespace.replace(withoutCommands, " ").trim()
        return punctuation.replace(compact, "").trim()
    }

    private fun maskRanges(text: String, ranges: List<IntRange>): String {
        val characters = text.toCharArray()
        ranges.forEach { range ->
            range.forEach { index ->
                if (index in characters.indices) characters[index] = ' '
            }
        }
        return String(characters)
    }
}
