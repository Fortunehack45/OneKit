package com.one.utility

import com.one.utility.core.processing.BarcodeDecoderEngine
import com.one.utility.core.processing.QrPayloadType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeDecoderTest {

    private val decoder = BarcodeDecoderEngine()

    @Test
    fun testUrlPayload() {
        val payload = decoder.parsePayload("https://github.com/one-utility")
        assertTrue(payload is QrPayloadType.Url)
        assertEquals("https://github.com/one-utility", (payload as QrPayloadType.Url).url)
    }

    @Test
    fun testWifiPayload() {
        val payload = decoder.parsePayload("WIFI:T:WPA;S:HomeNetwork;P:MySecretPass;;")
        assertTrue(payload is QrPayloadType.Wifi)
        val wifi = payload as QrPayloadType.Wifi
        assertEquals("HomeNetwork", wifi.ssid)
        assertEquals("MySecretPass", wifi.pass)
        assertEquals("WPA", wifi.type)
    }

    @Test
    fun testPhonePayload() {
        val payload = decoder.parsePayload("tel:+1234567890")
        assertTrue(payload is QrPayloadType.Phone)
        assertEquals("+1234567890", (payload as QrPayloadType.Phone).number)
    }

    @Test
    fun testPlainTextPayload() {
        val payload = decoder.parsePayload("Simple note text")
        assertTrue(payload is QrPayloadType.PlainText)
        assertEquals("Simple note text", (payload as QrPayloadType.PlainText).text)
    }
}
