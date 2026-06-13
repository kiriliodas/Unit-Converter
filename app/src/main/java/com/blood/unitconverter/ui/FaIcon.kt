package com.blood.unitconverter.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.blood.unitconverter.ui.theme.FontAwesome

/**
 * Renders a Font Awesome glyph as an "icon". Icon fonts are drawn as text, so
 * this wraps a [Text] with the [FontAwesome] family and exposes a size + tint
 * matching the Material `Icon` API for drop-in use.
 *
 * CENTERING: a plain Text glyph carries asymmetric font padding + leading, which
 * pushes the glyph off-center inside a circular button. We neutralize that by:
 *  - disabling includeFontPadding,
 *  - setting lineHeight == fontSize with LineHeightStyle Trim.Both + Center,
 *  - centering the text horizontally.
 * The result is a tight, optically-centered glyph box, so the icon sits dead
 * center in any parent that centers it.
 */
@Composable
fun FaIcon(
    glyph: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: TextUnit = 20.sp,
    tint: Color = LocalContentColor.current,
) {
    Text(
        text = glyph,
        color = tint,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = FontAwesome,
            fontWeight = FontWeight.Black,
            fontSize = size,
            lineHeight = size,
            platformStyle = PlatformTextStyle(includeFontPadding = false),
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        ),
        modifier = if (contentDescription != null) {
            modifier.semantics { this.contentDescription = contentDescription }
        } else modifier,
    )
}
