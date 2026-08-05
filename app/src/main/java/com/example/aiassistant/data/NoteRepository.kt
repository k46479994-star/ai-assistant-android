package com.example.aiassistant.data

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insert(
        title: String,
        body: String,
        nowEpochMillis: Long
    ): Long = noteDao.insert(
        NoteEntity(
            title = title,
            body = body,
            createdAtEpochMillis = nowEpochMillis,
            updatedAtEpochMillis = nowEpochMillis
        )
    )

    suspend fun listLatest(limit: Int): List<NoteEntity> =
        noteDao.listLatest(limit.coerceAtLeast(0))
}
