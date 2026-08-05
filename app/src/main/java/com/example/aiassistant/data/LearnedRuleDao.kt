package com.example.aiassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LearnedRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LearnedRuleEntity): Long

    @Insert
    suspend fun insertRaw(entity: LearnedRuleEntity): Long

    @Query("SELECT * FROM learned_rules WHERE normalizedKeyword = :keyword LIMIT 1")
    suspend fun findByKeyword(keyword: String): LearnedRuleEntity?

    @Query("SELECT * FROM learned_rules ORDER BY lastUsedAtEpochMillis DESC")
    suspend fun listAll(): List<LearnedRuleEntity>

    @Query("DELETE FROM learned_rules WHERE id = :id")
    suspend fun delete(id: Long)
}
