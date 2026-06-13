package com.blood.unitconverter.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
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
import androidx.compose.ui.focus.focusRequester
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

    val context = androidx.compose.ui.platform.LocalContext.current
    val precision by vm.precision.collectAsState()
    val scientific by vm.scientific.collectAsState()
    val favorites by vm.favorites.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var sharePreview by remember { mutableStateOf<String?>(null) }

    // Shared list state so we can scroll the results back to the top whenever a
    // unit picker is dismissed (the user expects to see the input + top result).
    val resultsListState = rememberLazyListState()

    fun copy(unit: UnitDef, value: String) {
        if (value.isEmpty()) return
        val text = vm.copyText(value, withSymbol = true, symbol = unit.symbol)
        clipboard.setText(AnnotatedString(text))
        vm.saveToHistory()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch { snackbar.showSnackbar("Copied $text") }
    }

    fun scrollTop() {
        scope.launch { resultsListState.animateScrollToItem(0) }
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
                historyCount = vm.history.collectAsState().value.size,
                onShare = {
                    val text = vm.shareText(precision, scientific)
                    if (text.isBlank()) {
                        scope.launch { snackbar.showSnackbar("Type a value to share") }
                    } else {
                        sharePreview = text // show preview before sending
                    }
                },
                onHistory = { showHistory = true },
                onSettings = { showSettings = true },
            )

            CategorySelector(vm)

            // Tightly grouped input + unit pickers.
            InputCard(vm, haptics, onPickerDismiss = { scrollTop() })

            // PRIMARY RESULT AREA — all units live. Tap = retarget, long-press =
            // use as input, star = pin, copy icon = copy.
            AllResultsList(
                vm = vm,
                precision = precision,
                scientific = scientific,
                favorites = favorites,
                listState = resultsListState,
                onCopy = { unit, value -> copy(unit, value) },
                onPickTarget = { unit ->
                    vm.onSelectTo(unit)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    scrollTop()
                },
                onReverse = { unit, value ->
                    vm.setAsInput(unit, value)
                    scrollTop()
                    scope.launch { snackbar.showSnackbar("Set ${unit.displayName} as input") }
                },
                onToggleFav = { unit ->
                    vm.toggleFavorite(unit)
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
            scientific = scientific,
            onPrecision = { vm.onSetPrecision(it) },
            onScientific = { vm.onSetScientific(it) },
            onDismiss = { showSettings = false },
        )
    }
    sharePreview?.let { preview ->
        SharePreviewDialog(
            text = preview,
            onShare = {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, preview)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Share conversion"))
                sharePreview = null
            },
            onCopy = {
                clipboard.setText(AnnotatedString(preview))
                sharePreview = null
                scope.launch { snackbar.showSnackbar("Copied to clipboard") }
            },
            onDismiss = { sharePreview = null },
        )
    }
}

@Composable
private fun Header(
    showTagline: Boolean,
    historyCount: Int,
    onShare: () -> Unit,
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
        HeaderAction(Icons.Rounded.IosShare, "Share conversion", onShare)
        Spacer(Modifier.width(4.dp))
        HeaderAction(Icons.Rounded.History, "History", onHistory, badge = historyCount)
        Spacer(Modifier.width(4.dp))
        HeaderAction(Icons.Rounded.Tune, "Settings", onSettings)
    }
}

@Composable
private fun HeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    badge: Int = 0,
) {
    val interaction = remember { MutableInteractionSource() }
    Box {
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
        if (badge > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .heightIn(min = 18.dp)
                    .widthIn(min = 18.dp),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badge > 99) "99+" else badge.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }
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
 * INPUT CARD — no more redundant "From" row. The value field's unit chip IS the
 * From picker (tap it). Below: a single "To" selector with the swap button, so
 * each piece of info appears exactly once.
 */
@Composable
private fun InputCard(
    vm: ConverterViewModel,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onPickerDismiss: () -> Unit,
) {
    var swapToggles by remember { mutableStateOf(0) }
    val rotation by animateFloatAsState(
        targetValue = swapToggles * 180f,
        animationSpec = Motion.spatialExpressive(),
        label = "swapRotation",
    )
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

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
                onUnitClick = { fromExpanded = true },
            )
            Spacer(Modifier.height(12.dp))

            // Grouped "Show result in" sub-card: label + [swap] + [To selector].
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = "Highlight result in",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Swap, vertically centered with the To selector.
                        MorphIconButton(
                            onClick = {
                                vm.onSwap()
                                swapToggles++
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            size = 52.dp,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.CompareArrows,
                                contentDescription = "Swap from and to units",
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer { rotationZ = rotation },
                            )
                        }
                        UnitPickerButton(
                            selected = vm.toUnit,
                            onClick = { toExpanded = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    // From picker — scroll results to top on dismiss.
    UnitPickerSheet(
        expanded = fromExpanded,
        units = vm.selectedCategory.units,
        selected = vm.fromUnit,
        otherUnit = vm.toUnit,
        otherLabel = "To",
        onSelect = { vm.onSelectFrom(it); fromExpanded = false; onPickerDismiss() },
        onDismiss = { fromExpanded = false; onPickerDismiss() },
    )
    // To picker.
    UnitPickerSheet(
        expanded = toExpanded,
        units = vm.selectedCategory.units,
        selected = vm.toUnit,
        otherUnit = vm.fromUnit,
        otherLabel = "From",
        onSelect = { vm.onSelectTo(it); toExpanded = false; onPickerDismiss() },
        onDismiss = { toExpanded = false; onPickerDismiss() },
    )
}

/**
 * The value input.
 *  - Single-line; placeholder + live text share one Box + style (never overlap).
 *  - The unit chip on the right is the TAPPABLE "From" picker (chevron hint), so
 *    the unit is shown once and is also the affordance to change it.
 *  - Negatives supported.
 */
@Composable
private fun ValueField(
    value: String,
    isError: Boolean,
    unitSymbol: String,
    onChange: (String) -> Unit,
    onUnitClick: () -> Unit,
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

    // Auto-focus the field + open the keyboard on first launch.
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

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
                .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            Spacer(Modifier.width(8.dp))
            // Tappable From-unit chip with a chevron, so it reads as actionable.
            val chipInteraction = remember { MutableInteractionSource() }
            Surface(
                onClick = onUnitClick,
                interactionSource = chipInteraction,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.then(pressSqueeze(chipInteraction)),
            ) {
                Row(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .padding(start = 14.dp, end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = unitSymbol,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        Icons.Rounded.ArrowDropDown,
                        contentDescription = "Change input unit",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun UnitPickerButton(
    selected: UnitDef,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
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
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Change unit",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
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
    scientific: Boolean,
    favorites: Set<String>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onCopy: (UnitDef, String) -> Unit,
    onPickTarget: (UnitDef) -> Unit,
    onReverse: (UnitDef, String) -> Unit,
    onToggleFav: (UnitDef) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = vm.resultItems(precision, scientific, favorites)
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
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(
                    items,
                    key = { item ->
                        when (item) {
                            is ConverterViewModel.ResultItem.Header -> "header:${item.title}"
                            is ConverterViewModel.ResultItem.Row -> "row:${item.data.unit.id}"
                        }
                    },
                ) { item ->
                    when (item) {
                        is ConverterViewModel.ResultItem.Header -> SectionHeader(
                            title = item.title,
                            modifier = Modifier.animateItem(),
                        )
                        is ConverterViewModel.ResultItem.Row -> ResultRow(
                            row = item.data,
                            onSelect = { onPickTarget(item.data.unit) },
                            onReverse = { onReverse(item.data.unit, item.data.value) },
                            onCopy = { onCopy(item.data.unit, item.data.value) },
                            onToggleFav = { onToggleFav(item.data.unit) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = 10.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (title == "Starred") {
            Icon(
                Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ResultRow(
    row: ConverterViewModel.RowData,
    onSelect: () -> Unit,
    onReverse: () -> Unit,
    onCopy: () -> Unit,
    onToggleFav: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isTarget = row.isTarget
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
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
        shape = RoundedCornerShape(if (isTarget) 22.dp else 16.dp),
        color = container,
        modifier = modifier
            .fillMaxWidth()
            .then(pressSqueeze(interaction, pressedScale = 0.98f))
            .combinedClickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(),
                onClick = onSelect, // tap = make it the target (pins + blue)
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onReverse() // long-press = use this value+unit as new input
                },
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (isTarget) 76.dp else 60.dp)
                .padding(
                    start = 18.dp, end = 6.dp,
                    top = if (isTarget) 14.dp else 10.dp,
                    bottom = if (isTarget) 14.dp else 10.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.unit.displayName,
                    style = MaterialTheme.typography.titleSmall, // bolder label, faster scan
                    color = onContainerSub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (isTarget) 44.dp else 34.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    AutoSizeText(
                        text = row.value,
                        style = if (isTarget) DisplayNumberStyle.copy(
                            fontSize = MaterialTheme.typography.displaySmall.fontSize,
                        ) else InputNumberStyle,
                        color = onContainer,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = row.unit.symbol,
                style = MaterialTheme.typography.titleSmall,
                color = onContainerSub,
                maxLines = 1,
            )
            IconToggle(
                filled = row.isFavorite,
                tint = onContainerSub,
                description = if (row.isFavorite) "Unpin ${row.unit.displayName}" else "Pin ${row.unit.displayName}",
                iconFilled = Icons.Rounded.Star,
                iconOutline = Icons.Rounded.StarOutline,
                onClick = onToggleFav,
            )
            CopyButton(onClick = onCopy, tint = onContainerSub, description = "Copy ${row.unit.displayName}")
        }
    }
}

@Composable
private fun IconToggle(
    filled: Boolean,
    tint: Color,
    description: String,
    iconFilled: androidx.compose.ui.graphics.vector.ImageVector,
    iconOutline: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    // Springy pop when the filled state flips (star ☆ → ★).
    val pop by animateFloatAsState(
        targetValue = if (filled) 1f else 0f,
        animationSpec = Motion.spatialExpressive(),
        label = "starPop",
    )
    val scale = 1f + 0.25f * pop * (1f - pop) * 4f // peaks mid-transition
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        modifier = Modifier
            .size(40.dp)
            .then(pressSqueeze(interaction)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                if (filled) iconFilled else iconOutline,
                contentDescription = description,
                tint = if (filled) MaterialTheme.colorScheme.primary else tint,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale },
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
