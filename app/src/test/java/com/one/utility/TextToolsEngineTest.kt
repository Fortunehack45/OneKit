package com.one.utility

import com.one.utility.core.processing.TextToolsEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class TextToolsEngineTest {

    private val engine = TextToolsEngine()

    @Test
    fun testWordAndCharCount() {
        val text = "The quick brown fox jumps over the lazy dog."
        val stats = engine.analyze(text)
        assertEquals(9, stats.words)
        assertEquals(44, stats.charactersWithSpaces)
        assertEquals(36, stats.charactersWithoutSpaces)
        assertEquals(1, stats.sentences)
    }

    @Test
    fun testCaseConversions() {
        assertEquals("HELLO WORLD", engine.toUpperCase("hello world"))
        assertEquals("hello world", engine.toLowerCase("HELLO WORLD"))
        assertEquals("Hello World From One", engine.toTitleCase("hello world from one"))
    }

    @Test
    fun testExtraSpacesAndDuplicateLines() {
        val spaces = "This   is   a    spaced   sentence."
        assertEquals("This is a spaced sentence.", engine.removeExtraSpaces(spaces))

        val duplicateLines = "Apple\nBanana\nApple\nOrange\nBanana"
        assertEquals("Apple\nBanana\nOrange", engine.removeDuplicateLines(duplicateLines))
    }

    @Test
    fun testSortLines() {
        val unsorted = "Banana\nApple\nCherry"
        assertEquals("Apple\nBanana\nCherry", engine.sortLinesAlphabetically(unsorted, ascending = true))
    }
}
