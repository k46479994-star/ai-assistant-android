package com.example.aiassistant.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppThemeStoreTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun storesSelectedColorAndParsesHexPaletteInput() {
        val store = AppThemeStore(context)
        store.clearForTest()
        assertEquals(0xFF7C5CFF.toInt(), store.selectedColor())

        val parsed = AppThemeStore.parseColor("#1E88E5")
        assertEquals(0xFF1E88E5.toInt(), parsed)
        store.saveSelectedColor(parsed!!)
        assertEquals(parsed, AppThemeStore(context).selectedColor())
    }

    @Test
    fun paletteCreatesReadableNavigationColors() {
        val palette = ThemePalette.from(0xFFFFC107.toInt())
        assertTrue(palette.primary != palette.onPrimary)
        assertTrue(palette.navigationUnselected != palette.surface)
    }
}