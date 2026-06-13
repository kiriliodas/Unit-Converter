package com.blood.unitconverter.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.blood.unitconverter.R

/**
 * Fredoka — the app's primary typeface. Rounded, friendly, clean. Bundled as
 * static weights (OFL licensed) so it works 100% offline. Used everywhere.
 */
val Fredoka = FontFamily(
    Font(R.font.fredoka_regular, FontWeight.Normal),
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
    Font(R.font.fredoka_bold, FontWeight.Bold),
)

/**
 * Font Awesome 6 Free (Solid) — subset to only the glyphs the app uses, so it
 * adds just a few KB. Bundled offline. Attribution: Font Awesome Free,
 * https://fontawesome.com (Icons: CC BY 4.0, Fonts: SIL OFL 1.1).
 */
val FontAwesome = FontFamily(
    Font(R.font.fa_solid, FontWeight.Black),
)

/**
 * Font Awesome glyph codepoints (subset). Render with [FontAwesome] family.
 */
object FaIcons {
    const val SWAP = "\uf362"        // right-left
    const val SWAP_ALT = "\uf0ec"    // arrow-right-arrow-left
    const val COPY = "\uf0c5"        // copy
    const val HISTORY = "\uf1da"     // clock-rotate-left
    const val SETTINGS = "\uf1de"    // sliders
    const val SEARCH = "\uf002"      // magnifying-glass
    const val TRASH = "\uf2ed"       // trash-can
    const val CHECK = "\uf00c"       // check
    const val CLOSE = "\uf00d"       // xmark
    const val GEAR = "\uf013"        // gear
    const val RULER = "\uf545"       // ruler
}
