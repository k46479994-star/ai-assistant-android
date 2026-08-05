package com.example.aiassistant.calendar

import android.content.Intent
import android.provider.CalendarContract
import com.example.aiassistant.classification.EventDraft
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CalendarGatewayTest {
    @Test
    fun buildsInsertIntentWithoutCalendarPermissions() {
        val event = EventDraft(
            title = "병원",
            date = LocalDate.of(2026, 8, 6),
            startTime = LocalTime.of(15, 0),
            endTime = LocalTime.of(16, 0),
            reminderMinutes = 30
        )

        val intent = CalendarGateway().buildInsertIntent(
            event,
            ZoneId.of("Asia/Seoul")
        )

        assertEquals(Intent.ACTION_INSERT, intent.action)
        assertEquals(CalendarContract.Events.CONTENT_URI, intent.data)
        assertEquals(
            "병원",
            intent.getStringExtra(CalendarContract.Events.TITLE)
        )
        assertTrue(intent.hasExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME))
        assertTrue(intent.hasExtra(CalendarContract.EXTRA_EVENT_END_TIME))
    }
}
