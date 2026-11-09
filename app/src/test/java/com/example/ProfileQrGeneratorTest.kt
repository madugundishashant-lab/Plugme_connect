package com.example

import com.example.data.local.SavedCardEntity
import com.example.model.Contact
import com.example.model.GameEntry
import com.example.qr.ProfileQrGenerator
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileQrGeneratorTest {

    @Test
    fun testProfileUrlEncodingAndDecoding() {
        val sampleProfile = Contact(
            n = "Shashant Madugundi",
            u = "shashant_07",
            e = "mspace119@gmail.com",
            title = "Principal Engineer",
            company = "Plugme Technologies",
            phone = "+1 (555) 789-2024",
            a = 17,
            g = "Male",
            platforms = mapOf(
                "ig" to "shashant_07",
                "gh" to "shashant",
                "tw" to "shashant07"
            ),
            gms = listOf(
                GameEntry("Valorant", "Ace#007"),
                GameEntry("Chess.com", "GrandmasterShash")
            )
        )

        val shareUrl = ProfileQrGenerator.createProfileShareUrl(sampleProfile)
        assertTrue("Share URL must start with deep link prefix", shareUrl.startsWith("https://plugmeconnect.app/?d="))

        val decoded = ProfileQrGenerator.decodeProfileFromPayload(shareUrl)
        assertNotNull("Decoded contact must not be null", decoded)
        assertEquals("Shashant Madugundi", decoded?.n)
        assertEquals("shashant_07", decoded?.u)
        assertEquals("mspace119@gmail.com", decoded?.e)
        assertEquals("Principal Engineer", decoded?.title)
        assertEquals("Plugme Technologies", decoded?.company)
        assertEquals("+1 (555) 789-2024", decoded?.phone)
        assertEquals(17, decoded?.a)
        assertEquals("Male", decoded?.g)
        assertEquals("shashant_07", decoded?.platforms?.get("ig"))
        assertEquals("shashant", decoded?.platforms?.get("gh"))
        assertEquals("Ace#007", decoded?.gms?.get(0)?.i)
    }

    @Test
    fun testEncodeToBitMatrixWithZxing() {
        val profile = Contact(
            n = "Alex Rivera",
            u = "arivera",
            e = "alex@cyberdeck.io",
            title = "Security Architect",
            company = "Cyberdeck Systems",
            phone = "+1 (555) 345-9871",
            a = 21,
            g = "Non-binary",
            platforms = mapOf("dc" to "arivera#1337"),
            gms = listOf(GameEntry("Valorant", "ViperMain#404"))
        )

        val bitMatrix = ProfileQrGenerator.encodeToBitMatrix(
            contact = profile,
            widthPx = 256,
            heightPx = 256,
            errorCorrectionLevel = ErrorCorrectionLevel.H
        )
        assertNotNull(bitMatrix)
        assertEquals(256, bitMatrix.width)
        assertEquals(256, bitMatrix.height)

        var blackModules = 0
        for (y in 0 until 256) {
            for (x in 0 until 256) {
                if (bitMatrix.get(x, y)) blackModules++
            }
        }
        assertTrue("QR code must have black modules", blackModules > 100)
    }

    @Test
    fun testMinimalProfileZxingEncoding() {
        val minimal = Contact(
            n = "Zoe",
            u = "zoe"
        )
        val bitMatrix = ProfileQrGenerator.encodeToBitMatrix(minimal, widthPx = 128, heightPx = 128)
        assertNotNull(bitMatrix)
        assertEquals(128, bitMatrix.width)
        assertEquals(128, bitMatrix.height)
    }

    @Test
    fun testVCardParsing() {
        val vcard = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Dr. Aris Thorne
            TITLE:Lead Quantum Researcher
            ORG:Quantum Dynamics Inc
            EMAIL:aris.thorne@quantum.org
            TEL:+1-555-0199
            URL:https://github.com/aristhorne
            X-SOCIALPROFILE:https://instagram.com/aris_thorne
            END:VCARD
        """.trimIndent()

        val parsed = ProfileQrGenerator.decodeProfileFromPayload(vcard)
        assertNotNull(parsed)
        assertEquals("Dr. Aris Thorne", parsed?.n)
        assertEquals("Lead Quantum Researcher", parsed?.title)
        assertEquals("Quantum Dynamics Inc", parsed?.company)
        assertEquals("aris.thorne@quantum.org", parsed?.e)
        assertEquals("+1-555-0199", parsed?.effectivePhone)
        assertEquals("aristhorne", parsed?.platforms?.get("gh"))
        assertEquals("aris_thorne", parsed?.platforms?.get("ig"))
    }

    @Test
    fun testMeCardParsing() {
        val mecard = "MECARD:N:Kenji Sato;TEL:+819012345678;EMAIL:kenji@tokyodesign.jp;URL:https://x.com/kenjisato;;"
        val parsed = ProfileQrGenerator.decodeProfileFromPayload(mecard)
        assertNotNull(parsed)
        assertEquals("Kenji Sato", parsed?.n)
        assertEquals("kenji@tokyodesign.jp", parsed?.e)
        assertEquals("+819012345678", parsed?.effectivePhone)
        assertEquals("kenjisato", parsed?.platforms?.get("tw"))
    }

    @Test
    fun testContactFieldsForDeviceContactSaving() {
        val sample = Contact(
            n = "Elena Rostova",
            u = "elena_r",
            e = "elena@plugmeconnect.app",
            title = "Chief Design Officer",
            company = "Studio Rostova",
            phone = "+15551234567",
            a = 24,
            g = "Female",
            platforms = mapOf("ph" to "+15551234567", "ig" to "elena.r", "gh" to "elenarostova"),
            gms = listOf(GameEntry("Steam", "ElenaSteam"))
        )

        val displayName = sample.n.ifBlank { sample.u }
        assertEquals("Elena Rostova", displayName)
        assertEquals("Chief Design Officer", sample.title)
        assertEquals("Studio Rostova", sample.company)
        assertEquals("elena@plugmeconnect.app", sample.e)
        assertEquals("+15551234567", sample.effectivePhone)
        assertTrue(sample.platforms.containsKey("ig"))
        assertEquals("ElenaSteam", sample.gms.first().i)
    }

    @Test
    fun testRoomSavedCardEntityConversion() {
        val original = Contact(
            n = "Marcus Vance",
            u = "mvance",
            e = "marcus@vancecorp.com",
            title = "Creative Director",
            company = "Vance Creatives",
            phone = "+1 (555) 998-1122",
            a = 28,
            g = "Male",
            platforms = mapOf("li" to "marcus-vance", "tw" to "mvance_tech"),
            gms = listOf(GameEntry("Apex Legends", "VanceApex#99"))
        )

        val entity = SavedCardEntity.fromContact(original, isPersonalCard = false)
        assertEquals("mvance", entity.id)
        assertEquals("Marcus Vance", entity.name)
        assertEquals("mvance", entity.username)
        assertEquals("Creative Director", entity.title)
        assertEquals("Vance Creatives", entity.company)
        assertEquals("+1 (555) 998-1122", entity.phone)
        assertEquals(28, entity.age)

        val convertedBack = entity.toContact()
        assertEquals(original.n, convertedBack.n)
        assertEquals(original.u, convertedBack.u)
        assertEquals(original.e, convertedBack.e)
        assertEquals(original.title, convertedBack.title)
        assertEquals(original.company, convertedBack.company)
        assertEquals(original.effectivePhone, convertedBack.effectivePhone)
        assertEquals(original.a, convertedBack.a)
        assertEquals(original.g, convertedBack.g)
        assertEquals("marcus-vance", convertedBack.platforms["li"])
        assertEquals("VanceApex#99", convertedBack.gms.first().i)
    }

    @Test
    fun testPersonalProfilePersistenceAndQrPayloadIntegrity() {
        // Complete test for user requirement:
        // Input name, job title, company, email, phone number, store in Room and generate QR code
        val userProfile = Contact(
            n = "Sarah Connor",
            u = "sconnor",
            e = "sarah@cyberdyne.org",
            title = "Lead Security Operations",
            company = "Resistance HQ",
            phone = "+1 310-555-0144"
        )

        // 1. Room entity serialization
        val roomEntity = SavedCardEntity.fromContact(userProfile, isPersonalCard = true)
        assertEquals("me", roomEntity.id)
        assertTrue(roomEntity.isPersonalCard)
        assertEquals("Sarah Connor", roomEntity.name)
        assertEquals("Lead Security Operations", roomEntity.title)
        assertEquals("Resistance HQ", roomEntity.company)
        assertEquals("sarah@cyberdyne.org", roomEntity.email)
        assertEquals("+1 310-555-0144", roomEntity.phone)

        // 2. Room entity deserialization (persisting between launches)
        val restoredFromRoom = roomEntity.toContact()
        assertEquals(userProfile.n, restoredFromRoom.n)
        assertEquals(userProfile.title, restoredFromRoom.title)
        assertEquals(userProfile.company, restoredFromRoom.company)
        assertEquals(userProfile.e, restoredFromRoom.e)
        assertEquals(userProfile.phone, restoredFromRoom.effectivePhone)

        // 3. QR code generation from profile
        val qrUrl = ProfileQrGenerator.createProfileShareUrl(restoredFromRoom)
        val decodedFromQr = ProfileQrGenerator.decodeProfileFromPayload(qrUrl)
        assertNotNull(decodedFromQr)
        assertEquals("Sarah Connor", decodedFromQr?.n)
        assertEquals("Lead Security Operations", decodedFromQr?.title)
        assertEquals("Resistance HQ", decodedFromQr?.company)
        assertEquals("sarah@cyberdyne.org", decodedFromQr?.e)
        assertEquals("+1 310-555-0144", decodedFromQr?.effectivePhone)

        // 4. ZXing BitMatrix generation
        val bitMatrix = ProfileQrGenerator.encodeToBitMatrix(restoredFromRoom, widthPx = 256, heightPx = 256)
        assertNotNull(bitMatrix)
        assertEquals(256, bitMatrix.width)
        assertEquals(256, bitMatrix.height)
    }
}
