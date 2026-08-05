package com.example.aiassistant.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomNavigationStyleTest {
    @Test
    fun premiumBottomNavigationKeepsLabelsReadableAndInputProminent() {
        assertTrue(BottomNavigationStyle.UnselectedTextColor != PremiumColors.Surface)
        assertTrue(BottomNavigationStyle.BottomPaddingDp >= 20)
        assertTrue(BottomNavigationStyle.InputButtonSizeDp > BottomNavigationStyle.ItemHeightDp)
        assertEquals(12f, BottomNavigationStyle.LabelTextSizeSp)
    }
}
