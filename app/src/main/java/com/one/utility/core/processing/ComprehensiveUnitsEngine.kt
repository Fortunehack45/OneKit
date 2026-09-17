package com.one.utility.core.processing

data class UnitDefinition(
    val id: String,
    val name: String,
    val symbol: String,
    val factorToBase: Double // Multiply by this factor to convert to SI base unit
)

data class UnitCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val baseUnitName: String,
    val units: List<UnitDefinition>
)

class ComprehensiveUnitsEngine {

    val categories: List<UnitCategory> = listOf(
        // 1. Length & Astronomy
        UnitCategory(
            id = "length_astronomy",
            name = "Length & Astronomy",
            iconName = "astronomy",
            baseUnitName = "Meter",
            units = listOf(
                UnitDefinition("m", "Meter", "m", 1.0),
                UnitDefinition("km", "Kilometer", "km", 1000.0),
                UnitDefinition("cm", "Centimeter", "cm", 0.01),
                UnitDefinition("mm", "Millimeter", "mm", 0.001),
                UnitDefinition("um", "Micrometer", "µm", 1e-6),
                UnitDefinition("nm", "Nanometer", "nm", 1e-9),
                UnitDefinition("pm", "Picometer", "pm", 1e-12),
                UnitDefinition("angstrom", "Angstrom", "Å", 1e-10),
                UnitDefinition("planck_len", "Planck Length", "ℓP", 1.616255e-35),
                UnitDefinition("mi", "Mile", "mi", 1609.344),
                UnitDefinition("yd", "Yard", "yd", 0.9144),
                UnitDefinition("ft", "Foot", "ft", 0.3048),
                UnitDefinition("in", "Inch", "in", 0.0254),
                UnitDefinition("nmi", "Nautical Mile", "NM", 1852.0),
                UnitDefinition("fathom", "Fathom", "ftm", 1.8288),
                UnitDefinition("rod", "Rod / Pole", "rd", 5.0292),
                UnitDefinition("furlong", "Furlong", "fur", 201.168),
                UnitDefinition("league", "Statute League", "lea", 4828.032),
                UnitDefinition("au", "Astronomical Unit", "AU", 1.495978707e11),
                UnitDefinition("light_year", "Light-Year", "ly", 9.460730472e15),
                UnitDefinition("light_hour", "Light-Hour", "lh", 1.0792528488e12),
                UnitDefinition("light_minute", "Light-Minute", "lm", 1.798754748e10),
                UnitDefinition("light_second", "Light-Second", "ls", 2.99792458e8),
                UnitDefinition("parsec", "Parsec", "pc", 3.085677581e16),
                UnitDefinition("kiloparsec", "Kiloparsec", "kpc", 3.085677581e19),
                UnitDefinition("megaparsec", "Megaparsec", "Mpc", 3.085677581e22),
                UnitDefinition("lunar_dist", "Lunar Distance", "LD", 3.84402e8),
                UnitDefinition("earth_radius", "Earth Equatorial Radius", "R⊕", 6.3781e6),
                UnitDefinition("sun_radius", "Solar Radius", "R☉", 6.957e8)
            )
        ),

        // 2. Mass & Chemistry
        UnitCategory(
            id = "mass_chemistry",
            name = "Mass & Chemistry",
            iconName = "chemistry",
            baseUnitName = "Kilogram",
            units = listOf(
                UnitDefinition("kg", "Kilogram", "kg", 1.0),
                UnitDefinition("g", "Gram", "g", 1e-3),
                UnitDefinition("mg", "Milligram", "mg", 1e-6),
                UnitDefinition("ug", "Microgram", "µg", 1e-9),
                UnitDefinition("t", "Metric Ton", "t", 1000.0),
                UnitDefinition("lb", "Pound (Avoirdupois)", "lbs", 0.45359237),
                UnitDefinition("oz", "Ounce", "oz", 0.028349523125),
                UnitDefinition("stone", "Stone", "st", 6.35029318),
                UnitDefinition("us_ton", "US Short Ton", "ton", 907.18474),
                UnitDefinition("imp_ton", "Imperial Long Ton", "long ton", 1016.0469088),
                UnitDefinition("carat", "Carat", "ct", 0.0002),
                UnitDefinition("grain", "Grain", "gr", 0.00006479891),
                UnitDefinition("troy_oz", "Troy Ounce", "oz t", 0.0311034768),
                UnitDefinition("dalton", "Dalton / Atomic Mass (AMU)", "Da", 1.6605390666e-27),
                UnitDefinition("slug", "Slug", "slug", 14.5939029),
                UnitDefinition("planck_mass", "Planck Mass", "mP", 2.176434e-8),
                UnitDefinition("solar_mass", "Solar Mass", "M☉", 1.98847e30),
                UnitDefinition("earth_mass", "Earth Mass", "M⊕", 5.9722e24),
                UnitDefinition("electron_mass", "Electron Rest Mass", "me", 9.1093837e-31),
                UnitDefinition("proton_mass", "Proton Rest Mass", "mp", 1.6726219e-27)
            )
        ),

        // 3. Digital & Computer Science
        UnitCategory(
            id = "digital_cs",
            name = "Digital & Computer Science",
            iconName = "computer",
            baseUnitName = "Byte",
            units = listOf(
                UnitDefinition("b", "Bit", "b", 0.125),
                UnitDefinition("B", "Byte", "B", 1.0),
                UnitDefinition("kb", "Kilobit (Decimal)", "kb", 125.0),
                UnitDefinition("KB", "Kilobyte (Decimal)", "KB", 1e3),
                UnitDefinition("mb", "Megabit (Decimal)", "Mb", 125000.0),
                UnitDefinition("MB", "Megabyte (Decimal)", "MB", 1e6),
                UnitDefinition("gb", "Gigabit (Decimal)", "Gb", 1.25e8),
                UnitDefinition("GB", "Gigabyte (Decimal)", "GB", 1e9),
                UnitDefinition("tb", "Terabit (Decimal)", "Tb", 1.25e11),
                UnitDefinition("TB", "Terabyte (Decimal)", "TB", 1e12),
                UnitDefinition("PB", "Petabyte (Decimal)", "PB", 1e15),
                UnitDefinition("EB", "Exabyte (Decimal)", "EB", 1e18),
                UnitDefinition("ZB", "Zettabyte (Decimal)", "ZB", 1e21),
                UnitDefinition("YB", "Yottabyte (Decimal)", "YB", 1e24),
                UnitDefinition("KiB", "Kibibyte (Binary 1024)", "KiB", 1024.0),
                UnitDefinition("MiB", "Mebibyte (Binary 1024²)", "MiB", 1048576.0),
                UnitDefinition("GiB", "Gibibyte (Binary 1024³)", "GiB", 1073741824.0),
                UnitDefinition("TiB", "Tebibyte (Binary 1024⁴)", "TiB", 1099511627776.0),
                UnitDefinition("PiB", "Pebibyte (Binary 1024⁵)", "PiB", 1125899906842624.0),
                UnitDefinition("nibble", "Nibble (4 bits)", "nibble", 0.5)
            )
        ),

        // 4. Energy & Physics
        UnitCategory(
            id = "energy_physics",
            name = "Energy & Physics",
            iconName = "energy",
            baseUnitName = "Joule",
            units = listOf(
                UnitDefinition("J", "Joule", "J", 1.0),
                UnitDefinition("kJ", "Kilojoule", "kJ", 1000.0),
                UnitDefinition("MJ", "Megajoule", "MJ", 1e6),
                UnitDefinition("GJ", "Gigajoule", "GJ", 1e9),
                UnitDefinition("cal", "Gram Calorie", "cal", 4.184),
                UnitDefinition("kcal", "Food Calorie (kcal)", "kcal", 4184.0),
                UnitDefinition("Wh", "Watt-hour", "Wh", 3600.0),
                UnitDefinition("kWh", "Kilowatt-hour", "kWh", 3.6e6),
                UnitDefinition("MWh", "Megawatt-hour", "MWh", 3.6e9),
                UnitDefinition("eV", "Electron-volt", "eV", 1.602176634e-19),
                UnitDefinition("keV", "Kilo-electron-volt", "keV", 1.602176634e-16),
                UnitDefinition("MeV", "Mega-electron-volt", "MeV", 1.602176634e-13),
                UnitDefinition("GeV", "Giga-electron-volt", "GeV", 1.602176634e-10),
                UnitDefinition("TeV", "Tera-electron-volt", "TeV", 1.602176634e-7),
                UnitDefinition("btu", "British Thermal Unit", "BTU", 1055.06),
                UnitDefinition("therm", "US Therm", "thm", 105480400.0),
                UnitDefinition("ft_lb", "Foot-pound", "ft⋅lbf", 1.3558179483314),
                UnitDefinition("erg", "Erg", "erg", 1e-7),
                UnitDefinition("tnt_ton", "Ton of TNT", "tTNT", 4.184e9),
                UnitDefinition("rydberg", "Rydberg Constant Energy", "Ry", 2.179872e-18),
                UnitDefinition("hartree", "Hartree Energy", "Eh", 4.359744e-18)
            )
        ),

        // 5. Pressure & Mechanics
        UnitCategory(
            id = "pressure_mechanics",
            name = "Pressure & Vacuum",
            iconName = "pressure",
            baseUnitName = "Pascal",
            units = listOf(
                UnitDefinition("Pa", "Pascal", "Pa", 1.0),
                UnitDefinition("kPa", "Kilopascal", "kPa", 1e3),
                UnitDefinition("MPa", "Megapascal", "MPa", 1e6),
                UnitDefinition("GPa", "Gigapascal", "GPa", 1e9),
                UnitDefinition("bar", "Bar", "bar", 1e5),
                UnitDefinition("mbar", "Millibar", "mbar", 100.0),
                UnitDefinition("psi", "Pounds per Square Inch", "psi", 6894.757293168),
                UnitDefinition("ksi", "Kilopounds per Square Inch", "ksi", 6894757.293),
                UnitDefinition("atm", "Standard Atmosphere", "atm", 101325.0),
                UnitDefinition("torr", "Torr / Millimeter of Mercury", "mmHg", 133.3223684211),
                UnitDefinition("inhg", "Inch of Mercury", "inHg", 3386.389),
                UnitDefinition("barye", "Barye (CGS)", "Ba", 0.1)
            )
        ),

        // 6. Speed & Velocity
        UnitCategory(
            id = "speed_velocity",
            name = "Speed & Velocity",
            iconName = "speed",
            baseUnitName = "m/s",
            units = listOf(
                UnitDefinition("mps", "Meters per Second", "m/s", 1.0),
                UnitDefinition("kmh", "Kilometers per Hour", "km/h", 0.277777778),
                UnitDefinition("mph", "Miles per Hour", "mph", 0.44704),
                UnitDefinition("knot", "Knot (Nautical mi/h)", "kn", 0.514444444),
                UnitDefinition("fps", "Feet per Second", "ft/s", 0.3048),
                UnitDefinition("mach", "Mach (Speed of Sound STP)", "Ma", 340.29),
                UnitDefinition("c", "Speed of Light in Vacuum", "c", 299792458.0),
                UnitDefinition("escape_earth", "Earth Escape Velocity", "vesc", 11186.0)
            )
        ),

        // 7. Force & Power
        UnitCategory(
            id = "force_power",
            name = "Force & Power",
            iconName = "power",
            baseUnitName = "Watt",
            units = listOf(
                UnitDefinition("W", "Watt", "W", 1.0),
                UnitDefinition("kW", "Kilowatt", "kW", 1000.0),
                UnitDefinition("MW", "Megawatt", "MW", 1e6),
                UnitDefinition("GW", "Gigawatt", "GW", 1e9),
                UnitDefinition("hp", "Mechanical Horsepower", "hp", 745.699872),
                UnitDefinition("hp_metric", "Metric Horsepower", "PS", 735.49875),
                UnitDefinition("btu_hr", "BTU per Hour", "BTU/h", 0.29307107),
                UnitDefinition("refrig_ton", "Ton of Refrigeration", "TR", 3516.853),
                UnitDefinition("N", "Newton", "N", 1.0),
                UnitDefinition("kN", "Kilonewton", "kN", 1000.0),
                UnitDefinition("dyn", "Dyne", "dyn", 1e-5),
                UnitDefinition("lbf", "Pound-force", "lbf", 4.4482216),
                UnitDefinition("kgf", "Kilogram-force", "kgf", 9.80665)
            )
        ),

        // 8. Time & Frequency
        UnitCategory(
            id = "time_frequency",
            name = "Time & Frequency",
            iconName = "time",
            baseUnitName = "Second",
            units = listOf(
                UnitDefinition("s", "Second", "s", 1.0),
                UnitDefinition("ms", "Millisecond", "ms", 1e-3),
                UnitDefinition("us", "Microsecond", "µs", 1e-6),
                UnitDefinition("ns", "Nanosecond", "ns", 1e-9),
                UnitDefinition("ps", "Picosecond", "ps", 1e-12),
                UnitDefinition("fs", "Femtosecond", "fs", 1e-15),
                UnitDefinition("planck_time", "Planck Time", "tP", 5.391247e-44),
                UnitDefinition("min", "Minute", "min", 60.0),
                UnitDefinition("hr", "Hour", "h", 3600.0),
                UnitDefinition("day", "Day", "d", 86400.0),
                UnitDefinition("week", "Week", "wk", 604800.0),
                UnitDefinition("month_avg", "Month (Avg 30.43d)", "mo", 2629746.0),
                UnitDefinition("year_julian", "Julian Year", "yr", 31557600.0),
                UnitDefinition("decade", "Decade", "dec", 315576000.0),
                UnitDefinition("century", "Century", "c", 3155760000.0),
                UnitDefinition("hz", "Hertz", "Hz", 1.0),
                UnitDefinition("khz", "Kilohertz", "kHz", 1e3),
                UnitDefinition("mhz", "Megahertz", "MHz", 1e6),
                UnitDefinition("ghz", "Gigahertz", "GHz", 1e9),
                UnitDefinition("thz", "Terahertz", "THz", 1e12),
                UnitDefinition("rpm", "Revolutions per Minute", "RPM", 0.016666667)
            )
        ),

        // 9. Area & Volume
        UnitCategory(
            id = "area_volume",
            name = "Area & Volume",
            iconName = "area",
            baseUnitName = "Square Meter",
            units = listOf(
                UnitDefinition("sq_m", "Square Meter", "m²", 1.0),
                UnitDefinition("sq_km", "Square Kilometer", "km²", 1e6),
                UnitDefinition("sq_cm", "Square Centimeter", "cm²", 1e-4),
                UnitDefinition("sq_mm", "Square Millimeter", "mm²", 1e-6),
                UnitDefinition("sq_mi", "Square Mile", "mi²", 2589988.11),
                UnitDefinition("acre", "Acre", "ac", 4046.8564224),
                UnitDefinition("hectare", "Hectare", "ha", 10000.0),
                UnitDefinition("sq_yd", "Square Yard", "yd²", 0.83612736),
                UnitDefinition("sq_ft", "Square Foot", "ft²", 0.09290304),
                UnitDefinition("sq_in", "Square Inch", "in²", 0.00064516),
                UnitDefinition("barn", "Barn (Nuclear Area)", "b", 1e-28),
                UnitDefinition("cu_m", "Cubic Meter", "m³", 1.0),
                UnitDefinition("l", "Liter", "L", 1e-3),
                UnitDefinition("ml", "Milliliter", "mL", 1e-6),
                UnitDefinition("gal_us", "US Liquid Gallon", "gal", 0.003785411784),
                UnitDefinition("gal_uk", "Imperial Gallon", "imp gal", 0.00454609),
                UnitDefinition("qt", "US Liquid Quart", "qt", 0.000946352946),
                UnitDefinition("pt", "US Liquid Pint", "pt", 0.000473176473),
                UnitDefinition("fl_oz", "US Fluid Ounce", "fl oz", 0.0000295735295625),
                UnitDefinition("cup", "Metric Cup", "cup", 0.00025),
                UnitDefinition("tbsp", "US Tablespoon", "tbsp", 0.00001478676478125),
                UnitDefinition("tsp", "US Teaspoon", "tsp", 0.00000492892159375),
                UnitDefinition("bbl", "Barrel of Oil", "bbl", 0.158987294928)
            )
        )
    )

    fun convert(value: Double, fromUnitId: String, toUnitId: String, categoryId: String): Double {
        if (fromUnitId == toUnitId) return value

        // Temperature Special Conversion
        if (categoryId == "temperature") {
            return convertTemperature(value, fromUnitId, toUnitId)
        }

        val category = categories.find { it.id == categoryId } ?: return value
        val fromUnit = category.units.find { it.id == fromUnitId } ?: return value
        val toUnit = category.units.find { it.id == toUnitId } ?: return value

        val valueInBase = value * fromUnit.factorToBase
        return valueInBase / toUnit.factorToBase
    }

    private fun convertTemperature(value: Double, from: String, to: String): Double {
        // First convert 'from' to Kelvin
        val kelvin = when (from.lowercase()) {
            "c", "celsius" -> value + 273.15
            "f", "fahrenheit" -> (value - 32.0) * 5.0 / 9.0 + 273.15
            "r", "rankine" -> value * 5.0 / 9.0
            "de", "delisle" -> 373.15 - (value * 2.0 / 3.0)
            else -> value // Already Kelvin
        }

        // Convert Kelvin to 'to'
        return when (to.lowercase()) {
            "c", "celsius" -> kelvin - 273.15
            "f", "fahrenheit" -> (kelvin - 273.15) * 9.0 / 5.0 + 32.0
            "r", "rankine" -> kelvin * 9.0 / 5.0
            "de", "delisle" -> (373.15 - kelvin) * 3.0 / 2.0
            else -> kelvin
        }
    }
}
