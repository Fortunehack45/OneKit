package com.one.utility.core.processing

data class TextStatistics(
    val words: Int,
    val charactersWithSpaces: Int,
    val charactersWithoutSpaces: Int,
    val lines: Int,
    val sentences: Int
)

class TextToolsEngine {

    fun analyze(text: String): TextStatistics {
        val trimmed = text.trim()
        val words = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("""\s+""")).size
        val charsWithSpaces = text.length
        val charsWithoutSpaces = text.replace(" ", "").replace("\n", "").replace("\r", "").length
        val lines = if (text.isEmpty()) 0 else text.lines().size
        val sentences = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("""[.!?]+\s*""")).filter { it.isNotBlank() }.size

        return TextStatistics(
            words = words,
            charactersWithSpaces = charsWithSpaces,
            charactersWithoutSpaces = charsWithoutSpaces,
            lines = lines,
            sentences = sentences
        )
    }

    fun toUpperCase(text: String): String = text.uppercase()

    fun toLowerCase(text: String): String = text.lowercase()

    fun toTitleCase(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun removeExtraSpaces(text: String): String {
        return text.trim().replace(Regex("""[ \t]+"""), " ")
    }

    fun removeDuplicateLines(text: String): String {
        return text.lines().distinct().joinToString("\n")
    }

    fun sortLinesAlphabetically(text: String, ascending: Boolean = true): String {
        val lines = text.lines()
        return if (ascending) lines.sorted().joinToString("\n") else lines.sortedDescending().joinToString("\n")
    }

    fun reverseText(text: String): String = text.reversed()
}
