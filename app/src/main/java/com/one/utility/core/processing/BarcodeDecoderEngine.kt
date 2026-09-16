package com.one.utility.core.processing

sealed class QrPayloadType {
    data class Url(val url: String) : QrPayloadType()
    data class Wifi(val ssid: String, val pass: String, val type: String) : QrPayloadType()
    data class Phone(val number: String) : QrPayloadType()
    data class Email(val address: String) : QrPayloadType()
    data class PlainText(val text: String) : QrPayloadType()
}

class BarcodeDecoderEngine {

    /**
     * Inspects decoded string from QR or barcode and parses semantic structure.
     * Prevents auto-launching malicious external URLs.
     */
    fun parsePayload(raw: String): QrPayloadType {
        val trimmed = raw.trim()

        return when {
            trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true) -> {
                QrPayloadType.Url(trimmed)
            }

            trimmed.startsWith("WIFI:", ignoreCase = true) -> {
                val ssid = Regex("""S:([^;]+)""").find(trimmed)?.groupValues?.get(1) ?: ""
                val pass = Regex("""P:([^;]+)""").find(trimmed)?.groupValues?.get(1) ?: ""
                val type = Regex("""T:([^;]+)""").find(trimmed)?.groupValues?.get(1) ?: "WPA"
                QrPayloadType.Wifi(ssid, pass, type)
            }

            trimmed.startsWith("tel:", ignoreCase = true) -> {
                QrPayloadType.Phone(trimmed.removePrefix("tel:").removePrefix("TEL:"))
            }

            trimmed.startsWith("mailto:", ignoreCase = true) -> {
                QrPayloadType.Email(trimmed.removePrefix("mailto:").removePrefix("MAILTO:"))
            }

            else -> QrPayloadType.PlainText(trimmed)
        }
    }
}
