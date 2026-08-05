package com.example.aiassistant.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

object PremiumColors {
    var Primary: Int = AppThemeStore.DEFAULT_COLOR
        private set
    var Secondary: Int = 0xFFA18CFF.toInt()
        private set
    var Background: Int = 0xFFF8F8FD.toInt()
        private set
    var Surface: Int = 0xFFFFFFFF.toInt()
        private set
    var SurfaceMuted: Int = 0xFFF1EEFF.toInt()
        private set
    var TextPrimary: Int = 0xFF231F3A.toInt()
        private set
    var TextSecondary: Int = 0xFF565064.toInt()
        private set
    var NavigationUnselected: Int = 0xFF4F4A5C.toInt()
        private set
    var OnPrimary: Int = 0xFFFFFFFF.toInt()
        private set
    const val Divider: Int = 0xFFE7E3F4.toInt()
    const val Error: Int = 0xFFB91C1C.toInt()

    fun apply(palette: ThemePalette) {
        Primary = palette.primary
        Secondary = palette.secondary
        Background = palette.background
        Surface = palette.surface
        SurfaceMuted = palette.surfaceMuted
        TextPrimary = palette.textPrimary
        TextSecondary = palette.textSecondary
        NavigationUnselected = palette.navigationUnselected
        OnPrimary = palette.onPrimary
    }
}

object PremiumDimens {
    const val ScreenPaddingDp: Int = 20
    const val CardRadiusDp: Int = 26
    const val CardElevationDp: Int = 2
    const val SectionGapDp: Int = 16
    const val TouchTargetDp: Int = 48
}

fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).roundToInt()

fun premiumCard(context: Context): MaterialCardView = MaterialCardView(context).apply {
    radius = context.dp(PremiumDimens.CardRadiusDp).toFloat()
    cardElevation = context.dp(PremiumDimens.CardElevationDp).toFloat()
    setCardBackgroundColor(PremiumColors.Surface)
    strokeWidth = 0
    useCompatPadding = true
}

fun premiumPrimaryButton(context: Context, label: String): MaterialButton =
    MaterialButton(context).apply {
        text = label
        isAllCaps = false
        minHeight = context.dp(PremiumDimens.TouchTargetDp)
        cornerRadius = context.dp(18)
        backgroundTintList = ColorStateList.valueOf(PremiumColors.Primary)
        setTextColor(PremiumColors.OnPrimary)
        textSize = 15f
        setTypeface(typeface, Typeface.BOLD)
        insetTop = 0
        insetBottom = 0
    }

fun premiumSecondaryButton(context: Context, label: String): MaterialButton =
    MaterialButton(context).apply {
        text = label
        isAllCaps = false
        minHeight = context.dp(PremiumDimens.TouchTargetDp)
        cornerRadius = context.dp(18)
        backgroundTintList = ColorStateList.valueOf(PremiumColors.SurfaceMuted)
        setTextColor(PremiumColors.Primary)
        textSize = 15f
        insetTop = 0
        insetBottom = 0
    }

fun premiumSectionTitle(context: Context, label: String): TextView =
    TextView(context).apply {
        text = label
        textSize = 19f
        setTextColor(PremiumColors.TextPrimary)
        setTypeface(typeface, Typeface.BOLD)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
    }

fun premiumBodyText(context: Context, label: String): TextView =
    TextView(context).apply {
        text = label
        textSize = 14f
        setTextColor(PremiumColors.TextSecondary)
        setLineSpacing(0f, 1.15f)
    }
