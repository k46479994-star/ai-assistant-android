package com.example.aiassistant.data

import com.example.aiassistant.classification.InputType
import com.example.aiassistant.classification.LearnedRule
import com.example.aiassistant.classification.LearnedRuleReader

class LearnedRuleStore(
    private val learnedRuleDao: LearnedRuleDao
) : LearnedRuleReader {
    suspend fun upsert(
        keyword: String,
        targetType: InputType,
        nowEpochMillis: Long
    ): Long {
        require(targetType in SUPPORTED_TYPES) { "지원하지 않는 학습 유형입니다" }
        val normalizedKeyword = normalizeKeyword(keyword)
        require(normalizedKeyword.isNotBlank()) { "기억할 표현을 선택해 주세요" }

        val existing = learnedRuleDao.findByKeyword(normalizedKeyword)
        return learnedRuleDao.upsert(
            LearnedRuleEntity(
                id = existing?.id ?: 0,
                normalizedKeyword = normalizedKeyword,
                targetTypeName = targetType.name,
                createdAtEpochMillis = existing?.createdAtEpochMillis ?: nowEpochMillis,
                lastUsedAtEpochMillis = nowEpochMillis
            )
        )
    }

    override suspend fun getValidRules(): List<LearnedRule> =
        learnedRuleDao.listAll().mapNotNull { entity ->
            val type = InputType.entries.firstOrNull {
                it.name == entity.targetTypeName
            }
            if (type in SUPPORTED_TYPES) {
                LearnedRule(entity.normalizedKeyword, requireNotNull(type))
            } else {
                null
            }
        }

    suspend fun listAll(): List<LearnedRuleEntity> = learnedRuleDao.listAll()

    suspend fun delete(id: Long) {
        learnedRuleDao.delete(id)
    }

    private fun normalizeKeyword(keyword: String): String =
        NON_TOKEN_CHARACTERS.replace(keyword.trim(), " ")
            .trim()
            .lowercase()

    private companion object {
        val SUPPORTED_TYPES = setOf(InputType.EVENT, InputType.TASK, InputType.NOTE)
        val NON_TOKEN_CHARACTERS = Regex("[^가-힣A-Za-z0-9]+")
    }
}
