package com.blood.unitconverter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blood.unitconverter.ConverterViewModel
import com.blood.unitconverter.data.UnitDef
import com.blood.unitconverter.ui.morph.MorphBadge
import com.blood.unitconverter.ui.morph.MorphIconButton
import com.blood.unitconverter.ui.morph.MorphPolygonShape
import com.blood.unitconverter.ui.morph.Morphs
import com.blood.unitconverter.ui.morph.pressSqueeze
import com.blood.unitconverter.ui.theme.DisplayNumberStyle
import com.blood.unitconverter.ui.theme.InputNumberStyle
import com.blood.unitconverter.ui.theme.Motion

@Composable
fun ConverterScreen(vm: ConverterViewModel) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Header(active = vm.result.isNotEmpty())

            CategorySelector(vm)

            // Hero feature card: the big result on a morphing expressive shape.
            ResultCard(result = vm.result, unit = vm.toUnit)

            // Tightly grouped input + unit pickers on a single structural surface.
            InputCard(vm)

            Spacer(Modifier.height(4.dp))
            ActionRow(vm)
        }
    }
}

@Composable
private fun Header(active: Boolean) {
    // Slow breathing morph (circle ⇄ flower) that runs while a result is live.
    val breathe = rememberInfiniteTransition(label = "headerBreathe")
    val breatheProgress by breathe.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breatheProgress",
    )
    // When idle, settle the badge into its calm circle (progress 0) via spring.
    val activeProgress by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = Motion.spatialExpressive(),
        label = "headerActive",
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Convert",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Fast · Offline · Private",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        MorphBadge(
            progress = breatheProgress * activeProgress,
            size = 18.dp,
            morph = Morphs.circleToFlower,
        )
    }
}

@Composable
private fun CategorySelector(vm: ConverterViewModel) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        items(vm.categories, key = { it.id }) { category ->
            val selected = category.id == vm.selectedCategory.id
            ExpressiveCategoryChip(
                label = category.displayName,
                selected = selected,
                onClick = { vm.onSelectCategory(category) },
            )
        }
    }
}

/**
 * A category chip that MORPHS its container silhouette (smooth squircle ⇄
 * 8-point star) and spring-scales when it becomes the active selection — the
 * "drag the shape over to the new selection" feel from the spec, plus springy
 * press feedback. Colors cross-fade with a calm (non-bouncy) effect spring.
 */
@Composable
private fun ExpressiveCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }

    // Selected = star silhouette; unselected = smooth squircle. Spring-driven.
    val morphProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = Motion.spatialDefault(),
        label = "chipMorph",
    )
    // Selection pops with a slight overshoot.
    val selectScale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = Motion.spatialExpressive(),
        label = "chipSelectScale",
    )
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = Motion.effects(),
        label = "chipColor",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = Motion.effects(),
        label = "chipLabelColor",
    )

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = MorphPolygonShape(Morphs.cookieToStar, morphProgress),
        color = container,
        contentColor = labelColor,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .graphicsLayer {
                scaleX = selectScale
                scaleY = selectScale
            }
            .then(pressSqueeze(interaction)),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}

/**
 * HERO CARD — the big result.
 *
 * The card keeps a stable Extra-Large (32dp) squircle so wide text content is
 * never clipped, but it spring-breathes a subtle scale when a result appears
 * (organic weight). The figure itself does the expressive "overshoot & squeeze":
 * it spring-slides up and scales past 1.0 before settling. AnimatedContent keeps
 * the previous number from jumping while the new one arrives.
 *
 * A small decorative morphing accent (squircle ⇄ star) sits beside the unit
 * label to carry the shape-morph language without endangering the layout.
 */
@Composable
private fun ResultCard(result: String, unit: UnitDef) {
    val hasResult = result.isNotEmpty()

    // Gentle spring "breath" of the whole card when a value is present.
    val cardScale by animateFloatAsState(
        targetValue = if (hasResult) 1f else 0.985f,
        animationSpec = Motion.spatialExpressive(),
        label = "cardBreath",
    )
    // Accent dot morph progress: squircle (rest) -> star (active).
    val accentMorph by animateFloatAsState(
        targetValue = if (hasResult) 1f else 0f,
        animationSpec = Motion.spatialDefault(),
        label = "accentMorph",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 150.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            },
        shape = MaterialTheme.shapes.extraLarge, // stable 32dp squircle
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 28.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(MorphPolygonShape(Morphs.cookieToStar, accentMorph))
                        .background(MaterialTheme.colorScheme.primary),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = unit.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                AnimatedContent(
                    targetState = result,
                    transitionSpec = {
                        // Overshoot & squeeze: spring-scale + spring-slide in,
                        // calm fade out of the old value.
                        (scaleIn(Motion.spatialExpressive(), initialScale = 0.7f) +
                            slideInVertically(Motion.spatialDefault()) { it / 3 } +
                            fadeIn(Motion.effects()))
                            .togetherWith(
                                scaleOut(Motion.effects(), targetScale = 0.95f) +
                                    fadeOut(Motion.effects())
                            )
                    },
                    label = "result",
                ) { value ->
                    Text(
                        text = value.ifEmpty { "0" },
                        style = DisplayNumberStyle,
                        color = if (value.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = unit.symbol,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * INPUT CARD — groups the value field and the two unit selectors tightly. The
 * central control is now a MORPHING swap button (circle ⇄ star on press) that
 * also spins 180° with a bouncy spring whenever units are swapped.
 */
@Composable
private fun InputCard(vm: ConverterViewModel) {
    var swapToggles by remember { mutableStateOf(0) }
    val rotation by animateFloatAsState(
        targetValue = swapToggles * 180f,
        animationSpec = Motion.spatialExpressive(),
        label = "swapRotation",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // 28dp
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ValueField(
                value = vm.input,
                isError = vm.hasError,
                onChange = vm::onInputChange,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                UnitColumn(
                    label = "From",
                    units = vm.selectedCategory.units,
                    selected = vm.fromUnit,
                    onSelect = vm::onSelectFrom,
                    modifier = Modifier.weight(1f),
                )
                MorphIconButton(
                    onClick = {
                        vm.onSwap()
                        swapToggles++
                    },
                    size = 52.dp,
                ) {
                    Icon(
                        Icons.Rounded.SwapVert,
                        contentDescription = "Swap units",
                        modifier = Modifier.graphicsLayer { rotationZ = rotation },
                    )
                }
                UnitColumn(
                    label = "To",
                    units = vm.selectedCategory.units,
                    selected = vm.toUnit,
                    onSelect = vm::onSelectTo,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * The value input.
 *
 * BUG-PREVENTION DETAILS (unchanged guarantees):
 *  - Single-line; placeholder and live text share one Box + identical style, so
 *    they can never overlap or wrap.
 *  - Fixed min height reserves vertical space.
 *
 * Added expressive touch: on a parse error the field gives a small spring
 * "shake"/squeeze and its container color animates calmly to the error tone.
 */
@Composable
private fun ValueField(
    value: String,
    isError: Boolean,
    onChange: (String) -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (isError)
            MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = Motion.effects(),
        label = "fieldColor",
    )
    // Spring squeeze nudge when entering the error state.
    val errorScale by animateFloatAsState(
        targetValue = if (isError) 1.02f else 1f,
        animationSpec = Motion.spatialExpressive(),
        label = "fieldErrorScale",
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = container,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = errorScale },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(container)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Enter value",
                    style = InputNumberStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                maxLines = 1,
                textStyle = InputNumberStyle.copy(
                    color = if (isError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun UnitColumn(
    label: String,
    units: List<UnitDef>,
    selected: UnitDef,
    onSelect: (UnitDef) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
        )
        UnitPickerButton(
            selected = selected,
            onClick = { expanded = true },
        )
        UnitPickerSheet(
            expanded = expanded,
            units = units,
            selected = selected,
            onSelect = {
                onSelect(it)
                expanded = false
            },
            onDismiss = { expanded = false },
        )
    }
}

/** Unit picker button with springy press feedback and a swap-driven value
 *  cross-fade so the chosen unit changes feel animated, not abrupt. */
@Composable
private fun UnitPickerButton(selected: UnitDef, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .fillMaxWidth()
            .then(pressSqueeze(interaction, pressedScale = 0.97f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    (slideInVertically(Motion.spatialDefault()) { it / 2 } + fadeIn(Motion.effects()))
                        .togetherWith(fadeOut(Motion.effects()))
                },
                label = "unitLabel",
                modifier = Modifier.weight(1f),
            ) { unit ->
                Column {
                    Text(
                        text = unit.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = unit.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(vm: ConverterViewModel) {
    val clipboard = LocalClipboardManager.current
    val clearInteraction = remember { MutableInteractionSource() }
    val copyInteraction = remember { MutableInteractionSource() }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Clear (pill, tonal) — springy press squeeze.
        Surface(
            onClick = vm::onClear,
            interactionSource = clearInteraction,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .weight(1f)
                .then(pressSqueeze(clearInteraction)),
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Clear", style = MaterialTheme.typography.labelLarge)
            }
        }
        // Copy result (pill, primary) — springy press squeeze.
        Surface(
            onClick = {
                if (vm.result.isNotEmpty()) {
                    clipboard.setText(AnnotatedString(vm.result))
                }
            },
            interactionSource = copyInteraction,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .weight(1f)
                .then(pressSqueeze(copyInteraction)),
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = 52.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Copy", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
