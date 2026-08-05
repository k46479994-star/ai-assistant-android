package com.example.aiassistant

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineManifestTest {
    @Test
    fun automaticCloudBackupIsDisabled() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val backupFlag = context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP

        assertEquals(
            "Offline data must not be eligible for Android Auto Backup",
            0,
            backupFlag
        )
    }
}
