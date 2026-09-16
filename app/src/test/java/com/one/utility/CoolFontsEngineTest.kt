package com.one.utility

import com.one.utility.core.processing.CoolFontsEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoolFontsEngineTest {

    private val engine = CoolFontsEngine()

    @Test
    fun testGenerateStylesProducesRichList() {
        val styles = engine.generateStyles("Alex")
        assertTrue("Should generate at least 20 font styles", styles.size >= 20)

        // Verify Gothic bold transformation
        val gothicBold = styles.find { it.id == "fraktur_bold" }
        assertTrue("Should include Gothic bold", gothicBold != null)
        assertTrue("Gothic preview should contain transformed characters", gothicBold!!.previewText.contains("𝕬"))

        // Verify Cursive/Script transformation
        val cursive = styles.find { it.id == "script_normal" }
        assertTrue("Should include Cursive", cursive != null)
        assertTrue("Cursive preview should contain script characters", cursive!!.previewText.contains("𝒜"))

        // Verify Small Caps
        val smallCaps = styles.find { it.id == "small_caps" }
        assertTrue("Should include Small Caps", smallCaps != null)
        assertTrue("Small caps preview should transform letters", smallCaps!!.previewText.contains("ᴀ"))

        // Verify Kaomoji frames
        val kaomoji = styles.find { it.id == "kaomoji_wings" }
        assertTrue("Should include Kaomoji wings", kaomoji != null)
        assertTrue("Kaomoji preview should wrap query", kaomoji!!.previewText.contains("Alex"))
    }

    @Test
    fun testEmptyInputProvidesFallback() {
        val styles = engine.generateStyles("")
        assertFalse("Styles should not be empty even when input is blank", styles.isEmpty())
        assertTrue("Fallback text should be present", styles.first().previewText.isNotBlank())
    }
}
