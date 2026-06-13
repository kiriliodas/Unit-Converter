package com.blood.unitconverter.data

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * A single unit.
 *
 * Most units convert through a single base unit via a linear factor:
 *   baseValue = value * factor
 *   value     = baseValue / factor
 *
 * Affine units (temperature) need a more general transform, so each unit can
 * optionally override [toBase] / [fromBase]. The factor path is kept because it
 * is allocation-free and extremely fast for the overwhelmingly common case.
 */
data class UnitDef(
    val id: String,
    val displayName: String,
    val symbol: String,
    /** Multiplier that turns this unit into the category's base unit. */
    val factor: BigDecimal = BigDecimal.ONE,
    private val toBaseFn: ((BigDecimal) -> BigDecimal)? = null,
    private val fromBaseFn: ((BigDecimal) -> BigDecimal)? = null,
) {
    fun toBase(value: BigDecimal): BigDecimal =
        toBaseFn?.invoke(value) ?: value.multiply(factor, MC)

    fun fromBase(base: BigDecimal): BigDecimal =
        fromBaseFn?.invoke(base) ?: base.divide(factor, MC)

    companion object {
        /** Generous precision so chained conversions never visibly drift. */
        val MC: MathContext = MathContext(34, RoundingMode.HALF_UP)
    }
}

/** A group of inter-convertible units, e.g. Length or Temperature. */
data class UnitCategory(
    val id: String,
    val displayName: String,
    val iconKey: String,
    val units: List<UnitDef>,
    val defaultFromId: String,
    val defaultToId: String,
)

private fun bd(s: String): BigDecimal = BigDecimal(s)

/**
 * The full catalog. Factors are expressed relative to a single SI-ish base unit
 * per category (metre, kilogram, second, ...). Values are exact where possible.
 */
object UnitCatalog {

    val categories: List<UnitCategory> = listOf(
        length(),
        mass(),
        temperature(),
        dataStorage(),
        area(),
        volume(),
        speed(),
        time(),
        pressure(),
        energy(),
        power(),
        angle(),
        frequency(),
        fuelEconomy(),
    )

    // ---- Length (base: metre) ------------------------------------------------
    private fun length() = UnitCategory(
        id = "length", displayName = "Length", iconKey = "length",
        defaultFromId = "m", defaultToId = "ft",
        units = listOf(
            UnitDef("nm", "Nanometre", "nm", bd("0.000000001")),
            UnitDef("um", "Micrometre", "μm", bd("0.000001")),
            UnitDef("mm", "Millimetre", "mm", bd("0.001")),
            UnitDef("cm", "Centimetre", "cm", bd("0.01")),
            UnitDef("dm", "Decimetre", "dm", bd("0.1")),
            UnitDef("m", "Metre", "m", bd("1")),
            UnitDef("km", "Kilometre", "km", bd("1000")),
            UnitDef("in", "Inch", "in", bd("0.0254")),
            UnitDef("ft", "Foot", "ft", bd("0.3048")),
            UnitDef("yd", "Yard", "yd", bd("0.9144")),
            UnitDef("mi", "Mile", "mi", bd("1609.344")),
            UnitDef("nmi", "Nautical mile", "nmi", bd("1852")),
            UnitDef("ly", "Light year", "ly", bd("9460730472580800")),
            UnitDef("au", "Astronomical unit", "AU", bd("149597870700")),
            UnitDef("pc", "Parsec", "pc", bd("30856775814913673")),
        )
    )

    // ---- Mass (base: kilogram) -----------------------------------------------
    private fun mass() = UnitCategory(
        id = "mass", displayName = "Weight", iconKey = "mass",
        defaultFromId = "kg", defaultToId = "lb",
        units = listOf(
            UnitDef("ng", "Nanogram", "ng", bd("0.000000000001")),
            UnitDef("ug", "Microgram", "μg", bd("0.000000001")),
            UnitDef("mg", "Milligram", "mg", bd("0.000001")),
            UnitDef("g", "Gram", "g", bd("0.001")),
            UnitDef("kg", "Kilogram", "kg", bd("1")),
            UnitDef("t", "Tonne", "t", bd("1000")),
            UnitDef("oz", "Ounce", "oz", bd("0.028349523125")),
            UnitDef("lb", "Pound", "lb", bd("0.45359237")),
            UnitDef("st", "Stone", "st", bd("6.35029318")),
            UnitDef("ton_us", "US ton", "ton", bd("907.18474")),
            UnitDef("ton_uk", "Imperial ton", "long ton", bd("1016.0469088")),
            UnitDef("ct", "Carat", "ct", bd("0.0002")),
        )
    )

    // ---- Temperature (affine, base: Kelvin) ----------------------------------
    private fun temperature() = UnitCategory(
        id = "temperature", displayName = "Temperature", iconKey = "temperature",
        defaultFromId = "c", defaultToId = "f",
        units = listOf(
            UnitDef("c", "Celsius", "°C",
                toBaseFn = { it.add(bd("273.15")) },
                fromBaseFn = { it.subtract(bd("273.15")) }),
            UnitDef("f", "Fahrenheit", "°F",
                toBaseFn = { it.subtract(bd("32")).multiply(bd("5"), UnitDef.MC).divide(bd("9"), UnitDef.MC).add(bd("273.15")) },
                fromBaseFn = { it.subtract(bd("273.15")).multiply(bd("9"), UnitDef.MC).divide(bd("5"), UnitDef.MC).add(bd("32")) }),
            UnitDef("k", "Kelvin", "K",
                toBaseFn = { it },
                fromBaseFn = { it }),
            UnitDef("r", "Rankine", "°R",
                toBaseFn = { it.multiply(bd("5"), UnitDef.MC).divide(bd("9"), UnitDef.MC) },
                fromBaseFn = { it.multiply(bd("9"), UnitDef.MC).divide(bd("5"), UnitDef.MC) }),
        )
    )

    // ---- Digital storage (base: byte). Uses binary (IEC) + decimal (SI). -----
    private fun dataStorage() = UnitCategory(
        id = "data", displayName = "Data", iconKey = "data",
        defaultFromId = "mb", defaultToId = "mib",
        units = listOf(
            UnitDef("bit", "Bit", "bit", bd("0.125")),
            UnitDef("byte", "Byte", "B", bd("1")),
            UnitDef("kb", "Kilobyte", "kB", bd("1000")),
            UnitDef("mb", "Megabyte", "MB", bd("1000000")),
            UnitDef("gb", "Gigabyte", "GB", bd("1000000000")),
            UnitDef("tb", "Terabyte", "TB", bd("1000000000000")),
            UnitDef("pb", "Petabyte", "PB", bd("1000000000000000")),
            UnitDef("kib", "Kibibyte", "KiB", bd("1024")),
            UnitDef("mib", "Mebibyte", "MiB", bd("1048576")),
            UnitDef("gib", "Gibibyte", "GiB", bd("1073741824")),
            UnitDef("tib", "Tebibyte", "TiB", bd("1099511627776")),
            UnitDef("pib", "Pebibyte", "PiB", bd("1125899906842624")),
        )
    )

    // ---- Area (base: square metre) -------------------------------------------
    private fun area() = UnitCategory(
        id = "area", displayName = "Area", iconKey = "area",
        defaultFromId = "m2", defaultToId = "ft2",
        units = listOf(
            UnitDef("mm2", "Square millimetre", "mm²", bd("0.000001")),
            UnitDef("cm2", "Square centimetre", "cm²", bd("0.0001")),
            UnitDef("m2", "Square metre", "m²", bd("1")),
            UnitDef("ha", "Hectare", "ha", bd("10000")),
            UnitDef("km2", "Square kilometre", "km²", bd("1000000")),
            UnitDef("in2", "Square inch", "in²", bd("0.00064516")),
            UnitDef("ft2", "Square foot", "ft²", bd("0.09290304")),
            UnitDef("yd2", "Square yard", "yd²", bd("0.83612736")),
            UnitDef("ac", "Acre", "ac", bd("4046.8564224")),
            UnitDef("mi2", "Square mile", "mi²", bd("2589988.110336")),
        )
    )

    // ---- Volume (base: litre) ------------------------------------------------
    private fun volume() = UnitCategory(
        id = "volume", displayName = "Volume", iconKey = "volume",
        defaultFromId = "l", defaultToId = "gal_us",
        units = listOf(
            UnitDef("ml", "Millilitre", "mL", bd("0.001")),
            UnitDef("l", "Litre", "L", bd("1")),
            UnitDef("m3", "Cubic metre", "m³", bd("1000")),
            UnitDef("cm3", "Cubic centimetre", "cm³", bd("0.001")),
            UnitDef("tsp", "Teaspoon (US)", "tsp", bd("0.00492892159375")),
            UnitDef("tbsp", "Tablespoon (US)", "tbsp", bd("0.01478676478125")),
            UnitDef("floz_us", "Fluid ounce (US)", "fl oz", bd("0.0295735295625")),
            UnitDef("cup_us", "Cup (US)", "cup", bd("0.2365882365")),
            UnitDef("pt_us", "Pint (US)", "pt", bd("0.473176473")),
            UnitDef("qt_us", "Quart (US)", "qt", bd("0.946352946")),
            UnitDef("gal_us", "Gallon (US)", "gal", bd("3.785411784")),
            UnitDef("gal_uk", "Gallon (UK)", "gal", bd("4.54609")),
        )
    )

    // ---- Speed (base: metre/second) ------------------------------------------
    private fun speed() = UnitCategory(
        id = "speed", displayName = "Speed", iconKey = "speed",
        defaultFromId = "kmh", defaultToId = "mph",
        units = listOf(
            UnitDef("ms", "Metre/second", "m/s", bd("1")),
            UnitDef("kmh", "Kilometre/hour", "km/h", bd("0.277777777777777778")),
            UnitDef("mph", "Mile/hour", "mph", bd("0.44704")),
            UnitDef("fts", "Foot/second", "ft/s", bd("0.3048")),
            UnitDef("kn", "Knot", "kn", bd("0.514444444444444444")),
            UnitDef("mach", "Mach (sea level)", "Ma", bd("340.29")),
        )
    )

    // ---- Time (base: second) -------------------------------------------------
    private fun time() = UnitCategory(
        id = "time", displayName = "Time", iconKey = "time",
        defaultFromId = "min", defaultToId = "s",
        units = listOf(
            UnitDef("ns", "Nanosecond", "ns", bd("0.000000001")),
            UnitDef("us", "Microsecond", "μs", bd("0.000001")),
            UnitDef("ms", "Millisecond", "ms", bd("0.001")),
            UnitDef("s", "Second", "s", bd("1")),
            UnitDef("min", "Minute", "min", bd("60")),
            UnitDef("h", "Hour", "h", bd("3600")),
            UnitDef("d", "Day", "d", bd("86400")),
            UnitDef("wk", "Week", "wk", bd("604800")),
            UnitDef("mo", "Month (30d)", "mo", bd("2592000")),
            UnitDef("yr", "Year (365d)", "yr", bd("31536000")),
        )
    )

    // ---- Pressure (base: pascal) ---------------------------------------------
    private fun pressure() = UnitCategory(
        id = "pressure", displayName = "Pressure", iconKey = "pressure",
        defaultFromId = "bar", defaultToId = "psi",
        units = listOf(
            UnitDef("pa", "Pascal", "Pa", bd("1")),
            UnitDef("kpa", "Kilopascal", "kPa", bd("1000")),
            UnitDef("bar", "Bar", "bar", bd("100000")),
            UnitDef("mbar", "Millibar", "mbar", bd("100")),
            UnitDef("atm", "Atmosphere", "atm", bd("101325")),
            UnitDef("psi", "Pound/inch²", "psi", bd("6894.757293168")),
            UnitDef("torr", "Torr", "Torr", bd("133.322368421")),
            UnitDef("mmhg", "mmHg", "mmHg", bd("133.322387415")),
        )
    )

    // ---- Energy (base: joule) ------------------------------------------------
    private fun energy() = UnitCategory(
        id = "energy", displayName = "Energy", iconKey = "energy",
        defaultFromId = "kcal", defaultToId = "kj",
        units = listOf(
            UnitDef("j", "Joule", "J", bd("1")),
            UnitDef("kj", "Kilojoule", "kJ", bd("1000")),
            UnitDef("cal", "Calorie", "cal", bd("4.184")),
            UnitDef("kcal", "Kilocalorie", "kcal", bd("4184")),
            UnitDef("wh", "Watt hour", "Wh", bd("3600")),
            UnitDef("kwh", "Kilowatt hour", "kWh", bd("3600000")),
            UnitDef("ev", "Electronvolt", "eV", bd("0.0000000000000000001602176634")),
            UnitDef("btu", "BTU", "BTU", bd("1055.05585262")),
            UnitDef("ftlb", "Foot pound", "ft·lb", bd("1.3558179483314")),
        )
    )

    // ---- Power (base: watt) --------------------------------------------------
    private fun power() = UnitCategory(
        id = "power", displayName = "Power", iconKey = "power",
        defaultFromId = "hp", defaultToId = "kw",
        units = listOf(
            UnitDef("w", "Watt", "W", bd("1")),
            UnitDef("kw", "Kilowatt", "kW", bd("1000")),
            UnitDef("mw", "Megawatt", "MW", bd("1000000")),
            UnitDef("hp", "Horsepower (mech)", "hp", bd("745.69987158227022")),
            UnitDef("hp_m", "Horsepower (metric)", "PS", bd("735.49875")),
            UnitDef("btu_h", "BTU/hour", "BTU/h", bd("0.29307107017")),
        )
    )

    // ---- Angle (base: degree) ------------------------------------------------
    private fun angle() = UnitCategory(
        id = "angle", displayName = "Angle", iconKey = "angle",
        defaultFromId = "deg", defaultToId = "rad",
        units = listOf(
            UnitDef("deg", "Degree", "°", bd("1")),
            UnitDef("rad", "Radian", "rad", bd("57.29577951308232087680")),
            UnitDef("grad", "Gradian", "grad", bd("0.9")),
            UnitDef("arcmin", "Arcminute", "'", bd("0.016666666666666667")),
            UnitDef("arcsec", "Arcsecond", "\"", bd("0.000277777777777778")),
            UnitDef("turn", "Turn", "turn", bd("360")),
        )
    )

    // ---- Frequency (base: hertz) ---------------------------------------------
    private fun frequency() = UnitCategory(
        id = "frequency", displayName = "Frequency", iconKey = "frequency",
        defaultFromId = "mhz", defaultToId = "ghz",
        units = listOf(
            UnitDef("hz", "Hertz", "Hz", bd("1")),
            UnitDef("khz", "Kilohertz", "kHz", bd("1000")),
            UnitDef("mhz", "Megahertz", "MHz", bd("1000000")),
            UnitDef("ghz", "Gigahertz", "GHz", bd("1000000000")),
            UnitDef("rpm", "Revolutions/min", "rpm", bd("0.016666666666666667")),
        )
    )

    // ---- Fuel economy (base: L/100km equivalent via L per km) -----------------
    // Note: mpg <-> L/100km is reciprocal, so we model the base as litres per
    // metre and supply custom transforms for the mpg variants.
    private fun fuelEconomy() = UnitCategory(
        id = "fuel", displayName = "Fuel", iconKey = "fuel",
        defaultFromId = "l100km", defaultToId = "mpg_us",
        units = listOf(
            // base unit: litres per metre
            UnitDef("l100km", "L/100km", "L/100km",
                toBaseFn = { it.divide(bd("100000"), UnitDef.MC) },
                fromBaseFn = { it.multiply(bd("100000"), UnitDef.MC) }),
            UnitDef("kml", "km/L", "km/L",
                toBaseFn = { if (it.signum() == 0) BigDecimal.ZERO else BigDecimal.ONE.divide(it.multiply(bd("1000"), UnitDef.MC), UnitDef.MC) },
                fromBaseFn = { if (it.signum() == 0) BigDecimal.ZERO else BigDecimal.ONE.divide(it.multiply(bd("1000"), UnitDef.MC), UnitDef.MC) }),
            UnitDef("mpg_us", "MPG (US)", "mpg",
                toBaseFn = { if (it.signum() == 0) BigDecimal.ZERO else bd("0.00235214583").divide(it, UnitDef.MC) },
                fromBaseFn = { if (it.signum() == 0) BigDecimal.ZERO else bd("0.00235214583").divide(it, UnitDef.MC) }),
            UnitDef("mpg_uk", "MPG (UK)", "mpg",
                toBaseFn = { if (it.signum() == 0) BigDecimal.ZERO else bd("0.00282481").divide(it, UnitDef.MC) },
                fromBaseFn = { if (it.signum() == 0) BigDecimal.ZERO else bd("0.00282481").divide(it, UnitDef.MC) }),
        )
    )
}
