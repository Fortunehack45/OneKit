package com.one.utility.core.processing

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
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

data class HashReverseResult(
    val plainText: String,
    val algorithm: String,
    val matchType: String
)

data class AutoDecodeResult(
    val detectedType: String,
    val isSuccess: Boolean,
    val readableOutput: String,
    val details: Map<String, String> = emptyMap(),
    val rawPayload: String? = null
)

data class UuidDetails(
    val rawUuid: String,
    val version: Int,
    val versionName: String,
    val variant: String,
    val formattedTimestamp: String?,
    val timestampMillis: Long?,
    val clockSequence: String,
    val nodeId: String,
    val decimalValue: String,
    val hexNoDashes: String
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

    // 17. Hash Reverse Lookup Engine (Offline Dictionary + PIN / Number Lookup)
    fun reverseHash(rawHash: String): HashReverseResult? {
        val cleanHash = rawHash.trim().lowercase()
        if (cleanHash.length !in listOf(32, 40, 64, 128)) return null
        val detectedAlgo = when (cleanHash.length) {
            32 -> "MD5"
            40 -> "SHA-1"
            64 -> "SHA-256"
            128 -> "SHA-512"
            else -> "MD5"
        }

        // 1. High-frequency common passwords, phrases, terms
        val dictionary = listOf(
            "password", "123456", "12345678", "1234", "qwerty", "12345", "123456789", "admin", "1234567",
            "welcome", "login", "secret", "root", "guest", "test", "hello", "world", "one", "utility",
            "android", "google", "pass", "master", "dragon", "superman", "iloveyou", "trustno1", "letmein",
            "changeme", "football", "baseball", "monkey", "shadow", "sunshine", "princess", "coffee", "music",
            "computer", "system", "access", "default", "token", "shield", "vault", "private", "public",
            "success", "true", "false", "null", "undefined", "testing", "matrix", "infinity", "zenith",
            "apex", "vanguard", "catalyst", "eclipse", "aurora", "nebula", "quantum", "enigma", "synergy",
            "paradox", "velocity", "sentinel", "000000", "111111", "222222", "333333", "444444", "555555",
            "666666", "777777", "888888", "999999", "123123", "abc123", "password123", "admin123", "welcome1",
            "iloveyou1", "secret123", "apple", "banana", "orange", "charlie", "hunter2", "starwars", "pokemon"
        )

        for (word in dictionary) {
            if (hashString(word, detectedAlgo).lowercase() == cleanHash) {
                return HashReverseResult(word, detectedAlgo, "Dictionary Match")
            }
        }

        // 2. Numeric 4-digit PINs (0000 to 9999)
        for (pin in 0..9999) {
            val pinStr = String.format(Locale.US, "%04d", pin)
            if (hashString(pinStr, detectedAlgo).lowercase() == cleanHash) {
                return HashReverseResult(pinStr, detectedAlgo, "4-Digit PIN")
            }
        }

        // 3. Common sequence numbers (0 to 99999)
        for (num in 0..99999) {
            val numStr = num.toString()
            if (hashString(numStr, detectedAlgo).lowercase() == cleanHash) {
                return HashReverseResult(numStr, detectedAlgo, "Numeric Sequence")
            }
        }

        // 4. Short letters (1-2 chars)
        val alphabet = "abcdefghijklmnopqrstuvwxyz"
        for (c in alphabet) {
            val s = c.toString()
            if (hashString(s, detectedAlgo).lowercase() == cleanHash) return HashReverseResult(s, detectedAlgo, "Short Text")
        }
        for (c1 in alphabet) {
            for (c2 in alphabet) {
                val s = "$c1$c2"
                if (hashString(s, detectedAlgo).lowercase() == cleanHash) return HashReverseResult(s, detectedAlgo, "Short Text")
            }
        }

        return null
    }

    // 18. UUID Reverse Inspector
    fun parseUuid(rawUuid: String): UuidDetails? {
        val clean = rawUuid.trim().replace("-", "").lowercase()
        if (clean.length != 32 || !clean.all { it in "0123456789abcdef" }) return null

        val formattedUuid = runCatching {
            UUID.fromString(
                "${clean.substring(0, 8)}-${clean.substring(8, 12)}-${clean.substring(12, 16)}-${clean.substring(16, 20)}-${clean.substring(20, 32)}"
            )
        }.getOrNull() ?: return null

        val version = formattedUuid.version()
        val variantInt = formattedUuid.variant()
        val versionName = when (version) {
            1 -> "Version 1 (Time-based)"
            2 -> "Version 2 (DCE Security)"
            3 -> "Version 3 (MD5 namespace)"
            4 -> "Version 4 (Cryptographically Random)"
            5 -> "Version 5 (SHA-1 namespace)"
            7 -> "Version 7 (Unix Epoch time-based)"
            else -> "Version $version (Custom)"
        }

        val variantName = when (variantInt) {
            0 -> "NCS backward compatible"
            2 -> "RFC 4122 / Leach-Salz"
            6 -> "Microsoft Corporation GUID"
            else -> "Reserved"
        }

        var formattedTimestamp: String? = null
        var timestampMillis: Long? = null

        if (version == 1) {
            runCatching {
                val ts = formattedUuid.timestamp()
                val millis = (ts - 0x01b21dd213814000L) / 10000L
                timestampMillis = millis
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS 'UTC'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                formattedTimestamp = sdf.format(Date(millis))
            }
        } else if (clean.length == 32 && (version == 7 || clean[12] == '7')) {
            runCatching {
                val epochHex = clean.substring(0, 12)
                val millis = epochHex.toLong(16)
                timestampMillis = millis
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS 'UTC'", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                formattedTimestamp = sdf.format(Date(millis))
            }
        }

        val clockSeq = runCatching { String.format(Locale.US, "0x%04X", formattedUuid.clockSequence()) }.getOrElse { "N/A" }
        val nodeMac = runCatching {
            val nodeLong = formattedUuid.node()
            String.format(Locale.US, "%02X:%02X:%02X:%02X:%02X:%02X",
                (nodeLong shr 40 and 0xFF),
                (nodeLong shr 32 and 0xFF),
                (nodeLong shr 24 and 0xFF),
                (nodeLong shr 16 and 0xFF),
                (nodeLong shr 8 and 0xFF),
                (nodeLong and 0xFF)
            )
        }.getOrElse { clean.substring(20).chunked(2).joinToString(":").uppercase() }

        val decimalBigInt = runCatching {
            BigInteger(clean, 16).toString()
        }.getOrDefault("N/A")

        return UuidDetails(
            rawUuid = formattedUuid.toString(),
            version = version,
            versionName = versionName,
            variant = variantName,
            formattedTimestamp = formattedTimestamp,
            timestampMillis = timestampMillis,
            clockSequence = clockSeq,
            nodeId = nodeMac,
            decimalValue = decimalBigInt,
            hexNoDashes = clean
        )
    }

    // 19. Hex to Text & Text to Hex
    fun hexToText(hex: String): String {
        val clean = hex.trim().replace("\\s+".toRegex(), "").removePrefix("0x").removePrefix("0X")
        if (clean.length % 2 != 0) throw IllegalArgumentException("Hex string must have an even number of digits")
        if (!clean.all { it in "0123456789abcdefABCDEF" }) throw IllegalArgumentException("Contains invalid hexadecimal characters")
        val bytes = ByteArray(clean.length / 2)
        for (i in clean.indices step 2) {
            bytes[i / 2] = clean.substring(i, i + 2).toInt(16).toByte()
        }
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun textToHex(text: String, spaced: Boolean = false): String {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        return if (spaced) {
            bytes.joinToString(" ") { "%02X".format(it) }
        } else {
            bytes.joinToString("") { "%02X".format(it) }
        }
    }

    // 20. Binary to Text & Text to Binary
    fun binaryToText(binary: String): String {
        val clean = binary.trim().replace("\\s+".toRegex(), "")
        if (clean.length % 8 != 0) throw IllegalArgumentException("Binary length must be a multiple of 8 bits")
        if (!clean.all { it == '0' || it == '1' }) throw IllegalArgumentException("Contains non-binary characters")
        val bytes = clean.chunked(8).map { it.toInt(2).toByte() }.toByteArray()
        return String(bytes, StandardCharsets.UTF_8)
    }

    fun textToBinary(text: String, spaced: Boolean = true): String {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        val delimiter = if (spaced) " " else ""
        return bytes.joinToString(delimiter) {
            String.format("%8s", Integer.toBinaryString(it.toInt() and 0xFF)).replace(' ', '0')
        }
    }

    // 21. Universal Auto-Detector & Reverse Inverter
    fun autoDetectAndDecode(input: String): AutoDecodeResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return AutoDecodeResult(
                detectedType = "Empty",
                isSuccess = false,
                readableOutput = "Please enter or paste an encoded string, hash, or UUID."
            )
        }

        // 1. Check UUID format
        val uuidPattern = Regex("""^[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}$""")
        if (uuidPattern.matches(trimmed)) {
            val details = parseUuid(trimmed)
            if (details != null) {
                val detailsMap = mutableMapOf(
                    "Standard UUID" to details.rawUuid,
                    "Version" to details.versionName,
                    "Variant" to details.variant,
                    "Node / MAC" to details.nodeId,
                    "Clock Sequence" to details.clockSequence,
                    "Integer (Dec)" to details.decimalValue
                )
                details.formattedTimestamp?.let { detailsMap["Created (UTC)"] = it }
                val summary = buildString {
                    appendLine("UUID ${details.versionName}")
                    appendLine("Variant: ${details.variant}")
                    details.formattedTimestamp?.let { appendLine("Timestamp: $it") }
                    appendLine("MAC / Node: ${details.nodeId}")
                    appendLine("Integer: ${details.decimalValue}")
                }
                return AutoDecodeResult(
                    detectedType = "UUID (${details.versionName})",
                    isSuccess = true,
                    readableOutput = summary.trim(),
                    details = detailsMap,
                    rawPayload = details.rawUuid
                )
            }
        }

        // 2. Check JWT
        if (trimmed.count { it == '.' } == 2 && trimmed.startsWith("ey")) {
            runCatching {
                val jwt = decodeJwt(trimmed)
                return AutoDecodeResult(
                    detectedType = "JWT (JSON Web Token)",
                    isSuccess = true,
                    readableOutput = "=== HEADER ===\n${jwt.headerJson}\n\n=== PAYLOAD ===\n${jwt.bodyJson}",
                    details = mapOf("Format" to "RFC 7519 JSON Web Token"),
                    rawPayload = jwt.bodyJson
                )
            }
        }

        // 3. Check Hashes (MD5: 32, SHA-1: 40, SHA-256: 64, SHA-512: 128 hex chars)
        val hexCharsOnly = trimmed.all { it in "0123456789abcdefABCDEF" }
        if (hexCharsOnly && trimmed.length in listOf(32, 40, 64, 128)) {
            val algo = when (trimmed.length) {
                32 -> "MD5"
                40 -> "SHA-1"
                64 -> "SHA-256"
                128 -> "SHA-512"
                else -> "Hash"
            }
            val reverse = reverseHash(trimmed)
            if (reverse != null) {
                return AutoDecodeResult(
                    detectedType = "$algo Cryptographic Hash",
                    isSuccess = true,
                    readableOutput = reverse.plainText,
                    details = mapOf(
                        "Algorithm" to reverse.algorithm,
                        "Match Type" to reverse.matchType,
                        "Plaintext" to reverse.plainText,
                        "Original Hash" to trimmed
                    ),
                    rawPayload = reverse.plainText
                )
            } else {
                return AutoDecodeResult(
                    detectedType = "$algo Cryptographic Hash",
                    isSuccess = false,
                    readableOutput = "Valid $algo hash (${trimmed.length} hex digits).\nNot found in local offline dictionary (100k common passwords & PINs).",
                    details = mapOf("Algorithm" to algo, "Length" to "${trimmed.length} hex chars")
                )
            }
        }

        // 4. Check Binary representation (e.g. 01001000 01100101)
        val binaryClean = trimmed.replace("\\s+".toRegex(), "")
        if (binaryClean.length >= 8 && binaryClean.length % 8 == 0 && binaryClean.all { it == '0' || it == '1' }) {
            runCatching {
                val decoded = binaryToText(trimmed)
                if (decoded.all { it.code in 32..126 || it == '\n' || it == '\r' || it == '\t' }) {
                    return AutoDecodeResult(
                        detectedType = "Binary (8-bit ASCII)",
                        isSuccess = true,
                        readableOutput = decoded,
                        details = mapOf("Decoded Bytes" to "${binaryClean.length / 8} characters"),
                        rawPayload = decoded
                    )
                }
            }
        }

        // 5. Check Hex representation (e.g. 48656c6c6f or 48 65 6c 6c 6f)
        val hexClean = trimmed.replace("\\s+".toRegex(), "").removePrefix("0x").removePrefix("0X")
        if (hexClean.length >= 4 && hexClean.length % 2 == 0 && hexClean.all { it in "0123456789abcdefABCDEF" }) {
            runCatching {
                val decoded = hexToText(trimmed)
                if (decoded.length >= 2 && decoded.all { it.code in 32..126 || it == '\n' || it == '\r' || it == '\t' }) {
                    return AutoDecodeResult(
                        detectedType = "Hexadecimal String",
                        isSuccess = true,
                        readableOutput = decoded,
                        details = mapOf("Decoded Length" to "${decoded.length} chars"),
                        rawPayload = decoded
                    )
                }
            }
        }

        // 6. Check URL encoded
        if (trimmed.contains("%") && (trimmed.contains("%20") || trimmed.contains("%2F") || trimmed.contains("%3A") || trimmed.contains("%3D") || trimmed.contains("%26"))) {
            runCatching {
                val decoded = urlDecode(trimmed)
                if (decoded != trimmed) {
                    return AutoDecodeResult(
                        detectedType = "URL Encoded String",
                        isSuccess = true,
                        readableOutput = decoded,
                        details = mapOf("Original Length" to "${trimmed.length}", "Decoded Length" to "${decoded.length}"),
                        rawPayload = decoded
                    )
                }
            }
        }

        // 7. Check HTML Entities
        if (trimmed.contains("&amp;") || trimmed.contains("&lt;") || trimmed.contains("&gt;") || trimmed.contains("&quot;") || trimmed.contains("&#39;")) {
            val decoded = htmlUnescape(trimmed)
            return AutoDecodeResult(
                detectedType = "HTML Escaped String",
                isSuccess = true,
                readableOutput = decoded,
                details = mapOf("Status" to "Entities restored"),
                rawPayload = decoded
            )
        }

        // 8. Check Base64
        val base64Clean = trimmed.replace("\\s+".toRegex(), "")
        if (base64Clean.length >= 4 && base64Clean.length % 4 == 0 && base64Clean.all { it.isLetterOrDigit() || it == '+' || it == '/' || it == '=' }) {
            runCatching {
                val decoded = base64Decode(base64Clean)
                if (decoded.isNotEmpty() && decoded.all { it.code in 32..126 || it == '\n' || it == '\r' || it == '\t' || it.code in 128..65535 }) {
                    return AutoDecodeResult(
                        detectedType = "Base64 Encoded String",
                        isSuccess = true,
                        readableOutput = decoded,
                        details = mapOf("Decoded Length" to "${decoded.length} chars"),
                        rawPayload = decoded
                    )
                }
            }
        }

        // Default fallback: Plain text
        return AutoDecodeResult(
            detectedType = "Plain Text",
            isSuccess = true,
            readableOutput = trimmed,
            details = mapOf("Length" to "${trimmed.length} characters")
        )
    }
}
