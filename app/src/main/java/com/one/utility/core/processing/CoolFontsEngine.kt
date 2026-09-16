package com.one.utility.core.processing

data class FontStyleResult(
    val id: String,
    val styleName: String,
    val category: String, // "Decorative", "Clean", "Symbols", "Stylized"
    val previewText: String
)

class CoolFontsEngine {

    companion object {
        // Unicode base offsets for Latin letters (A-Z, a-z, 0-9)
        private val SCRIPT_NORMAL_MAP = mapOf(
            'A' to "𝒜", 'B' to "ℬ", 'C' to "𝒞", 'D' to "𝒟", 'E' to "ℰ", 'F' to "ℱ", 'G' to "𝒢", 'H' to "ℋ",
            'I' to "ℐ", 'J' to "𝒥", 'K' to "𝒦", 'L' to "ℒ", 'M' to "ℳ", 'N' to "𝒩", 'O' to "𝒪", 'P' to "𝒫",
            'Q' to "𝒬", 'R' to "ℛ", 'S' to "𝒮", 'T' to "𝒯", 'U' to "𝒰", 'V' to "𝒱", 'W' to "𝒲", 'X' to "𝒳",
            'Y' to "𝒴", 'Z' to "𝒵",
            'a' to "𝒶", 'b' to "𝒷", 'c' to "𝒸", 'd' to "𝒹", 'e' to "ℯ", 'f' to "𝒻", 'g' to "ℊ", 'h' to "𝒽",
            'i' to "𝒾", 'j' to "𝒿", 'k' to "𝓀", 'l' to "𝓁", 'm' to "𝓂", 'n' to "𝓃", 'o' to "ℴ", 'p' to "𝓅",
            'q' to "𝓆", 'r' to "𝓇", 's' to "𝓈", 't' to "𝓉", 'u' to "𝓊", 'v' to "𝓋", 'w' to "𝓌", 'x' to "𝓍",
            'y' to "𝓎", 'z' to "𝓏"
        )

        private val SCRIPT_BOLD_MAP = mapOf(
            'A' to "𝓐", 'B' to "𝓑", 'C' to "𝓒", 'D' to "𝓓", 'E' to "𝓔", 'F' to "𝓕", 'G' to "𝓖", 'H' to "𝓗",
            'I' to "𝓘", 'J' to "𝓙", 'K' to "𝓚", 'L' to "𝓛", 'M' to "𝓜", 'N' to "𝓝", 'O' to "𝓞", 'P' to "𝓟",
            'Q' to "𝓠", 'R' to "𝓡", 'S' to "𝓢", 'T' to "𝓣", 'U' to "𝓤", 'V' to "𝓥", 'W' to "𝓦", 'X' to "𝓧",
            'Y' to "𝓨", 'Z' to "𝓩",
            'a' to "𝓪", 'b' to "𝓫", 'c' to "𝓬", 'd' to "𝓭", 'e' to "𝓮", 'f' to "𝓯", 'g' to "𝓰", 'h' to "𝓱",
            'i' to "𝓲", 'j' to "𝓳", 'k' to "𝓴", 'l' to "𝓵", 'm' to "𝓶", 'n' to "𝓷", 'o' to "𝓸", 'p' to "𝓹",
            'q' to "𝓺", 'r' to "𝓻", 's' to "𝓼", 't' to "𝓽", 'u' to "𝓾", 'v' to "𝓿", 'w' to "𝔀", 'x' to "𝔁",
            'y' to "𝔂", 'z' to "𝔃"
        )

        private val GOTHIC_MAP = mapOf(
            'A' to "𝔄", 'B' to "𝔅", 'C' to "ℭ", 'D' to "𝔇", 'E' to "𝔈", 'F' to "𝔉", 'G' to "𝔊", 'H' to "ℌ",
            'I' to "ℑ", 'J' to "𝔍", 'K' to "𝔎", 'L' to "𝔏", 'M' to "𝔐", 'N' to "𝔑", 'O' to "𝔒", 'P' to "𝔓",
            'Q' to "𝔔", 'R' to "ℜ", 'S' to "𝔖", 'T' to "𝔗", 'U' to "𝔘", 'V' to "𝔙", 'W' to "𝔚", 'X' to "𝔛",
            'Y' to "𝔜", 'Z' to "ℨ",
            'a' to "𝔞", 'b' to "𝔟", 'c' to "𝔠", 'd' to "𝔡", 'e' to "𝔢", 'f' to "𝔣", 'g' to "𝔤", 'h' to "𝔥",
            'i' to "𝔦", 'j' to "𝔧", 'k' to "𝔨", 'l' to "𝔩", 'm' to "𝔪", 'n' to "𝔫", 'o' to "𝔬", 'p' to "𝔭",
            'q' to "𝔮", 'r' to "𝔯", 's' to "𝔰", 't' to "𝔱", 'u' to "𝔲", 'v' to "𝔳", 'w' to "𝔴", 'x' to "𝔵",
            'y' to "𝔶", 'z' to "𝔷"
        )

        private val GOTHIC_BOLD_MAP = mapOf(
            'A' to "𝕬", 'B' to "𝕭", 'C' to "𝕮", 'D' to "𝕯", 'E' to "𝕰", 'F' to "𝕱", 'G' to "𝕲", 'H' to "𝕳",
            'I' to "𝕴", 'J' to "𝕵", 'K' to "𝕶", 'L' to "𝕷", 'M' to "𝕸", 'N' to "𝕹", 'O' to "𝕺", 'P' to "𝕻",
            'Q' to "𝕼", 'R' to "𝕽", 'S' to "𝕾", 'T' to "𝕿", 'U' to "𝖀", 'V' to "𝖁", 'W' to "𝖂", 'X' to "𝖃",
            'Y' to "𝖄", 'Z' to "𝖅",
            'a' to "𝖆", 'b' to "𝖇", 'c' to "𝖈", 'd' to "𝖉", 'e' to "𝖊", 'f' to "𝖋", 'g' to "𝖌", 'h' to "𝖍",
            'i' to "𝖎", 'j' to "𝖏", 'k' to "𝖐", 'l' to "𝖑", 'm' to "𝖒", 'n' to "𝖓", 'o' to "𝖔", 'p' to "𝖕",
            'q' to "𝖖", 'r' to "𝖗", 's' to "𝖘", 't' to "𝖙", 'u' to "𝖚", 'v' to "𝖛", 'w' to "𝖜", 'x' to "𝖝",
            'y' to "𝖞", 'z' to "𝖟"
        )

        private val DOUBLE_STRUCK_MAP = mapOf(
            'A' to "𝔸", 'B' to "𝔹", 'C' to "ℂ", 'D' to "𝔻", 'E' to "𝔼", 'F' to "𝔽", 'G' to "𝔾", 'H' to "ℍ",
            'I' to "𝕀", 'J' to "𝕁", 'K' to "𝕂", 'L' to "𝕃", 'M' to "𝕄", 'N' to "ℕ", 'O' to "𝕆", 'P' to "ℙ",
            'Q' to "ℚ", 'R' to "ℝ", 'S' to "𝕊", 'T' to "𝕋", 'U' to "𝕌", 'V' to "𝕍", 'W' to "𝕎", 'X' to "𝕏",
            'Y' to "𝕐", 'Z' to "ℤ",
            'a' to "𝕒", 'b' to "𝕓", 'c' to "𝕔", 'd' to "𝕕", 'e' to "𝕖", 'f' to "𝕗", 'g' to "𝕘", 'h' to "𝕙",
            'i' to "𝕚", 'j' to "𝕛", 'k' to "𝕜", 'l' to "𝕝", 'm' to "𝕞", 'n' to "𝕟", 'o' to "𝕠", 'p' to "𝕡",
            'q' to "𝕢", 'r' to "𝕣", 's' to "𝕤", 't' to "𝕥", 'u' to "𝕦", 'v' to "𝕧", 'w' to "𝕨", 'x' to "𝕩",
            'y' to "𝕪", 'z' to "𝕫",
            '0' to "𝟘", '1' to "𝟙", '2' to "𝟚", '3' to "𝟛", '4' to "𝟜", '5' to "𝟝", '6' to "𝟞", '7' to "𝟟",
            '8' to "𝟠", '9' to "𝟡"
        )

        private val SMALL_CAPS_MAP = mapOf(
            'a' to "ᴀ", 'b' to "ʙ", 'c' to "ᴄ", 'd' to "ᴅ", 'e' to "ᴇ", 'f' to "ꜰ", 'g' to "ɢ", 'h' to "ʜ",
            'i' to "ɪ", 'j' to "ᴊ", 'k' to "ᴋ", 'l' to "ʟ", 'm' to "ᴍ", 'n' to "ɴ", 'o' to "ᴏ", 'p' to "ᴘ",
            'q' to "ǫ", 'r' to "ʀ", 's' to "s", 't' to "ᴛ", 'u' to "ᴜ", 'v' to "ᴠ", 'w' to "ᴡ", 'x' to "x",
            'y' to "ʏ", 'z' to "ᴢ",
            'A' to "ᴀ", 'B' to "ʙ", 'C' to "ᴄ", 'D' to "ᴅ", 'E' to "ᴇ", 'F' to "ꜰ", 'G' to "ɢ", 'H' to "ʜ",
            'I' to "ɪ", 'J' to "ᴊ", 'K' to "ᴋ", 'L' to "ʟ", 'M' to "ᴍ", 'N' to "ɴ", 'O' to "ᴏ", 'P' to "ᴘ",
            'Q' to "ǫ", 'R' to "ʀ", 'S' to "s", 'T' to "ᴛ", 'U' to "ᴜ", 'V' to "ᴠ", 'W' to "ᴡ", 'X' to "x",
            'Y' to "ʏ", 'Z' to "ᴢ"
        )

        private val CIRCLED_MAP = mapOf(
            'A' to "Ⓐ", 'B' to "Ⓑ", 'C' to "Ⓒ", 'D' to "Ⓓ", 'E' to "Ⓔ", 'F' to "Ⓕ", 'G' to "Ⓖ", 'H' to "Ⓗ",
            'I' to "Ⓘ", 'J' to "Ⓙ", 'K' to "Ⓚ", 'L' to "Ⓛ", 'M' to "Ⓜ", 'N' to "Ⓝ", 'O' to "Ⓞ", 'P' to "Ⓟ",
            'Q' to "Ⓠ", 'R' to "Ⓡ", 'S' to "Ⓢ", 'T' to "Ⓣ", 'U' to "Ⓤ", 'V' to "Ⓥ", 'W' to "Ⓦ", 'X' to "Ⓧ",
            'Y' to "Ⓨ", 'Z' to "Ⓩ",
            'a' to "ⓐ", 'b' to "ⓑ", 'c' to "ⓒ", 'd' to "ⓓ", 'e' to "ⓔ", 'f' to "ⓕ", 'g' to "ⓖ", 'h' to "ⓗ",
            'i' to "ⓘ", 'j' to "ⓙ", 'k' to "ⓚ", 'l' to "ⓛ", 'm' to "ⓜ", 'n' to "ⓝ", 'o' to "ⓞ", 'p' to "ⓟ",
            'q' to "ⓠ", 'r' to "ⓡ", 's' to "ⓢ", 't' to "ⓣ", 'u' to "ⓤ", 'v' to "ⓥ", 'w' to "ⓦ", 'x' to "ⓧ",
            'y' to "ⓨ", 'z' to "ⓩ",
            '0' to "⓪", '1' to "①", '2' to "②", '3' to "③", '4' to "④", '5' to "⑤", '6' to "⑥", '7' to "⑦",
            '8' to "⑧", '9' to "⑨"
        )

        private val CIRCLED_DARK_MAP = mapOf(
            'A' to "🅐", 'B' to "🅑", 'C' to "🅒", 'D' to "🅓", 'E' to "🅔", 'F' to "🅕", 'G' to "🅖", 'H' to "🅗",
            'I' to "🅘", 'J' to "🅙", 'K' to "🅚", 'L' to "🅛", 'M' to "🅜", 'N' to "🅝", 'O' to "🅞", 'P' to "🅟",
            'Q' to "🅠", 'R' to "🅡", 'S' to "🅢", 'T' to "🅣", 'U' to "🅤", 'V' to "🅥", 'W' to "🅦", 'X' to "🅧",
            'Y' to "🅨", 'Z' to "🅩",
            'a' to "🅐", 'b' to "🅑", 'c' to "🅒", 'd' to "🅓", 'e' to "🅔", 'f' to "🅕", 'g' to "🅖", 'h' to "🅗",
            'i' to "🅘", 'j' to "🅙", 'k' to "🅚", 'l' to "🅛", 'm' to "🅜", 'n' to "🅝", 'o' to "🅞", 'p' to "🅟",
            'q' to "🅠", 'r' to "🅡", 's' to "🅢", 't' to "🅣", 'u' to "🅤", 'v' to "🅥", 'w' to "🅦", 'x' to "🅧",
            'y' to "🅨", 'z' to "🅩",
            '0' to "⓿", '1' to "❶", '2' to "❷", '3' to "❸", '4' to "❹", '5' to "❺", '6' to "❻", '7' to "❼",
            '8' to "❽", '9' to "❾"
        )

        private val SQUARED_MAP = mapOf(
            'A' to "🄰", 'B' to "🄱", 'C' to "🄲", 'D' to "🄳", 'E' to "🄴", 'F' to "🄵", 'G' to "🄶", 'H' to "🄷",
            'I' to "🄸", 'J' to "🄹", 'K' to "🄺", 'L' to "🄻", 'M' to "🄼", 'N' to "🄽", 'O' to "🄾", 'P' to "🄿",
            'Q' to "🅀", 'R' to "🅁", 'S' to "🅂", 'T' to "🅃", 'U' to "🅄", 'V' to "🅅", 'W' to "🅆", 'X' to "🅇",
            'Y' to "🅈", 'Z' to "🅉",
            'a' to "🄰", 'b' to "🄱", 'c' to "🄲", 'd' to "🄳", 'e' to "🄴", 'f' to "🄵", 'g' to "🄶", 'h' to "🄷",
            'i' to "🄸", 'j' to "🄹", 'k' to "🄺", 'l' to "🄻", 'm' to "🄼", 'n' to "🄽", 'o' to "🄾", 'p' to "🄿",
            'q' to "🅀", 'r' to "🅁", 's' to "🅂", 't' to "🅃", 'u' to "🅄", 'v' to "🅅", 'w' to "🅆", 'x' to "🅇",
            'y' to "🅈", 'z' to "🅉"
        )

        private val SQUARED_DARK_MAP = mapOf(
            'A' to "🅰", 'B' to "🅱", 'C' to "🅲", 'D' to "🅳", 'E' to "🅴", 'F' to "🅵", 'G' to "🅶", 'H' to "🅷",
            'I' to "🅸", 'J' to "🅹", 'K' to "🅺", 'L' to "🅻", 'M' to "🅼", 'N' to "🅽", 'O' to "🅾", 'P' to "🅿",
            'Q' to "🆀", 'R' to "🆁", 'S' to "🆂", 'T' to "🆃", 'U' to "🆄", 'V' to "🆅", 'W' to "🆆", 'X' to "🆇",
            'Y' to "🆈", 'Z' to "🆉",
            'a' to "🅰", 'b' to "🅱", 'c' to "🅲", 'd' to "🅳", 'e' to "🅴", 'f' to "🅵", 'g' to "🅶", 'h' to "🅷",
            'i' to "🅸", 'j' to "🅹", 'k' to "🅺", 'l' to "🅻", 'm' to "🅼", 'n' to "🅽", 'o' to "🅾", 'p' to "🅿",
            'q' to "🆀", 'r' to "🆁", 's' to "🆂", 't' to "🆃", 'u' to "🆄", 'v' to "🆅", 'w' to "🆆", 'x' to "🆇",
            'y' to "🆈", 'z' to "🆉"
        )

        private val UPSIDE_DOWN_MAP = mapOf(
            'a' to "ɐ", 'b' to "q", 'c' to "ɔ", 'd' to "p", 'e' to "ǝ", 'f' to "ɟ", 'g' to "ƃ", 'h' to "ɥ",
            'i' to "ᴉ", 'j' to "ɾ", 'k' to "ʞ", 'l' to "l", 'm' to "ɯ", 'n' to "u", 'o' to "o", 'p' to "d",
            'q' to "b", 'r' to "ɹ", 's' to "s", 't' to "ʇ", 'u' to "n", 'v' to "ʌ", 'w' to "ʍ", 'x' to "x",
            'y' to "ʎ", 'z' to "z",
            'A' to "∀", 'B' to "𐐒", 'C' to "Ɔ", 'D' to "p", 'E' to "Ǝ", 'F' to "Ⅎ", 'G' to "⅁", 'H' to "H",
            'I' to "I", 'J' to "ſ", 'K' to "ʞ", 'L' to "˥", 'M' to "W", 'N' to "N", 'O' to "O", 'P' to "Ԁ",
            'Q' to "Ό", 'R' to "ᴚ", 'S' to "S", 'T' to "⊥", 'U' to "∩", 'V' to "Λ", 'W' to "M", 'X' to "X",
            'Y' to "⅄", 'Z' to "Z",
            '?' to "¿", '!' to "¡", '.' to "˙", ',' to "'", '_' to "‾"
        )
    }

    private fun mapChars(input: String, map: Map<Char, String>): String {
        return buildString {
            for (ch in input) {
                append(map[ch] ?: ch.toString())
            }
        }
    }

    private fun mathTransform(input: String, upperBase: Int, lowerBase: Int, digitBase: Int? = null): String {
        return buildString {
            for (ch in input) {
                when {
                    ch in 'A'..'Z' -> append(String(Character.toChars(upperBase + (ch - 'A'))))
                    ch in 'a'..'z' -> append(String(Character.toChars(lowerBase + (ch - 'a'))))
                    digitBase != null && ch in '0'..'9' -> append(String(Character.toChars(digitBase + (ch - '0'))))
                    else -> append(ch)
                }
            }
        }
    }

    fun generateStyles(text: String): List<FontStyleResult> {
        val query = if (text.isBlank()) "ONE Utility" else text

        return listOf(
            // 1. Gothic & Fraktur
            FontStyleResult("fraktur_bold", "Gothic Bold", "Decorative", mapChars(query, GOTHIC_BOLD_MAP)),
            FontStyleResult("fraktur_normal", "Gothic Classic", "Decorative", mapChars(query, GOTHIC_MAP)),

            // 2. Cursive & Script
            FontStyleResult("script_bold", "Cursive Bold", "Decorative", mapChars(query, SCRIPT_BOLD_MAP)),
            FontStyleResult("script_normal", "Cursive Elegant", "Decorative", mapChars(query, SCRIPT_NORMAL_MAP)),

            // 3. Double-Struck / Blackboard
            FontStyleResult("double_struck", "Double Struck (Outline)", "Decorative", mapChars(query, DOUBLE_STRUCK_MAP)),

            // 4. Clean Serif & Sans
            FontStyleResult("serif_bold", "Serif Bold", "Clean", mathTransform(query, 0x1D400, 0x1D41A, 0x1D7CE)),
            FontStyleResult("serif_italic", "Serif Italic", "Clean", mathTransform(query, 0x1D434, 0x1D44E)),
            FontStyleResult("serif_bold_italic", "Serif Bold Italic", "Clean", mathTransform(query, 0x1D468, 0x1D482)),
            FontStyleResult("sans_bold", "Sans-Serif Bold", "Clean", mathTransform(query, 0x1D5D4, 0x1D5EE, 0x1D7EC)),
            FontStyleResult("sans_italic", "Sans-Serif Italic", "Clean", mathTransform(query, 0x1D608, 0x1D622)),
            FontStyleResult("sans_bold_italic", "Sans Bold Italic", "Clean", mathTransform(query, 0x1D63C, 0x1D656)),
            FontStyleResult("monospace", "Monospace / Code", "Clean", mathTransform(query, 0x1D670, 0x1D68A, 0x1D7F6)),

            // 5. Bubbles & Frames
            FontStyleResult("circled", "Circled Bubbles", "Symbols", mapChars(query, CIRCLED_MAP)),
            FontStyleResult("circled_dark", "Dark Bubble Badges", "Symbols", mapChars(query, CIRCLED_DARK_MAP)),
            FontStyleResult("squared", "Squared Frames", "Symbols", mapChars(query, SQUARED_MAP)),
            FontStyleResult("squared_dark", "Dark Square Badges", "Symbols", mapChars(query, SQUARED_DARK_MAP)),

            // 6. Sizes & Orientations
            FontStyleResult("small_caps", "Small Capitals", "Stylized", mapChars(query, SMALL_CAPS_MAP)),
            FontStyleResult("vaporwave", "V a p o r w a v e (Wide)", "Stylized", buildString {
                for (ch in query) {
                    when {
                        ch in '!'..'~' -> append(String(Character.toChars(0xFF01 + (ch.code - 33))))
                        ch == ' ' -> append("　")
                        else -> append(ch)
                    }
                }
            }),
            FontStyleResult("upside_down", "Inverted / Upside Down", "Stylized", buildString {
                for (i in query.length - 1 downTo 0) {
                    val ch = query[i]
                    append(UPSIDE_DOWN_MAP[ch] ?: ch.toString())
                }
            }),

            // 7. Modifiers (Strikethrough & Underline)
            FontStyleResult("strikethrough", "S̶t̶r̶i̶k̶e̶t̶h̶r̶o̶u̶g̶h̶", "Stylized", buildString {
                for (ch in query) {
                    append(ch).append('\u0336')
                }
            }),
            FontStyleResult("underline", "U̲n̲d̲e̲r̲l̲i̲n̲e̲", "Stylized", buildString {
                for (ch in query) {
                    append(ch).append('\u0332')
                }
            }),

            // 8. Aesthetic Decorative Frames & Kaomoji (Viral Bio & Name Styles)
            FontStyleResult("kaomoji_wings", "Royal Crown Wings", "Decorative", "꧁༺ $query ༻꧂"),
            FontStyleResult("kaomoji_stars", "Sparkle Stars", "Decorative", "★彡 $query 彡★"),
            FontStyleResult("kaomoji_aesthetic", "Aesthetic Glow", "Decorative", "✧･ﾟ: * $query *:･ﾟ✧"),
            FontStyleResult("kaomoji_sparkles", "Magic Sparkles", "Decorative", "✦ ࣪ ˖ $query ˖ ࣪ ✦"),
            FontStyleResult("kaomoji_arrow", "Clean Pointer", "Decorative", "╰┈➤ $query"),
            FontStyleResult("kaomoji_brackets", "Japanese Quoted", "Decorative", "『 $query 』"),
            FontStyleResult("kaomoji_heart", "Heart Accented", "Decorative", "♡ $query ♡")
        )
    }
}
