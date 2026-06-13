package com.blood.unitconverter

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.blood.unitconverter.data.HistoryEntry
import com.blood.unitconverter.data.LastSelection
import com.blood.unitconverter.data.SettingsRepository
import com.blood.unitconverter.data.UnitCatalog
import com.blood.unitconverter.data.UnitCategory
import com.blood.unitconverter.data.UnitDef
import com.blood.unitconverter.logic.Converter
import com.blood.unitconverter.logic.Precision
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * Holds all converter UI state. Conversion math is pure/in-memory; preferences
 * and history are persisted ON-DEVICE ONLY via [SettingsRepository].
 */
class ConverterViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app)

    val categories: List<UnitCategory> = UnitCatalog.categories

    var selectedCategory by mutableStateOf(categories.first())
        private set
    var fromUnit by mutableStateOf(unitOf(selectedCategory, selectedCategory.defaultFromId))
        private set
    var toUnit by mutableStateOf(unitOf(selectedCategory, selectedCategory.defaultToId))
        private set
    var input by mutableStateOf("")
        private set

    // ---- Persisted preferences exposed as Compose-friendly flows -------------

    val precision: StateFlow<Precision> =
        repo.precision.stateIn(viewModelScope, SharingStarted.Eagerly, Precision.AUTO)

    val scientific: StateFlow<Boolean> =
        repo.scientific.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /** Snapshotted once at launch: true only on the very first session. */
    var showTagline by mutableStateOf(true)
        private set

    val history: StateFlow<List<HistoryEntry>> =
        repo.history.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Favorite unit keys ("categoryId:unitId"). */
    val favorites: StateFlow<Set<String>> =
        repo.favorites.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    private fun favKey(unit: UnitDef) = "${selectedCategory.id}:${unit.id}"

    init {
        // Restore last-used category/units + tagline visibility on launch.
        viewModelScope.launch {
            val sel = repo.lastSelection.first()
            if (sel != null) applyLastSelection(sel)

            // Read the tagline flag once for THIS session, then hide it for next time.
            showTagline = repo.showTagline.first()
            if (showTagline) repo.setShowTagline(false)
        }
    }

    // ---- Derived results -----------------------------------------------------

    /** The single primary result (From → To). Pass the observed precision so
     *  the UI recomposes when the user changes the decimal-places setting. */
    fun result(p: Precision = precision.value): String {
        val parsed = Converter.parse(input) ?: return ""
        return Converter.format(Converter.convert(parsed, fromUnit, toUnit), p, scientific.value)
    }

    val hasError: Boolean
        get() = input.isNotBlank() && Converter.parse(input) == null

    /** One row in the all-units result list. */
    data class RowData(
        val unit: UnitDef,
        val value: String,
        val isTarget: Boolean,
        val isFavorite: Boolean,
    )

    /** A list item: either a section header or a result row. */
    sealed interface ResultItem {
        data class Header(val title: String) : ResultItem
        data class Row(val data: RowData) : ResultItem
    }

    /**
     * The all-units list, grouped into sections:
     *   [Target row] → "Starred" header + starred rows → "All units" header + rest.
     * Headers are omitted when a section is empty.
     */
    fun resultItems(p: Precision, sci: Boolean, favs: Set<String>): List<ResultItem> {
        val parsed = Converter.parse(input) ?: return emptyList()
        val rows = selectedCategory.units.map { unit ->
            RowData(
                unit = unit,
                value = Converter.format(Converter.convert(parsed, fromUnit, unit), p, sci),
                isTarget = unit.id == toUnit.id,
                isFavorite = favKey(unit) in favs,
            )
        }
        val target = rows.firstOrNull { it.isTarget }
        val starred = rows.filter { it.isFavorite && !it.isTarget }
        val rest = rows.filter { !it.isFavorite && !it.isTarget }

        val items = mutableListOf<ResultItem>()
        target?.let { items += ResultItem.Row(it) }
        if (starred.isNotEmpty()) {
            items += ResultItem.Header("Starred")
            starred.forEach { items += ResultItem.Row(it) }
        }
        if (rest.isNotEmpty()) {
            if (starred.isNotEmpty() || target != null) items += ResultItem.Header("All units")
            rest.forEach { items += ResultItem.Row(it) }
        }
        return items
    }

    /** Clipboard text — number only, with an ASCII sign and optional symbol. */
    fun copyText(value: String, withSymbol: Boolean, symbol: String): String {
        val number = value.replace(",", "").replace(Converter.MINUS, "-")
        return if (withSymbol) "$number $symbol" else number
    }

    /** A multi-line snapshot of the current conversion for the Share sheet. */
    fun shareText(p: Precision, sci: Boolean): String {
        val parsed = Converter.parse(input) ?: return ""
        val header = "${input.replace(Converter.MINUS, "-")} ${fromUnit.symbol} ="
        val lines = selectedCategory.units
            .filter { it.id != fromUnit.id }
            .joinToString("\n") { u ->
                val v = Converter.format(Converter.convert(parsed, fromUnit, u), p, sci)
                    .replace(",", "").replace(Converter.MINUS, "-")
                "  $v ${u.symbol}  (${u.displayName})"
            }
        return "$header\n$lines\n— Converter (offline)"
    }

    fun toggleFavorite(unit: UnitDef) {
        viewModelScope.launch { repo.toggleFavorite(favKey(unit)) }
    }

    /**
     * Reverse lookup: make [unit] the new From, with the value it currently
     * shows. We RECOMPUTE the plain number from the source input (rather than
     * parsing the formatted string) so it works even in scientific mode.
     */
    fun setAsInput(unit: UnitDef, @Suppress("UNUSED_PARAMETER") value: String) {
        val parsed = Converter.parse(input) ?: return
        val converted = Converter.convert(parsed, fromUnit, unit)
        // Plain, parseable representation (no grouping, ascii minus).
        input = converted.stripTrailingZeros().toPlainString()
        fromUnit = unit
        persistSelection()
    }

    // ---- Events --------------------------------------------------------------

    fun onInputChange(new: String) {
        input = new.filter { it.isDigit() || it == '.' || it == ',' || it == '-' || it == '\u2212' }
    }

    fun onSelectCategory(category: UnitCategory) {
        if (category.id == selectedCategory.id) return
        selectedCategory = category
        fromUnit = unitOf(category, category.defaultFromId)
        toUnit = unitOf(category, category.defaultToId)
        persistSelection()
    }

    fun onSelectFrom(unit: UnitDef) { fromUnit = unit; persistSelection() }
    fun onSelectTo(unit: UnitDef) { toUnit = unit; persistSelection() }

    fun onSwap() {
        val f = fromUnit; fromUnit = toUnit; toUnit = f
        persistSelection()
    }

    fun onClear() { input = "" }

    fun onSetPrecision(p: Precision) {
        viewModelScope.launch { repo.setPrecision(p) }
    }

    fun onSetScientific(on: Boolean) {
        viewModelScope.launch { repo.setScientific(on) }
    }

    /** Persist the current conversion to history (called on copy / explicit save). */
    fun saveToHistory() {
        val r = result()
        if (input.isBlank() || r.isEmpty()) return
        val entry = HistoryEntry(
            categoryId = selectedCategory.id,
            fromUnitId = fromUnit.id,
            toUnitId = toUnit.id,
            inputText = input,
            resultText = r,
            timestamp = System.currentTimeMillis(),
        )
        viewModelScope.launch { repo.addHistory(entry) }
    }

    fun applyHistory(entry: HistoryEntry) {
        val category = categories.firstOrNull { it.id == entry.categoryId } ?: return
        selectedCategory = category
        fromUnit = unitOf(category, entry.fromUnitId)
        toUnit = unitOf(category, entry.toUnitId)
        input = entry.inputText
        persistSelection()
    }

    fun clearHistory() {
        viewModelScope.launch { repo.clearHistory() }
    }

    // ---- Helpers -------------------------------------------------------------

    private fun applyLastSelection(sel: LastSelection) {
        val category = categories.firstOrNull { it.id == sel.categoryId } ?: return
        selectedCategory = category
        fromUnit = category.units.firstOrNull { it.id == sel.fromUnitId }
            ?: unitOf(category, category.defaultFromId)
        toUnit = category.units.firstOrNull { it.id == sel.toUnitId }
            ?: unitOf(category, category.defaultToId)
    }

    private fun persistSelection() {
        viewModelScope.launch {
            repo.setLastSelection(
                LastSelection(selectedCategory.id, fromUnit.id, toUnit.id)
            )
        }
    }

    private fun unitOf(category: UnitCategory, id: String): UnitDef =
        category.units.first { it.id == id }
}
