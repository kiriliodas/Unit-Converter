package com.blood.unitconverter.ui.morph

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.blood.unitconverter.ui.theme.Motion

/**
 * A circular/squircle icon button whose silhouette MORPHS from a smooth circle
 * into an 8-point star while pressed, with a springy scale "squeeze". Releasing
 * lets it overshoot back to rest — the signature expressive touch feedback.
 */
@Composable
fun MorphIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    morph: Morph = Morphs.circleToStar,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Spring-driven morph progress: 0 = circle (rest), 1 = star (pressed).
    val progress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        animationSpec = Motion.spatialFast(),
        label = "iconMorph",
    )
    // Independent squeeze so the press feels tactile (slight overshoot on release).
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = Motion.spatialDefault(),
        label = "iconSqueeze",
    )

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = MorphPolygonShape(morph, progress),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

/**
 * A pressable surface that gently squeezes (spring scale) on touch. Used for the
 * pill action buttons and unit pickers to give every tap organic weight without
 * changing their outline shape.
 */
@Composable
fun pressSqueeze(
    interaction: MutableInteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.spatialDefault(),
        label = "pressSqueeze",
    )
    return Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * A decorative morphing badge (circle ⇄ flower) that slowly breathes, used as a
 * lightweight "live / active" accent. Driven by spring-reversed progress passed
 * in by the caller so it stays in sync with app state.
 */
@Composable
fun MorphBadge(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    morph: Morph = Morphs.circleToFlower,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(MorphPolygonShape(morph, progress))
            .background(color),
    )
}
