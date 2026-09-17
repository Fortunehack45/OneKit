package com.one.utility.core.processing

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class JwtPayload(
    val headerJson: String,
    val bodyJson: String
)

data class ColorDetails(
    val hex: String,
    val r: Int,
    val g: Int,
    val b: Int,
    val h: Float,
    val s: Float,
    val l: Float,
    val complementaryHex: String,
    val contrastRatioWithWhite: Double,
    val contrastRatioWithBlack: Double
)

class DeveloperToolsEngine {

    // 1. JSON Formatter & Minifier
    fun formatJson(raw: String, indentSpaces: Int = 2): String {
        val trimmed = raw.trim()
        return if (trimmed.startsWith("{")) {
            JSONObject(trimmed).toString(indentSpaces)
        } else if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString(indentSpaces)
        } else {
            raw
        }
    }

    fun minifyJson(raw: String): String {
        val trimmed = raw.trim()
        return if (trimmed.startsWith("{")) {
            JSONObject(trimmed).toString()
        } else if (trimmed.startsWith("[")) {
            JSONArray(trimmed).toString()
        } else {
            raw
        }
    }

    // 2. Base64
    fun base64Encode(text: String): String {
        return Base64.encodeToString(text.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
    }

    fun base64Decode(encoded: String): String {
        val bytes = Base64.decode(encoded, Base64.DEFAULT)
        return String(bytes, StandardCharsets.UTF_8)
    }

    // 3. URL Encoding & Decoding
    fun urlEncode(text: String): String = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
    fun urlDecode(text: String): String = URLDecoder.decode(text, StandardCharsets.UTF_8.toString())

    // 4. HTML Escape & Unescape
    fun htmlEscape(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun htmlUnescape(text: String): String {
        return text
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
    }

    // 5. JWT Inspector
    fun decodeJwt(jwt: String): JwtPayload {
        val parts = jwt.trim().split(".")
        if (parts.size < 2) throw IllegalArgumentException("Invalid JWT format (requires at least 2 parts)")
        val header = String(Base64.decode(parts[0], Base64.URL_SAFE or Base64.NO_PADDING), StandardCharsets.UTF_8)
        val body = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING), StandardCharsets.UTF_8)
        return JwtPayload(
            headerJson = formatJson(header),
            bodyJson = formatJson(body)
        )
    }

    // 6. Hashes
    fun hashString(input: String, algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // 7. UUID & Random Generators
    fun generateUuid(): String = UUID.randomUUID().toString()

    // 8. Unix Timestamp
    fun timestampToDate(timestampSeconds: Long): String {
        val date = Date(timestampSeconds * 1000)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        return format.format(date)
    }

    fun currentTimestamp(): Long = System.currentTimeMillis() / 1000

    // 9. Color Parser & WCAG Contrast
    fun parseColor(hexInput: String): ColorDetails? {
        val cleanHex = hexInput.trim().removePrefix("#")
        if (cleanHex.length != 6 && cleanHex.length != 8) return null
        val r = cleanHex.substring(0, 2).toIntOrNull(16) ?: return null
        val g = cleanHex.substring(2, 4).toIntOrNull(16) ?: return null
        val b = cleanHex.substring(4, 6).toIntOrNull(16) ?: return null

        val rNorm = r / 255.0
        val gNorm = g / 255.0
        val bNorm = b / 255.0
        val max = maxOf(rNorm, gNorm, bNorm)
        val min = minOf(rNorm, gNorm, bNorm)
        val l = (max + min) / 2.0
        val d = max - min
        val s = if (d == 0.0) 0.0 else if (l > 0.5) d / (2.0 - max - min) else d / (max + min)
        val h = when {
            d == 0.0 -> 0.0
            max == rNorm -> ((gNorm - bNorm) / d + (if (gNorm < bNorm) 6.0 else 0.0)) * 60.0
            max == gNorm -> ((bNorm - rNorm) / d + 2.0) * 60.0
            else -> ((rNorm - gNorm) / d + 4.0) * 60.0
        }

        // Complementary (rotate hue 180°)
        val compH = (h + 180.0) % 360.0
        val compRgb = hslToRgb(compH, s, l)
        val compHex = "#%02X%02X%02X".format(compRgb.first, compRgb.second, compRgb.third)

        fun relLuminance(red: Int, green: Int, blue: Int): Double {
            fun channelLum(c: Int): Double {
                val sc = c / 255.0
                return if (sc <= 0.03928) sc / 12.92 else Math.pow((sc + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * channelLum(red) + 0.7152 * channelLum(green) + 0.0722 * channelLum(blue)
        }

        val lum = relLuminance(r, g, b)
        val contrastWhite = (1.0 + 0.05) / (lum + 0.05)
        val contrastBlack = (lum + 0.05) / (0.0 + 0.05)

        return ColorDetails(
            hex = "#" + cleanHex.take(6).uppercase(),
            r = r, g = g, b = b,
            h = h.toFloat(),
            s = (s * 100.0).toFloat(),
            l = (l * 100.0).toFloat(),
            complementaryHex = compHex,
            contrastRatioWithWhite = Math.round(contrastWhite * 10.0) / 10.0,
            contrastRatioWithBlack = Math.round(contrastBlack * 10.0) / 10.0
        )
    }

    private fun hslToRgb(h: Double, s: Double, l: Double): Triple<Int, Int, Int> {
        val c = (1.0 - Math.abs(2.0 * l - 1.0)) * s
        val x = c * (1.0 - Math.abs((h / 60.0) % 2.0 - 1.0))
        val m = l - c / 2.0
        val (rPrime, gPrime, bPrime) = when {
            h < 60.0 -> Triple(c, x, 0.0)
            h < 120.0 -> Triple(x, c, 0.0)
            h < 180.0 -> Triple(0.0, c, x)
            h < 240.0 -> Triple(0.0, x, c)
            h < 300.0 -> Triple(x, 0.0, c)
            else -> Triple(c, 0.0, x)
        }
        val red = Math.round((rPrime + m) * 255).toInt().coerceIn(0, 255)
        val green = Math.round((gPrime + m) * 255).toInt().coerceIn(0, 255)
        val blue = Math.round((bPrime + m) * 255).toInt().coerceIn(0, 255)
        return Triple(red, green, blue)
    }

    // 10. XML Formatter
    fun formatXml(xml: String): String {
        val trimmed = xml.trim()
        if (trimmed.isEmpty()) return ""
        val sb = StringBuilder()
        var indent = 0
        var i = 0
        while (i < trimmed.length) {
            if (trimmed[i] == '<') {
                val closeTag = trimmed.indexOf('>', i)
                if (closeTag == -1) {
                    sb.append(trimmed.substring(i))
                    break
                }
                val tag = trimmed.substring(i, closeTag + 1)
                val isClosing = tag.startsWith("</")
                val isSelfClosing = tag.endsWith("/>") || tag.startsWith("<?") || tag.startsWith("<!")

                if (isClosing) {
                    indent = (indent - 1).coerceAtLeast(0)
                }

                if (sb.isNotEmpty() && sb.last() != '\n') sb.append("\n")
                sb.append("  ".repeat(indent)).append(tag)

                if (!isClosing && !isSelfClosing) {
                    indent++
                }
                i = closeTag + 1
            } else {
                val nextTag = trimmed.indexOf('<', i)
                val content = if (nextTag == -1) trimmed.substring(i) else trimmed.substring(i, nextTag)
                val cleanContent = content.trim()
                if (cleanContent.isNotEmpty()) {
                    sb.append(cleanContent)
                }
                i = if (nextTag == -1) trimmed.length else nextTag
            }
        }
        return sb.toString()
    }

    // 11. CSS Formatter
    fun formatCss(css: String): String {
        val trimmed = css.trim()
        if (trimmed.isEmpty()) return ""
        val sb = StringBuilder()
        var indent = 0
        val tokens = trimmed.replace("\r", "").split(Regex("""(?<=[{};])|(?=[{}])""")).map { it.trim() }.filter { it.isNotEmpty() }
        for (token in tokens) {
            when (token) {
                "{" -> {
                    sb.append(" {\n")
                    indent++
                }
                "}" -> {
                    indent = (indent - 1).coerceAtLeast(0)
                    sb.append("\n").append("  ".repeat(indent)).append("}\n")
                }
                else -> {
                    if (sb.isNotEmpty() && sb.last() != '\n') sb.append("\n")
                    sb.append("  ".repeat(indent)).append(token)
                }
            }
        }
        return sb.toString().trim()
    }

    // 12. URL Parser
    data class ParsedUrl(
        val scheme: String,
        val host: String,
        val port: String,
        val path: String,
        val queryParams: Map<String, String>,
        val fragment: String
    )

    fun parseUrl(urlString: String): ParsedUrl {
        return runCatching {
            val uri = java.net.URI(urlString.trim())
            val params = mutableMapOf<String, String>()
            uri.query?.split("&")?.forEach { part ->
                val kv = part.split("=")
                if (kv.isNotEmpty()) {
                    val k = URLDecoder.decode(kv[0], "UTF-8")
                    val v = if (kv.size > 1) URLDecoder.decode(kv[1], "UTF-8") else ""
                    params[k] = v
                }
            }
            ParsedUrl(
                scheme = uri.scheme ?: "http",
                host = uri.host ?: "",
                port = if (uri.port != -1) uri.port.toString() else "default",
                path = uri.path ?: "/",
                queryParams = params,
                fragment = uri.fragment ?: ""
            )
        }.getOrDefault(ParsedUrl("", "", "", "", emptyMap(), ""))
    }

    // 13. Regex Evaluator
    data class RegexEvaluation(
        val isMatch: Boolean,
        val matchCount: Int,
        val matches: List<String>
    )

    fun evaluateRegex(pattern: String, text: String): RegexEvaluation {
        return runCatching {
            val r = Regex(pattern)
            val allMatches = r.findAll(text).map { it.value }.toList()
            RegexEvaluation(allMatches.isNotEmpty(), allMatches.size, allMatches)
        }.getOrElse {
            RegexEvaluation(false, 0, emptyList())
        }
    }

    // 14. AES 128/256 Offline Text Encryption & Decryption
    fun aesEncrypt(text: String, secretKey: String): String {
        return runCatching {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.toByteArray(StandardCharsets.UTF_8))
            val keySpec = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
            val iv = ByteArray(16) { 0 }
            val ivSpec = javax.crypto.spec.IvParameterSpec(iv)
            val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, keySpec, ivSpec)
            val encrypted = cipher.doFinal(text.toByteArray(StandardCharsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }.getOrElse { "Encryption failed: ${it.message}" }
    }

    fun aesDecrypt(cipherText: String, secretKey: String): String {
        return runCatching {
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(secretKey.toByteArray(StandardCharsets.UTF_8))
            val keySpec = javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
            val iv = ByteArray(16) { 0 }
            val ivSpec = javax.crypto.spec.IvParameterSpec(iv)
            val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, ivSpec)
            val decoded = Base64.decode(cipherText.trim(), Base64.DEFAULT)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted, StandardCharsets.UTF_8)
        }.getOrElse { "Decryption failed: Incorrect password or invalid ciphertext" }
    }

    // 15. Password Strength Evaluation
    data class PasswordStrength(
        val scorePercent: Int, // 0 to 100
        val rating: String,
        val crackTime: String,
        val checks: List<Pair<String, Boolean>>
    )

    fun evaluatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength(0, "Empty", "Instant", emptyList())
        var score = 0
        val checks = mutableListOf<Pair<String, Boolean>>()

        val len = password.length
        val hasLength = len >= 12
        checks.add("Length ≥ 12 characters" to hasLength)
        if (hasLength) score += 30 else if (len >= 8) score += 15

        val hasUpper = password.any { it.isUpperCase() }
        checks.add("Contains Uppercase letter" to hasUpper)
        if (hasUpper) score += 20

        val hasLower = password.any { it.isLowerCase() }
        checks.add("Contains Lowercase letter" to hasLower)
        if (hasLower) score += 15

        val hasDigit = password.any { it.isDigit() }
        checks.add("Contains Numbers" to hasDigit)
        if (hasDigit) score += 20

        val hasSpecial = password.any { !it.isLetterOrDigit() }
        checks.add("Contains Symbols / Special chars" to hasSpecial)
        if (hasSpecial) score += 15

        score = score.coerceIn(0, 100)
        val rating = when {
            score >= 80 -> "Very Strong"
            score >= 60 -> "Strong"
            score >= 40 -> "Moderate"
            else -> "Weak"
        }

        val crackTime = when {
            score >= 90 -> "Centuries"
            score >= 80 -> "Several Years"
            score >= 60 -> "Few Months"
            score >= 40 -> "Few Days"
            score >= 20 -> "Minutes"
            else -> "Seconds"
        }

        return PasswordStrength(score, rating, crackTime, checks)
    }

    // 16. Color Palette Generation
    fun generatePalette(hex: String): List<String> {
        val details = parseColor(hex) ?: return listOf(hex)
        val h = details.h
        val s = details.s / 100.0
        val l = details.l / 100.0

        fun toHex(rgb: Triple<Int, Int, Int>) = "#%02X%02X%02X".format(rgb.first, rgb.second, rgb.third)

        return listOf(
            details.hex,
            details.complementaryHex,
            toHex(hslToRgb(((h + 30.0) % 360.0), s, l)), // Analogous 1
            toHex(hslToRgb(((h + 330.0) % 360.0), s, l)), // Analogous 2
            toHex(hslToRgb(((h + 120.0) % 360.0), s, l)), // Triadic 1
            toHex(hslToRgb(((h + 240.0) % 360.0), s, l))  // Triadic 2
        )
    }
}
