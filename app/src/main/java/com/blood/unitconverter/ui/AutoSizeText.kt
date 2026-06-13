package com.blood.unitconverter.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * A single-line Text that automatically shrinks its font size to fit the
 * available width, so long numbers are NEVER truncated. It steps the font size
 * down (keeping line height proportional) until the text fits, down to
 * [minFontSize]. Used for the big result figures.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = style.fontSize * 0.45f,
) {
    BoxWithConstraints(modifier) {
        var scaled by remember(text, style.fontSize, maxWidth) { mutableStateOf(style.fontSize) }
        var ready by remember(text, style.fontSize, maxWidth) { mutableStateOf(false) }

        androidx.compose.material3.Text(
            text = text,
            color = color,
            maxLines = 1,
            softWrap = false,
            style = style.copy(
                fontSize = scaled,
                // keep line height tied to the (shrinking) font size
                lineHeight = scaled * 1.1f,
            ),
            modifier = Modifier,
            onTextLayout = { result: TextLayoutResult ->
                if (!ready) {
                    if (result.didOverflowWidth && scaled.value > minFontSize.value) {
                        // shrink in small steps for a smooth final fit
                        scaled = (scaled.value * 0.94f).coerceAtLeast(minFontSize.value).sp
                    } else {
                        ready = true
                    }
                }
            },
        )
    }
}
