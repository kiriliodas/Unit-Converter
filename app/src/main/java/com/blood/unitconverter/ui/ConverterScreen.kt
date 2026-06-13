package com.blood.unitconverter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.blood.unitconverter.ConverterViewModel
import com.blood.unitconverter.data.UnitDef
import com.blood.unitconverter.logic.Precision
import com.blood.unitconverter.ui.morph.MorphIconButton
import com.blood.unitconverter.ui.morph.pressSqueeze
import com.blood.unitconverter.ui.theme.DisplayNumberStyle
import com.blood.unitconverter.ui.theme.InputNumberStyle
import com.blood.unitconverter.ui.theme.Motion
import kotlinx.coroutines.launch

@Composable
fun ConverterScreen(vm: ConverterViewModel) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val haptics = LocalHapticFeedback.current

    val precision by vm.precision.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    fun copy(unit: UnitDef, value: String) {
        if (value.isEmpty()) return
        val text = vm.copyText(value, withSymbol = true, symbol = unit.symbol)
        clipboard.setText(AnnotatedString(text))
        vm.saveToHistory()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch { snackbar.showSnackbar("Copied $text") }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Header(
                showTagline = vm.showTagline,
                onHistory = { showHistory = true },
                onSettings = { showSettings = true },
            )

            CategorySelector(vm)

            // Tightly grouped input + unit pickers.
            InputCard(vm, precision, haptics)

            // PRIMARY RESULT AREA — all units live (Google-style). Tap to copy.
            AllResultsList(
                vm = vm,
                precision = precision,
                onCopy = { unit, value -> copy(unit, value) },
                onPickTarget = { unit ->
                    vm.onSelectTo(unit)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Spacer(Modifier.height(4.dp))
        }
    }

    if (showHistory) {
        HistorySheet(
            history = vm.history.collectAsState().value,
            categories = vm.categories,
            onPick = { vm.applyHistory(it); showHistory = false },
            onClear = { vm.clearHistory() },
            onDismiss = { showHistory = false },
        )
    }
    if (showSettings) {
        SettingsSheet(
            precision = precision,
            onPrecision = { vm.onSetPrecision(it) },
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun Header(
    showTagline: Boolean,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "Convert",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (showTagline) {
                Text(
                    text = "Fast · Offline · Private",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HeaderAction(Icons.Rounded.History, "History", onHistory)
        Spacer(Modifier.width(4.dp))
        HeaderAction(Icons.Rounded.Tune, "Settings", onSettings)
    }
}

@Composable
private fun HeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(48.dp) // ≥48dp touch target
            .then(pressSqueeze(interaction)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
        }
    }
}

/** Category chips with a fade gradient on both edges hinting at more tabs. */
@Composable
private fun CategorySelector(vm: ConverterViewModel) {
    val state = rememberLazyListState()
    val edge = MaterialTheme.colorScheme.surface

    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .drawWithContent {
                drawContent()
                val w = 24.dp.toPx()
                // left edge fade (opaque background -> transparent)
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to edge, 1f to Color.Transparent,
                        startX = 0f, endX = w,
                    ),
                    size = androidx.compose.ui.geometry.Size(w, size.height),
                )
                // right edge fade (transparent -> opaque background)
                drawRect(
                    brush = Brush.horizontalGradient(
                        0f to Color.Transparent, 1f to edge,
                        startX = size.width - w, endX = size.width,
                    ),
                    topLeft = Offset(size.width - w, 0f),
                    size = androidx.compose.ui.geometry.Size(w, size.height),
                )
            },
    ) {
        items(vm.categories, key = { it.id }) { category ->
            ExpressiveCategoryChip(
                label = category.displayName,
                selected = category.id == vm.selectedCategory.id,
                onClick = { vm.onSelectCategory(category) },
            )
        }
    }
}

@Composable
private fun ExpressiveCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
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
        shape = RoundedCornerShape(50),
        color = container,
        contentColor = labelColor,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .graphicsLayer { scaleX = selectScale; scaleY = selectScale }
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
 * INPUT CARD — value field (with a persistent unit label shown inline) + the two
 * unit selectors and a large morphing swap button with haptics.
 */
@Composable
private fun InputCard(
    vm: ConverterViewModel,
    precision: Precision,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
) {
    var swapToggles by remember { mutableStateOf(0) }
    val rotation by animateFloatAsState(
        targetValue = swapToggles * 180f,
        animationSpec = Motion.spatialExpressive(),
        label = "swapRotation",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ValueField(
                value = vm.input,
                isError = vm.hasError,
                unitSymbol = vm.fromUnit.symbol,
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
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    size = 56.dp, // ≥48dp touch target
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.CompareArrows,
                        contentDescription = "Swap units",
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = rotation },
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
 *  - Single-line; placeholder + live text share one Box + style (never overlap).
 *  - A PERSISTENT unit symbol stays pinned to the right so the active unit is
 *    always visible inline.
 *  - Negative values are fully supported (the keypad shows '-').
 */
@Composable
private fun ValueField(
    value: String,
    isError: Boolean,
    unitSymbol: String,
    onChange: (String) -> Unit,
) {
    val container by animateColorAsState(
        targetValue = if (isError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = Motion.effects(),
        label = "fieldColor",
    )
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .background(container)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
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
            Spacer(Modifier.width(10.dp))
            Text(
                text = unitSymbol,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
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
        UnitPickerButton(selected = selected, onClick = { expanded = true })
        UnitPickerSheet(
            expanded = expanded,
            units = units,
            selected = selected,
            onSelect = { onSelect(it); expanded = false },
            onDismiss = { expanded = false },
        )
    }
}

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
                .heightIn(min = 56.dp)
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
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = unit.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * ALL-UNITS LIVE LIST — the primary result surface. Every unit in the category
 * is converted from the current From value and shown at once. The chosen TARGET
 * unit is pinned to the TOP, shown big and BLUE. Tapping any row makes it the
 * new target (it animates to the top + turns blue); the copy button copies that
 * row. When there's no input, a hint shows so "0" is never mistaken for a result.
 */
@Composable
private fun AllResultsList(
    vm: ConverterViewModel,
    precision: Precision,
    onCopy: (UnitDef, String) -> Unit,
    onPickTarget: (UnitDef) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = vm.allResults(precision)
    val hasInput = vm.input.isNotBlank() && !vm.hasError

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        if (!hasInput) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .heightIn(min = 120.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (vm.hasError) "Enter a valid number"
                    else "Type a value to see every unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(rows, key = { it.first.id }) { (unit, value, isTarget) ->
                    ResultRow(
                        unit = unit,
                        value = value,
                        isTarget = isTarget,
                        onSelect = { onPickTarget(unit) },
                        onCopy = { onCopy(unit, value) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultRow(
    unit: UnitDef,
    value: String,
    isTarget: Boolean,
    onSelect: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val container by animateColorAsState(
        targetValue = if (isTarget) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = Motion.effects(),
        label = "rowColor",
    )
    val onContainer = if (isTarget) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val onContainerSub = if (isTarget) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onSelect, // tap the row to make it the target (pins + blue)
        interactionSource = interaction,
        shape = RoundedCornerShape(if (isTarget) 22.dp else 16.dp),
        color = container,
        modifier = modifier
            .fillMaxWidth()
            .then(pressSqueeze(interaction, pressedScale = 0.98f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isTarget) 76.dp else 60.dp)
                .padding(horizontal = 18.dp, vertical = if (isTarget) 14.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = unit.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = onContainerSub,
                )
                Box(
                    modifier = Modifier.heightIn(min = if (isTarget) 40.dp else 32.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    AnimatedContent(
                        targetState = value,
                        transitionSpec = {
                            (fadeIn(Motion.effects())).togetherWith(fadeOut(Motion.effects()))
                        },
                        label = "rowValue",
                    ) { v ->
                        Text(
                            text = v,
                            style = if (isTarget) DisplayNumberStyle.copy(
                                fontSize = MaterialTheme.typography.displaySmall.fontSize,
                            ) else InputNumberStyle,
                            color = onContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = unit.symbol,
                style = MaterialTheme.typography.titleSmall,
                color = onContainerSub,
            )
            Spacer(Modifier.width(6.dp))
            // Dedicated copy control (the row tap selects; this copies).
            CopyButton(
                onClick = onCopy,
                tint = onContainerSub,
                description = "Copy ${unit.displayName}",
            )
        }
    }
}

@Composable
private fun CopyButton(onClick: () -> Unit, tint: Color, description: String) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        modifier = Modifier
            .size(40.dp) // ≥40 inner, ample tap target
            .then(pressSqueeze(interaction)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                Icons.Rounded.ContentCopy,
                contentDescription = description,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
