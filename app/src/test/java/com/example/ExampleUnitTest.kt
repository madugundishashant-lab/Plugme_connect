package com.example

import com.example.model.Contact
import com.example.model.GameEntry
import com.example.model.PlatformCatalog
import com.example.qr.QrCodeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPlatformUrlBuilding() {
        // Strip '@' and append base URL
        val instagramUrl = PlatformCatalog.buildUrl("ig", "@shashant_07")
        assertEquals("https://instagram.com/shashant_07", instagramUrl)

        // Web URL adds https if missing
        val websiteUrl = PlatformCatalog.buildUrl("wb", "myportfolio.dev")
        assertEquals("https://myportfolio.dev", websiteUrl)

        // Email prepends mailto:
        val emailUrl = PlatformCatalog.buildUrl("em", "mspace119@gmail.com")
        assertEquals("mailto:mspace119@gmail.com", emailUrl)

        // Phone prepends tel:
        val phoneUrl = PlatformCatalog.buildUrl("ph", "+1 (234) 567-890")
        assertEquals("tel:+1234567890", phoneUrl)
    }

    @Test
    fun testContactSlimmingAndInitials() {
        val contact = Contact(
            n = "Shashant Madugundi",
            u = "shashant_07",
            e = "mspace119@gmail.com",
            a = 17,
            g = "Male",
            platforms = mapOf("ig" to "shashant_07", "tw" to "   "),
            gms = listOf(GameEntry("Valorant", "Ace#007"), GameEntry("", ""))
        )

        assertEquals("SM", contact.initials)

        val slim = contact.buildSlim()
        assertEquals(1, slim.platforms.size)
        assertTrue(slim.platforms.containsKey("ig"))
        assertEquals(1, slim.gms.size)
        assertEquals("Valorant", slim.gms[0].n)
    }

    @Test
    fun testQrEncodingAndDecodingRoundTrip() {
        val contact = Contact(
            n = "Alex Rivera",
            u = "arivera",
            e = "alex@cyberdeck.io",
            a = 21,
            g = "Non-binary",
            platforms = mapOf("dc" to "arivera#1337"),
            gms = listOf(GameEntry("Valorant", "ViperMain#404"))
        )

        val shareUrl = QrCodeHelper.encodeProfileToUrl(contact)
        assertTrue(shareUrl.startsWith("https://plugmeconnect.app/?d="))

        val decoded = QrCodeHelper.decodeFromUrlOrRaw(shareUrl)
        assertNotNull(decoded)
        assertEquals("Alex Rivera", decoded?.n)
        assertEquals("arivera", decoded?.u)
        assertEquals(21, decoded?.a)
        assertEquals("Non-binary", decoded?.g)
        assertEquals("arivera#1337", decoded?.platforms?.get("dc"))
        assertEquals("Valorant", decoded?.gms?.get(0)?.n)
    }
}
