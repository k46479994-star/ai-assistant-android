package com.example.aiassistant.calendar

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.CalendarContract
import com.example.aiassistant.classification.EventDraft
import java.time.ZoneId
import java.time.ZonedDateTime

class CalendarGateway {
    fun buildInsertIntent(
        event: EventDraft,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Intent {
        val startMillis = ZonedDateTime.of(
            event.date,
            event.startTime,
            zoneId
        ).toInstant().toEpochMilli()
        val endMillis = ZonedDateTime.of(
            event.date,
            event.endTime,
            zoneId
        ).toInstant().toEpochMilli()

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
        }
    }

    fun launch(activity: Activity, event: EventDraft): Boolean = try {
        activity.startActivity(buildInsertIntent(event))
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
