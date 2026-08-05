package com.example.aiassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NoteDao {
    @Insert
    suspend fun insert(entity: NoteEntity): Long

    @Query("SELECT * FROM notes ORDER BY createdAtEpochMillis DESC LIMIT :limit")
    suspend fun listLatest(limit: Int): List<NoteEntity>
}
