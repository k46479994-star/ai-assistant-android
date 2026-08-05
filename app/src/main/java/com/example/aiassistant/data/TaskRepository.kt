package com.example.aiassistant.data

import java.time.LocalDate

class TaskRepository(private val taskDao: TaskDao) {
    suspend fun insert(
        title: String,
        originalText: String,
        dueDate: LocalDate?,
        nowEpochMillis: Long
    ): Long = taskDao.insert(
        TaskEntity(
            title = title,
            originalText = originalText,
            dueDateEpochDay = dueDate?.toEpochDay(),
            isCompleted = false,
            createdAtEpochMillis = nowEpochMillis,
            updatedAtEpochMillis = nowEpochMillis
        )
    )

    suspend fun listAll(): List<TaskEntity> = taskDao.listAll()

    suspend fun setCompleted(
        id: Long,
        completed: Boolean,
        updatedAtEpochMillis: Long
    ) {
        taskDao.setCompleted(id, completed, updatedAtEpochMillis)
    }

    suspend fun countOpenDueOn(date: LocalDate): Int =
        taskDao.countOpenDueOn(date.toEpochDay())
}
