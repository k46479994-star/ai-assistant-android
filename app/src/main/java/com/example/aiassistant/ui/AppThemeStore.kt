package com.example.aiassistant.ui

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.ColorUtils

class AppThemeStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun selectedColor(): Int = preferences.getInt(KEY_COLOR, DEFAULT_COLOR)

    fun saveSelectedColor(color: Int) {
        preferences.edit().putInt(KEY_COLOR, color or 0xFF000000.toInt()).apply()
    }

    internal fun clearForTest() {
        preferences.edit().clear().commit()
    }

    companion object {
        const val DEFAULT_COLOR: Int = 0xFF7C5CFF.toInt()
        private const val PREFS = "smartvisor_theme"
        private const val KEY_COLOR = "primary_color"

        fun parseColor(value: String): Int? = try {
            val normalized = value.trim().let { if (it.startsWith("#")) it else "#$it" }
            val parsed = Color.parseColor(normalized)
            parsed or 0xFF000000.toInt()
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}

data class ThemePalette(
    val primary: Int,
    val secondary: Int,
    val surfaceMuted: Int,
    val onPrimary: Int,
    val navigationUnselected: Int,
    val surface: Int = 0xFFFFFFFF.toInt(),
    val background: Int = 0xFFF8F8FD.toInt(),
    val textPrimary: Int = 0xFF231F3A.toInt(),
    val textSecondary: Int = 0xFF565064.toInt()
) {
    companion object {
        fun from(primary: Int): ThemePalette {
            val opaque = primary or 0xFF000000.toInt()
            val luminance = ColorUtils.calculateLuminance(opaque)
            return ThemePalette(
                primary = opaque,
                secondary = ColorUtils.blendARGB(opaque, Color.WHITE, 0.28f),
                surfaceMuted = ColorUtils.blendARGB(opaque, Color.WHITE, 0.88f),
                onPrimary = if (luminance > 0.48) Color.BLACK else Color.WHITE,
                navigationUnselected = 0xFF4F4A5C.toInt()
            )
        }
    }
}