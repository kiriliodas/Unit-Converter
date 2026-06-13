package com.blood.unitconverter.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * Material 3 Expressive Motion Scheme.
 *
 * Per the Expressive spec, motion is driven by SPRING PHYSICS (mass / stiffness
 * / damping) rather than fixed cubic-bezier easing. Springs give the
 * "overshoot & squeeze" cadence — the value can rush toward the target, sail
 * slightly past it, then settle back with organic, rubbery elasticity.
 *
 * We expose a small set of named springs so every animated surface in the app
 * shares one coherent, tuned feel:
 *
 *  - [spatial*]  — for things that move / resize / morph (position, size, shape).
 *                  Lower stiffness + a touch of bounce = visible elasticity.
 *  - [effects*]  — for non-spatial properties (alpha, color). Critically damped,
 *                  no bounce, so fades stay calm and don't shimmer.
 */
object Motion {

    // ---- Spatial springs (bouncy, expressive) --------------------------------

    /** Default expressive spring for most morph / resize interactions. */
    fun <T> spatialDefault(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.65f,                 // slightly underdamped → gentle overshoot
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Quick, snappy spring for small, frequent touches (chips, buttons). */
    fun <T> spatialFast(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.6f,
        stiffness = Spring.StiffnessMedium,
    )

    /** Slow, dramatic spring for big hero transitions (the result morph). */
    fun <T> spatialExpressive(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.55f,                 // more bounce
        stiffness = Spring.StiffnessLow,
    )

    // ---- Effect springs (calm, no bounce) ------------------------------------

    /** Critically damped spring for fades / color so they never wobble. */
    fun <T> effects(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )
}
