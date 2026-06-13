package com.blood.unitconverter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blood.unitconverter.data.UnitDef
import com.blood.unitconverter.ui.morph.MorphPolygonShape
import com.blood.unitconverter.ui.morph.Morphs
import com.blood.unitconverter.ui.morph.pressSqueeze
import com.blood.unitconverter.ui.theme.Motion

/**
 * Modal bottom sheet for choosing a unit.
 *
 * Expressive touches:
 *  - Each row springs its press feedback (squeeze) for tactile weight.
 *  - The SELECTED row morphs its silhouette into a soft star and its check mark
 *    pops in with a bouncy spring scale.
 *  - Container color cross-fades calmly between selected / unselected states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPickerSheet(
    expanded: Boolean,
    units: List<UnitDef>,
    selected: UnitDef,
    onSelect: (UnitDef) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!expanded) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Text(
            text = "Select unit",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(units, key = { it.id }) { unit ->
                UnitRow(
                    unit = unit,
                    selected = unit.id == selected.id,
                    onClick = { onSelect(unit) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun UnitRow(
    unit: UnitDef,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }

    // Selected rows morph from a rounded rectangle look into a soft star edge.
    val morphProgress by animateFloatAsState(
        targetValue = if (selected) 0.4f else 0f,
        animationSpec = Motion.spatialDefault(),
        label = "rowMorph",
    )
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = Motion.effects(),
        label = "rowColor",
    )
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val subContent = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = MorphPolygonShape(Morphs.cookieToStar, morphProgress),
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .then(pressSqueeze(interaction, pressedScale = 0.98f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = unit.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = content,
                )
                Text(
                    text = unit.symbol,
                    style = MaterialTheme.typography.labelMedium,
                    color = subContent,
                )
            }
            AnimatedVisibility(
                visible = selected,
                enter = scaleIn(Motion.spatialExpressive(), initialScale = 0.4f) + fadeIn(Motion.effects()),
                exit = scaleOut(Motion.effects(), targetScale = 0.6f) + fadeOut(Motion.effects()),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}
