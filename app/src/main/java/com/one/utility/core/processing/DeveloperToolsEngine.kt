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

    // 3. URL Encoding
    fun urlEncode(text: String): String = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
    fun urlDecode(text: String): String = URLDecoder.decode(text, StandardCharsets.UTF_8.toString())

    // 4. JWT Inspector
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

    // 5. Hashes
    fun hashString(input: String, algorithm: String = "SHA-256"): String {
        val digest = MessageDigest.getInstance(algorithm)
        val bytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // 6. UUID & Random Generators
    fun generateUuid(): String = UUID.randomUUID().toString()

    // 7. Unix Timestamp
    fun timestampToDate(timestampSeconds: Long): String {
        val date = Date(timestampSeconds * 1000)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())
        return format.format(date)
    }

    fun currentTimestamp(): Long = System.currentTimeMillis() / 1000
}
