package com.example.aiassistant

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppContainerTest {
    @Test
    fun applicationProvidesOneContainerInstance() {
        val application =
            ApplicationProvider.getApplicationContext<AiAssistantApplication>()

        assertSame(application.container, application.container)
        assertNotNull(application.container.offlineInputProcessor)
        assertNotNull(application.container.taskRepository)
    }
}
