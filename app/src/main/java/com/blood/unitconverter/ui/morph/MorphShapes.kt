package com.blood.unitconverter.ui.morph

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

/**
 * Expressive polygons used by the morphing icon button.
 *
 * Each shape is built around the unit circle (radius 1, centered at origin) with
 * smoothed corners so straight edges blend continuously into the curves.
 */
private object Shapes {

    /** 8-pointed star — the energetic "pressed" target. */
    fun star8(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = 8,
        innerRadius = 0.72f,
        rounding = CornerRounding(0.18f, smoothing = 0.8f),
        radius = 1f,
    )

    /** Near-circular smooth polygon — the calm resting shape. */
    fun circle(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = 12,
        innerRadius = 0.985f,
        rounding = CornerRounding(1f, smoothing = 1f),
        radius = 1f,
    )
}

/**
 * A Compose [Shape] that renders a [Morph] at a given [progress] (0f..1f).
 *
 * The polygon is scaled UNIFORMLY (by the smaller dimension) and centered, so
 * the round silhouette is never stretched into a flat "lens" on non-square
 * components. Intended for roughly square components (icon buttons).
 */
class MorphPolygonShape(
    private val morph: Morph,
    private val progress: Float,
) : Shape {

    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        // graphics-shapes emits a path roughly in [-1, 1] centered at origin.
        val path: Path = morph.toPath(progress).asComposePath()
        matrix.reset()
        val r = minOf(size.width, size.height) / 2f
        matrix.translate(size.width / 2f, size.height / 2f)
        matrix.scale(r, r)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

/** Shared cached morph so it isn't rebuilt on every recomposition. */
object Morphs {
    val circleToStar: Morph by lazy { Morph(Shapes.circle(), Shapes.star8()) }
}
