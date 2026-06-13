package com.blood.unitconverter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive type scale.
 *
 * Key choices:
 *  - Display/headline roles are heavy (Bold/SemiBold) for the big numeric
 *    results and section headers — high emphasis, instantly scannable.
 *  - Every style pins an explicit lineHeight with LineHeightStyle alignment so
 *    text rows have perfectly defined vertical bounds. This is what guarantees
 *    the "Enter value" placeholder and the live input can never overlap or
 *    drift onto a second line unexpectedly.
 */

// Force both trim disabled so glyph asc/descent always fit the declared box.
private val FixedLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

val ExpressiveTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
        lineHeightStyle = FixedLineHeight,
    ),
    displayMedium = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    displaySmall = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    headlineLarge = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    headlineMedium = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    headlineSmall = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    titleLarge = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    titleMedium = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    titleSmall = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    bodyLarge = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    bodyMedium = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    labelLarge = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    labelMedium = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = FixedLineHeight,
    ),
    labelSmall = TextStyle(
        fontFamily = Fredoka,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = FixedLineHeight,
    ),
)

/**
 * Number styles for shifting numeric output (live input echo + results).
 *
 * Fredoka is a proportional font with NON-tabular digits (a "1" is much narrower
 * than a "2"), which would make the result jump around as you type. To keep the
 * layout rock-steady we render shifting numbers in a MONOSPACE family — every
 * digit is the same width, so rows never reflow. Fredoka still styles all the
 * surrounding labels/headers for the friendly, clean look.
 */
val DisplayNumberStyle: TextStyle = ExpressiveTypography.displayMedium.copy(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Bold,
    fontFeatureSettings = "tnum",
    lineHeight = 1.1.em,
)

val InputNumberStyle: TextStyle = ExpressiveTypography.headlineMedium.copy(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.SemiBold,
    fontFeatureSettings = "tnum",
)
