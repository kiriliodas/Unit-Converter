package com.blood.unitconverter.ui.morph

import androidx.compose.ui.geometry.Rect
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
 * Catalog of expressive polygons used across the app.
 *
 * Every shape is built around the unit circle (radius 1, centered at 0,0) with
 * fully/strongly smoothed corners so straight edges blend continuously into the
 * curves — the "squircle / cookie / blob" look, not hard circular rounding.
 */
object Shapes {

    /** Soft 4-sided "cookie" squircle — the resting state for most surfaces. */
    fun cookie4(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = 4,
        innerRadius = 0.86f,
        rounding = CornerRounding(0.45f, smoothing = 1f),
        radius = 1f,
    )

    /** Pebble-like 6-sided blob for a gently organic resting state. */
    fun blob6(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = 6,
        innerRadius = 0.9f,
        rounding = CornerRounding(0.5f, smoothing = 1f),
        radius = 1f,
    )

    /** 8-pointed star — energetic "active / pressed" target. */
    fun star8(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = 8,
        innerRadius = 0.72f,
        rounding = CornerRounding(0.18f, smoothing = 0.8f),
        radius = 1f,
    )

    /** 12-pointed flower — dramatic accent target for hero interactions. */
    fun flower12(): RoundedPolygon = RoundedPolygon.star(
        numVerticesPerRadius = 12,
        innerRadius = 0.82f,
        rounding = CornerRounding(0.12f, smoothing = 0.8f),
        radius = 1f,
    )

    /** Near-circular smooth polygon — calm icon-button resting shape. */
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
 * Because it implements [Shape], it can be passed to ANY component's `shape`
 * parameter (Surface, Card, Button, IconButton, clip, etc.). That means real,
 * interactive components actually clip to the morphing silhouette and animate
 * elastically — the geometry isn't just painted decoration.
 *
 * The morph path is built on the unit square and then scaled/translated to fill
 * the component bounds, so it works at any size.
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
        // graphics-shapes emits a path roughly in [-1, 1]; normalize to [0, 1]
        // then scale to the component's pixel size.
        val path: Path = morph.toPath(progress).asComposePath()
        matrix.reset()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

/**
 * A static (non-morphing) polygon as a Compose [Shape], handy for resting
 * states where we still want the squircle silhouette without animation.
 */
class RoundedPolygonShape(
    private val polygon: RoundedPolygon,
) : Shape {

    private val matrix = Matrix()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = polygon.toPath().asComposePath()
        matrix.reset()
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)
        path.transform(matrix)
        return Outline.Generic(path)
    }
}

/** Shared cached morphs so we don't rebuild them every recomposition. */
object Morphs {
    val cookieToStar: Morph by lazy { Morph(Shapes.cookie4(), Shapes.star8()) }
    val circleToStar: Morph by lazy { Morph(Shapes.circle(), Shapes.star8()) }
    val circleToFlower: Morph by lazy { Morph(Shapes.circle(), Shapes.flower12()) }
    val blobToFlower: Morph by lazy { Morph(Shapes.blob6(), Shapes.flower12()) }
}
