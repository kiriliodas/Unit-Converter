package com.blood.unitconverter.logic

import com.blood.unitconverter.data.UnitDef
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * How many decimals to show in results.
 *
 *  - AUTO   : show enough significant figures to be useful, strip trailing noise.
 *  - FIXED_n: always round to exactly n decimal places (power-user friendly).
 *  - FIXED_0: whole numbers.
 */
enum class Precision(val label: String, val fixedDigits: Int?) {
    FIXED_0("0", 0),
    FIXED_2("2", 2),
    FIXED_4("4", 4),
    FIXED_6("6", 6),
    FIXED_8("8", 8),
    AUTO("Auto", null);

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

    /** Proper typographic minus sign (U+2212) — clearer than a hyphen. */
    const val MINUS = "\u2212"

    /** Convert [value] from [from] to [to] within the same category. */
    fun convert(value: BigDecimal, from: UnitDef, to: UnitDef): BigDecimal {
        if (from.id == to.id) return value
        val base = from.toBase(value)
        return to.fromBase(base)
    }

    /**
     * Parses raw user text into a BigDecimal. Tolerant of:
     *  - leading/trailing spaces and grouping spaces (regular + thin)
     *  - a single leading minus, hyphen or U+2212 (negatives supported, e.g. -40)
     *  - both '.' and ',' as decimal separators
     *  - an empty/partial input (returns null so the UI can show a blank result)
     */
    fun parse(input: String): BigDecimal? {
        val cleaned = input
            .replace("\u2212", "-")   // proper minus -> ascii
            .replace("\u202F", "")    // narrow no-break space
            .replace(" ", "")
            .replace(",", ".")
            .trim()
        if (cleaned.isEmpty() || cleaned == "-" || cleaned == "." || cleaned == "-.") return null
        return try {
            BigDecimal(cleaned)
        } catch (e: NumberFormatException) {
            null
        }
    }

    private val sciSymbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
    }

    /**
     * Formats a result for display.
     *
     *  - Only the INTEGER part is grouped in 3-digit blocks (e.g. 1 234 567.89).
     *    The fractional part is NEVER grouped — mid-decimal spaces are confusing.
     *  - [precision] controls decimals (FIXED rounds exactly; AUTO trims noise).
     *  - Truly extreme magnitudes fall back to scientific notation so the value
     *    is never lost; the UI also auto-shrinks the font before that kicks in.
     *  - Uses a proper minus sign (U+2212).
     */
    fun format(value: BigDecimal, precision: Precision = Precision.AUTO): String {
        if (value.signum() == 0) return "0"

        val abs = value.abs()
        // Only go scientific for genuinely enormous / minuscule magnitudes.
        val useScientific = abs >= BigDecimal("1E18") || abs < BigDecimal("1E-9")
        if (useScientific) {
            val sci = DecimalFormat("0.######E0", sciSymbols)
            return sci.format(value).replace("-", MINUS)
        }

        val scaled: BigDecimal = if (precision.fixedDigits != null) {
            value.setScale(precision.fixedDigits, RoundingMode.HALF_UP)
        } else {
            autoScale(value, abs)
        }

        val plain = scaled.stripTrailingZeros().toPlainString()
        return groupIntegerOnly(plain)
    }

    /**
     * AUTO scaling: ~6 useful fraction digits. For values >= 1 cap at 6 decimal
     * places; for small values keep enough decimals to retain ~6 significant
     * figures so tiny conversions don't collapse to 0.
     */
    private fun autoScale(value: BigDecimal, abs: BigDecimal): BigDecimal {
        val maxDecimals = when {
            abs >= BigDecimal("1") -> 6
            else -> {
                val leadingZeros = -(abs.precision() - abs.scale())
                (leadingZeros + 6).coerceIn(6, 12)
            }
        }
        return value.setScale(maxDecimals, RoundingMode.HALF_UP)
    }

    /** Groups ONLY the integer part in 3-digit blocks; fraction left intact. */
    private fun groupIntegerOnly(plain: String): String {
        val negative = plain.startsWith("-")
        val unsigned = if (negative) plain.substring(1) else plain
        val dot = unsigned.indexOf('.')
        val intPart = if (dot >= 0) unsigned.substring(0, dot) else unsigned
        val fracPart = if (dot >= 0) unsigned.substring(dot) else "" // includes the '.'

        val sb = StringBuilder()
        val n = intPart.length
        for (i in 0 until n) {
            if (i > 0 && (n - i) % 3 == 0) sb.append(' ')
            sb.append(intPart[i])
        }
        sb.append(fracPart)
        return (if (negative) MINUS else "") + sb
    }
}
