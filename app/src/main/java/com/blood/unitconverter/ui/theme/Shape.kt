package com.blood.unitconverter.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive shape scale.
 *
 * We deliberately push the large end of the scale: feature cards use generous
 * 28-32dp Extra Large rounding, while structural buttons elsewhere lean on
 * fully-rounded pill shapes (applied per-component). This breaks away from the
 * old uniform 4/8/12 rounding to feel modern and expressive.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),        // major surfaces
    extraLarge = RoundedCornerShape(32.dp),   // hero feature card
)
