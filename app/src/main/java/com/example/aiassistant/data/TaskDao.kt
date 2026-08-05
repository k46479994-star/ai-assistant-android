package com.example.aiassistant.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(entity: TaskEntity): Long

    @Query(
        """
        SELECT * FROM tasks
        ORDER BY isCompleted ASC,
                 CASE WHEN dueDateEpochDay IS NULL THEN 1 ELSE 0 END,
                 dueDateEpochDay ASC,
                 createdAtEpochMillis DESC
        """
    )
    suspend fun listAll(): List<TaskEntity>

    @Query(
        """
        UPDATE tasks
        SET isCompleted = :completed,
            updatedAtEpochMillis = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun setCompleted(id: Long, completed: Boolean, updatedAt: Long)

    @Query(
        """
        SELECT COUNT(*) FROM tasks
        WHERE isCompleted = 0 AND dueDateEpochDay = :epochDay
        """
    )
    suspend fun countOpenDueOn(epochDay: Long): Int
}
