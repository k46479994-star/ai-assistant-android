package com.example.aiassistant.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

object PremiumColors {
    const val Primary: Int = 0xFF7C5CFF.toInt()
    const val Secondary: Int = 0xFFA18CFF.toInt()
    const val Background: Int = 0xFFF8F8FD.toInt()
    const val Surface: Int = 0xFFFFFFFF.toInt()
    const val SurfaceMuted: Int = 0xFFF1EEFF.toInt()
    const val TextPrimary: Int = 0xFF231F3A.toInt()
    const val TextSecondary: Int = 0xFF68637A.toInt()
    const val Divider: Int = 0xFFE7E3F4.toInt()
    const val Error: Int = 0xFFB91C1C.toInt()
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
        setTextColor(PremiumColors.Surface)
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
