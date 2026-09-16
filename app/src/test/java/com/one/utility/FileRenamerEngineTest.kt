package com.one.utility

import com.one.utility.core.processing.FileRenamerEngine
import com.one.utility.core.processing.RenameRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class FileRenamerEngineTest {

    private val engine = FileRenamerEngine()

    @Test
    fun testPatternReplacement() {
        val files = listOf(File("IMG_001.jpg"), File("IMG_002.jpg"))
        val rule = RenameRule(findText = "IMG", replaceWith = "Vacation")
        val previews = engine.previewBatchRename(files, rule)

        assertEquals("Vacation_001.jpg", previews[0].newName)
        assertEquals("Vacation_002.jpg", previews[1].newName)
    }

    @Test
    fun testSequentialNumberingWithPadding() {
        val files = listOf(File("Photo.png"), File("Photo.png"))
        val rule = RenameRule(prefix = "Doc", appendIndex = true, startIndex = 1, indexPadding = 3)
        val previews = engine.previewBatchRename(files, rule)

        assertEquals("DocPhoto_001.png", previews[0].newName)
        assertEquals("DocPhoto_002.png", previews[1].newName)
    }

    @Test
    fun testSuffixAddition() {
        val files = listOf(File("Contract.pdf"))
        val rule = RenameRule(suffix = "_Signed")
        val previews = engine.previewBatchRename(files, rule)

        assertEquals("Contract_Signed.pdf", previews[0].newName)
    }
}
