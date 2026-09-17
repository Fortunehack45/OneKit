package com.one.utility.core.processing

import java.security.SecureRandom

data class PasswordOptions(
    val length: Int = 16,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSymbols: Boolean = true,
    val excludeAmbiguous: Boolean = true
)

class PasswordGeneratorEngine {

    private val uppercase = "ABCDEFGHJKLMNPQRSTUVWXYZ" // O, I excluded if ambiguous
    private val uppercaseAll = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    private val lowercase = "abcdefghijkmnpqrstuvwxyz" // l, o excluded if ambiguous
    private val lowercaseAll = "abcdefghijklmnopqrstuvwxyz"

    private val numbers = "23456789" // 0, 1 excluded if ambiguous
    private val numbersAll = "0123456789"

    private val symbols = "!@#$%^&*()-_=+[]{}<>?"

    fun generatePassword(options: PasswordOptions): String {
        val random = SecureRandom()
        val pool = StringBuilder()

        if (options.includeUppercase) pool.append(if (options.excludeAmbiguous) uppercase else uppercaseAll)
        if (options.includeLowercase) pool.append(if (options.excludeAmbiguous) lowercase else lowercaseAll)
        if (options.includeNumbers) pool.append(if (options.excludeAmbiguous) numbers else numbersAll)
        if (options.includeSymbols) pool.append(symbols)

        if (pool.isEmpty()) return ""

        val password = StringBuilder()
        for (i in 0 until options.length) {
            val index = random.nextInt(pool.length)
            password.append(pool[index])
        }

        return password.toString()
    }

    private val dicewareWords = listOf(
        "correct", "horse", "battery", "staple", "galaxy", "orbit", "solar", "matrix",
        "quantum", "cipher", "beacon", "falcon", "forest", "harbor", "island", "jungle",
        "meteor", "nebula", "planet", "quarry", "rocket", "summit", "tunnel", "valley",
        "whisper", "zenith", "crystal", "dragon", "echo", "frost", "glacier", "horizon",
        "legend", "mirror", "oasis", "prism", "quartz", "shadow", "timber", "voyage"
    )

    fun generatePassphrase(wordCount: Int = 4, separator: String = "-"): String {
        val random = SecureRandom()
        val count = wordCount.coerceIn(2, 10)
        return (1..count).map {
            dicewareWords[random.nextInt(dicewareWords.size)]
        }.joinToString(separator)
    }
}
