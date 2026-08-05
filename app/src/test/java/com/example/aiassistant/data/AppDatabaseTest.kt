package com.example.aiassistant.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.aiassistant.classification.InputType
import com.example.aiassistant.classification.LearnedRule
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun tasksSaveCompleteAndSort() = runTest {
        val repository = TaskRepository(database.taskDao())
        val id = repository.insert(
            title = "보고서 제출",
            originalText = "금요일까지 보고서 제출",
            dueDate = LocalDate.of(2026, 8, 7),
            nowEpochMillis = 1000
        )

        repository.setCompleted(id, completed = true, updatedAtEpochMillis = 2000)

        assertTrue(repository.listAll().single().isCompleted)
    }

    @Test
    fun notesReturnNewestFirst() = runTest {
        val repository = NoteRepository(database.noteDao())
        repository.insert("첫 메모", "첫 메모", 1000)
        repository.insert("둘째 메모", "둘째 메모", 2000)

        assertEquals("둘째 메모", repository.listLatest(3).first().title)
    }

    @Test
    fun learnedKeywordIsUniqueAndInvalidTypeIsIgnored() = runTest {
        val store = LearnedRuleStore(database.learnedRuleDao())
        store.upsert("복습", InputType.NOTE, 1000)
        store.upsert("복습", InputType.TASK, 2000)
        database.learnedRuleDao().insertRaw(
            LearnedRuleEntity(
                id = 0,
                normalizedKeyword = "잘못된규칙",
                targetTypeName = "BROKEN",
                createdAtEpochMillis = 3000,
                lastUsedAtEpochMillis = 3000
            )
        )

        val rules = store.getValidRules()

        assertEquals(listOf(LearnedRule("복습", InputType.TASK)), rules)
    }
}
