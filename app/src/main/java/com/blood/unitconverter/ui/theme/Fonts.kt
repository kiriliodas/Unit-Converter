package com.blood.unitconverter.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.blood.unitconverter.R

/**
 * Fredoka — the app's primary typeface. Rounded, friendly, clean. Bundled as
 * static weights (SIL OFL 1.1) so it works 100% offline. Used everywhere except
 * shifting numbers, which use a monospace family for stable digit widths.
 */
val Fredoka = FontFamily(
    Font(R.font.fredoka_regular, FontWeight.Normal),
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)
