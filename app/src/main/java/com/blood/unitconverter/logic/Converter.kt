package com.blood.unitconverter.logic

import com.blood.unitconverter.data.UnitDef
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Pure conversion + formatting helpers. No Android dependencies so this is
 * trivially unit-testable and fast.
 */
object Converter {

    /** Convert [value] from [from] to [to] within the same category. */
    fun convert(value: BigDecimal, from: UnitDef, to: UnitDef): BigDecimal {
        if (from.id == to.id) return value
        val base = from.toBase(value)
        return to.fromBase(base)
    }

    /**
     * Parses raw user text into a BigDecimal. Tolerant of:
     *  - leading/trailing spaces
     *  - a single leading minus
     *  - both '.' and ',' as decimal separators (locale friendliness)
     *  - an empty/partial input (returns null so the UI can show a blank result)
     */
    fun parse(input: String): BigDecimal? {
        val cleaned = input.trim().replace(",", ".")
        if (cleaned.isEmpty() || cleaned == "-" || cleaned == "." || cleaned == "-.") return null
        return try {
            BigDecimal(cleaned)
        } catch (e: NumberFormatException) {
            null
        }
    }

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ' ' // thin, calm grouping for big numbers
        decimalSeparator = '.'
    }

    /**
     * Formats a result for display. Goals:
     *  - Up to [maxFractionDigits] significant fraction digits, trailing zeros
     *    stripped so the value reads cleanly.
     *  - Falls back to scientific notation for extreme magnitudes so the text
     *    can never explode the layout width.
     */
    fun format(value: BigDecimal, maxFractionDigits: Int = 8): String {
        if (value.signum() == 0) return "0"

        val abs = value.abs()
        val useScientific = abs >= BigDecimal("1E15") || abs < BigDecimal("1E-9")

        if (useScientific) {
            val sci = DecimalFormat("0.######E0", symbols)
            return sci.format(value)
        }

        val scaled = value.setScale(maxFractionDigits, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        // stripTrailingZeros can yield "1E+1" style for whole numbers; toPlainString fixes it.
        val plain = scaled.toPlainString()

        // Apply calm grouping only to the integer part.
        return groupInteger(plain)
    }

    private fun groupInteger(plain: String): String {
        val negative = plain.startsWith("-")
        val unsigned = if (negative) plain.substring(1) else plain
        val dot = unsigned.indexOf('.')
        val intPart = if (dot >= 0) unsigned.substring(0, dot) else unsigned
        val fracPart = if (dot >= 0) unsigned.substring(dot) else ""

        val grouped = StringBuilder()
        val len = intPart.length
        for (i in 0 until len) {
            if (i > 0 && (len - i) % 3 == 0) grouped.append(' ')
            grouped.append(intPart[i])
        }
        return (if (negative) "-" else "") + grouped + fracPart
    }
}
