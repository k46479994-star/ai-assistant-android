package com.example.aiassistant.classification

class KeywordCandidateExtractor {
    private val stopWords = setOf(
        "오늘", "내일", "모레", "오전", "오후", "전에", "알려줘", "해줘",
        "추가", "일정", "할일", "메모"
    )
    private val actionSuffixes = setOf(
        "복습하기", "준비하기", "확인하기", "신청하기", "전화하기", "보내기", "사기", "하기"
    )
    private val splitPattern = Regex("[^가-힣A-Za-z]+")

    fun extract(text: String, consumedRanges: List<IntRange>): List<String> {
        val masked = maskRanges(text, consumedRanges)
        val distinct = LinkedHashSet<String>()

        masked.split(splitPattern).forEach { rawToken ->
            val token = normalize(rawToken)
            if (token.length in 2..20 && token !in stopWords) {
                distinct += token
            }
        }

        return distinct.toList()
    }

    private fun normalize(rawToken: String): String {
        val token = rawToken.trim()
        return if (token in actionSuffixes && token.endsWith("하기") && token.length > 2) {
            token.removeSuffix("하기")
        } else {
            token
        }
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
