package com.one.utility.core.processing

data class TextStatistics(
    val words: Int,
    val charactersWithSpaces: Int,
    val charactersWithoutSpaces: Int,
    val lines: Int,
    val sentences: Int,
    val estimatedReadingTimeMinutes: Int
)

class TextToolsEngine {

    fun analyze(text: String): TextStatistics {
        val trimmed = text.trim()
        val words = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("""\s+""")).size
        val charsWithSpaces = text.length
        val charsWithoutSpaces = text.replace(" ", "").replace("\n", "").replace("\r", "").length
        val lines = if (text.isEmpty()) 0 else text.lines().size
        val sentences = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("""[.!?]+\s*""")).filter { it.isNotBlank() }.size
        val readingTime = if (words == 0) 0 else ((words + 199) / 200).coerceAtLeast(1)

        return TextStatistics(
            words = words,
            charactersWithSpaces = charsWithSpaces,
            charactersWithoutSpaces = charsWithoutSpaces,
            lines = lines,
            sentences = sentences,
            estimatedReadingTimeMinutes = readingTime
        )
    }

    fun toUpperCase(text: String): String = text.uppercase()

    fun toLowerCase(text: String): String = text.lowercase()

    fun toTitleCase(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    fun toSentenceCase(text: String): String {
        return text.split(Regex("""(?<=[.!?]\s+)""")).joinToString("") { sentence ->
            sentence.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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

    fun findAndReplace(text: String, find: String, replace: String, ignoreCase: Boolean = true): String {
        if (find.isEmpty()) return text
        return text.replace(find, replace, ignoreCase = ignoreCase)
    }

    fun cleanText(text: String): String {
        return text
            .replace(Regex("""<[^>]*>"""), "") // Strip HTML
            .replace(Regex("""[\x00-\x08\x0B\x0C\x0E-\x1F]"""), "") // Strip non-printables
            .trim()
    }

    fun generateLoremIpsum(paragraphs: Int = 2): String {
        val standard = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
        return List(paragraphs.coerceIn(1, 5)) { standard }.joinToString("\n\n")
    }
}