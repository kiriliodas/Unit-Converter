package com.blood.unitconverter.logic

import com.blood.unitconverter.data.UnitDef
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * How many decimals to show in results.
 *
 *  - AUTO   : show enough significant figures to be useful, strip noise.
 *  - FIXED_n: always round to exactly n decimal places (power-user friendly).
 */
enum class Precision(val label: String, val fixedDigits: Int?) {
    AUTO("Auto", null),
    FIXED_2("2", 2),
    FIXED_4("4", 4),
    FIXED_6("6", 6),
    FIXED_8("8", 8);

    companion object {
        fun fromName(name: String?): Precision =
            entries.firstOrNull { it.name == name } ?: AUTO
    }
}

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
     *  - a single leading minus (negatives ARE supported, e.g. -40)
     *  - both '.' and ',' as decimal separators (locale friendliness)
     *  - grouping spaces the formatter may have inserted
     *  - an empty/partial input (returns null so the UI can show a blank result)
     */
    fun parse(input: String): BigDecimal? {
        val cleaned = input.trim().replace(" ", "").replace(",", ".")
        if (cleaned.isEmpty() || cleaned == "-" || cleaned == "." || cleaned == "-.") return null
        return try {
            BigDecimal(cleaned)
        } catch (e: NumberFormatException) {
            null
        }
    }

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ' '
        decimalSeparator = '.'
    }

    /**
     * Formats a result for display.
     *
     *  - Scientific notation for extreme magnitudes so text never explodes width.
     *  - [precision] controls decimals: AUTO trims noise while keeping small
     *    numbers meaningful (significant-figure aware); FIXED rounds to exactly
     *    that many places.
     *  - Both integer AND fraction parts are grouped in calm 3-digit blocks,
     *    e.g. 47.683 715 82 — easy to scan, never jittery (tabular figures).
     */
    fun format(value: BigDecimal, precision: Precision = Precision.AUTO): String {
        if (value.signum() == 0) return "0"

        val abs = value.abs()
        val useScientific = abs >= BigDecimal("1E15") || abs < BigDecimal("1E-9")
        if (useScientific) {
            val sci = DecimalFormat("0.######E0", symbols)
            return sci.format(value)
        }

        val scaled: BigDecimal = if (precision.fixedDigits != null) {
            value.setScale(precision.fixedDigits, RoundingMode.HALF_UP)
        } else {
            autoScale(value, abs)
        }

        val plain = scaled.stripTrailingZeros().toPlainString()
        return group(plain)
    }

    /**
     * AUTO scaling: aim for ~8 useful digits total. For values >= 1 we cap at 6
     * decimal places; for small values we keep enough decimals to retain ~6
     * significant figures so tiny conversions don't collapse to 0.
     */
    private fun autoScale(value: BigDecimal, abs: BigDecimal): BigDecimal {
        val maxDecimals = when {
            abs >= BigDecimal("1") -> 6
            else -> {
                // leading zeros after the decimal point + 6 sig figs
                val leadingZeros = -(abs.precision() - abs.scale()) // ~ -floor(log10)
                (leadingZeros + 6).coerceIn(6, 12)
            }
        }
        return value.setScale(maxDecimals, RoundingMode.HALF_UP)
    }

    /** Groups BOTH integer and fraction parts in 3-digit blocks with spaces. */
    private fun group(plain: String): String {
        val negative = plain.startsWith("-")
        val unsigned = if (negative) plain.substring(1) else plain
        val dot = unsigned.indexOf('.')
        val intPart = if (dot >= 0) unsigned.substring(0, dot) else unsigned
        val fracPart = if (dot >= 0) unsigned.substring(dot + 1) else ""

        val sb = StringBuilder()
        // integer: group from the right
        val n = intPart.length
        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) sb.append(' ')
            sb.append(intPart[i])
        }
        // fraction: group from the left
        if (fracPart.isNotEmpty()) {
            sb.append('.')
            for (i in fracPart.indices) {
                if (i > 0 && i % 3 == 0) sb.append(' ')
                sb.append(fracPart[i])
            }
        }
        return (if (negative) "-" else "") + sb
    }
}
