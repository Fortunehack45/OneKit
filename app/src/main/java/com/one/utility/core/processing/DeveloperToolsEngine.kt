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
}
