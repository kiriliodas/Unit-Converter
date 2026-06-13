package com.blood.unitconverter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.blood.unitconverter.data.UnitCatalog
import com.blood.unitconverter.data.UnitCategory
import com.blood.unitconverter.data.UnitDef
import com.blood.unitconverter.logic.Converter

/**
 * Holds all converter UI state. Pure in-memory, no persistence, no I/O — which
 * is exactly what the privacy-first, instant-launch philosophy demands.
 */
class ConverterViewModel : ViewModel() {

    val categories: List<UnitCategory> = UnitCatalog.categories

    var selectedCategory by mutableStateOf(categories.first())
        private set

    var fromUnit by mutableStateOf(unitOf(selectedCategory, selectedCategory.defaultFromId))
        private set

    var toUnit by mutableStateOf(unitOf(selectedCategory, selectedCategory.defaultToId))
        private set

    /** Raw text the user typed. Kept as the single source of truth. */
    var input by mutableStateOf("")
        private set

    /** Formatted output string, recomputed on every state change. */
    val result: String
        get() {
            val parsed = Converter.parse(input) ?: return ""
            val converted = Converter.convert(parsed, fromUnit, toUnit)
            return Converter.format(converted)
        }

    /** Whether the current input is non-empty but unparseable. */
    val hasError: Boolean
        get() = input.isNotBlank() && Converter.parse(input) == null

    fun onInputChange(new: String) {
        // Allow only characters that can form a number, keeping typing instant.
        val filtered = new.filter { it.isDigit() || it == '.' || it == ',' || it == '-' }
        input = filtered
    }

    fun onSelectCategory(category: UnitCategory) {
        if (category.id == selectedCategory.id) return
        selectedCategory = category
        fromUnit = unitOf(category, category.defaultFromId)
        toUnit = unitOf(category, category.defaultToId)
    }

    fun onSelectFrom(unit: UnitDef) { fromUnit = unit }
    fun onSelectTo(unit: UnitDef) { toUnit = unit }

    fun onSwap() {
        val f = fromUnit
        fromUnit = toUnit
        toUnit = f
    }

    fun onClear() { input = "" }

    private fun unitOf(category: UnitCategory, id: String): UnitDef =
        category.units.first { it.id == id }
}
