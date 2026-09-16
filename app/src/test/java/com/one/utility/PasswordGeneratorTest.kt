package com.one.utility

import com.one.utility.core.processing.PasswordGeneratorEngine
import com.one.utility.core.processing.PasswordOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordGeneratorTest {

    private val engine = PasswordGeneratorEngine()

    @Test
    fun testPasswordLength() {
        val options = PasswordOptions(length = 24)
        val password = engine.generatePassword(options)
        assertEquals(24, password.length)
    }

    @Test
    fun testExcludeAmbiguous() {
        val options = PasswordOptions(length = 50, excludeAmbiguous = true)
        val password = engine.generatePassword(options)
        // Ensure ambiguous characters (0, O, 1, l, I) are not present
        assertFalse(password.contains('0'))
        assertFalse(password.contains('O'))
        assertFalse(password.contains('1'))
        assertFalse(password.contains('l'))
        assertFalse(password.contains('I'))
    }
}
