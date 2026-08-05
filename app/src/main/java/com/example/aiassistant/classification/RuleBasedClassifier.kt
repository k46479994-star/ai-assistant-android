package com.example.aiassistant.classification

import java.time.ZonedDateTime

class RuleBasedClassifier(
    private val parser: KoreanDateTimeParser = KoreanDateTimeParser(),
    private val titleExtractor: TitleExtractor = TitleExtractor(),
    private val keywordCandidateExtractor: KeywordCandidateExtractor = KeywordCandidateExtractor()
) : LocalInputClassifier {
    private val eventKeywords = listOf("예약", "회의", "약속", "수업", "병원", "치과", "면접", "행사")
    private val taskKeywords = listOf("사기", "하기", "복습", "준비", "전화", "보내기", "확인", "신청", "제출")
    private val noteKeywords = listOf("메모", "기록", "아이디어", "내용", "참고")
    private val taskEnding = Regex("(사기|복습하기|준비하기|전화하기|보내기|확인하기|신청하기|제출|해|해줘|할\\s*것)\\s*$")
    private val colonWithText = Regex(":\\s*\\S+")

    override fun classify(
        text: String,
        now: ZonedDateTime,
        learnedRules: List<LearnedRule>
    ): ClassificationResult {
        val parsed = parser.parse(text, now)
        val normalizedTokens = keywordCandidateExtractor.extract(text, parsed.consumedRanges)
        val learnedMatches = learnedRules
            .filter { rule ->
                rule.targetType != InputType.AMBIGUOUS &&
                    rule.normalizedKeyword.isNotBlank() &&
                    rule.normalizedKeyword in normalizedTokens
            }
            .distinctBy { it.normalizedKeyword }
        val learnedTargetByToken = learnedMatches.associate {
            it.normalizedKeyword to it.targetType
        }

        val scores = linkedMapOf(
            InputType.EVENT to 0,
            InputType.TASK to 0,
            InputType.NOTE to 0
        )
        val matchedRules = mutableListOf<String>()

        fun addScore(type: InputType, points: Int, description: String) {
            scores[type] = scores.getValue(type) + points
            val sign = if (points >= 0) "+" else ""
            matchedRules += "${type.name}: $description ($sign$points)"
        }

        fun isSuppressed(keyword: String, type: InputType): Boolean {
            val learnedTarget = learnedTargetByToken[keyword] ?: return false
            return learnedTarget != type
        }

        learnedMatches.forEach { rule ->
            addScore(
                rule.targetType,
                6,
                "학습 키워드 '${rule.normalizedKeyword}'"
            )
        }

        if (parsed.hasDateToken && parsed.hasTimeToken) {
            addScore(InputType.EVENT, 5, "날짜와 시간이 모두 있음")
        }
        eventKeywords.firstOrNull { keyword ->
            text.contains(keyword) && !isSuppressed(keyword, InputType.EVENT)
        }?.let { keyword ->
            addScore(InputType.EVENT, 3, "일정 키워드 '$keyword'")
        }
        if (parsed.reminderMinutes != null) {
            addScore(InputType.EVENT, 2, "알림 표현")
        }
        if (parsed.hasDeadlineToken && !parsed.hasTimeToken) {
            addScore(InputType.EVENT, -2, "시간 없는 마감 표현")
        }

        val hasDeadlineOrSubmission =
            parsed.hasDeadlineToken ||
                text.contains("마감") ||
                (text.contains("제출") && !isSuppressed("제출", InputType.TASK))
        if (hasDeadlineOrSubmission) {
            addScore(InputType.TASK, 4, "마감 또는 제출 표현")
        }

        taskKeywords.firstOrNull { keyword ->
            val isPresent = if (keyword == "하기") {
                keyword in normalizedTokens
            } else {
                text.contains(keyword)
            }
            isPresent && !isSuppressed(keyword, InputType.TASK)
        }?.let { keyword ->
            addScore(InputType.TASK, 3, "할 일 키워드 '$keyword'")
        }

        val lastToken = normalizedTokens.lastOrNull()
        val actionIsLearnedAsAnotherType = lastToken?.let { token ->
            learnedTargetByToken[token]?.let { it != InputType.TASK }
        } == true
        val hasTaskActionClue = taskEnding.containsMatchIn(text.trim()) && !actionIsLearnedAsAnotherType
        if (hasTaskActionClue) {
            addScore(InputType.TASK, 2, "할 일 동작으로 끝남")
        }
        if (parsed.hasDateToken && parsed.hasTimeToken) {
            addScore(InputType.TASK, -2, "날짜와 시간이 모두 있음")
        }

        noteKeywords.firstOrNull { keyword ->
            text.contains(keyword) && !isSuppressed(keyword, InputType.NOTE)
        }?.let { keyword ->
            addScore(InputType.NOTE, 3, "메모 키워드 '$keyword'")
        }
        if (!parsed.hasDateToken && !parsed.hasTimeToken && !hasTaskActionClue) {
            addScore(InputType.NOTE, 2, "날짜·시간·할 일 동작 단서가 없음")
        }
        if (colonWithText.containsMatchIn(text)) {
            addScore(InputType.NOTE, 2, "콜론 뒤 설명 문장")
        }

        val ordered = scores.entries.sortedByDescending { it.value }
        val best = ordered[0]
        val second = ordered[1]
        val confidence = when {
            best.value >= 7 && best.value - second.value >= 3 -> Confidence.HIGH
            best.value >= 5 && best.value - second.value >= 2 -> Confidence.MEDIUM
            else -> Confidence.LOW
        }
        val type = if (confidence == Confidence.LOW || best.value == second.value) {
            InputType.AMBIGUOUS
        } else {
            best.key
        }

        val title = titleExtractor.extract(text, parsed.consumedRanges, type)
        val missing = buildSet {
            if (type == InputType.AMBIGUOUS) add(RequiredField.TYPE)
            if (title.isBlank()) add(RequiredField.TITLE)
            if (type == InputType.EVENT && parsed.date == null) add(RequiredField.EVENT_DATE)
            if (type == InputType.EVENT && parsed.time == null) add(RequiredField.EVENT_TIME)
        }

        return ClassificationResult(
            originalText = text,
            suggestedType = type,
            confidence = confidence,
            title = title,
            eventDate = parsed.date,
            eventStartTime = parsed.time,
            eventEndTime = parsed.time?.plusMinutes(60),
            taskDueDate = parsed.dueDate,
            reminderMinutes = parsed.reminderMinutes,
            missingFields = missing,
            matchedRules = matchedRules,
            isPastDate = parsed.isPast
        )
    }
}
