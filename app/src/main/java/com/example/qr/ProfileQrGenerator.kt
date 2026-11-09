package com.example.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.Contact
import com.example.model.PlatformCatalog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/**
 * QR Code Generation Utility for Plugme Connect.
 * Uses the ZXing library to convert user profile information into a high-quality,
 * customizable, and shareable QR code image.
 */
object ProfileQrGenerator {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    /**
     * Pure Kotlin URL-safe Base64 encoder/decoder (RFC 4648).
     * Works consistently across Android runtime and local host JVM environments.
     */
    object Base64Url {
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
        private val DECODE_TABLE = IntArray(256) { -1 }.apply {
            for (i in ALPHABET.indices) {
                this[ALPHABET[i].code] = i
            }
            this['+'.code] = 62
            this['/'.code] = 63
        }

        fun encode(data: ByteArray): String {
            val out = StringBuilder((data.size * 4 + 2) / 3)
            var i = 0
            while (i < data.size) {
                val b0 = data[i++].toInt() and 0xFF
                val b1 = if (i < data.size) data[i++].toInt() and 0xFF else -1
                val b2 = if (i < data.size) data[i++].toInt() and 0xFF else -1

                out.append(ALPHABET[b0 shr 2])
                if (b1 != -1) {
                    out.append(ALPHABET[((b0 and 0x03) shl 4) or (b1 shr 4)])
                    if (b2 != -1) {
                        out.append(ALPHABET[((b1 and 0x0F) shl 2) or (b2 shr 6)])
                        out.append(ALPHABET[b2 and 0x3F])
                    } else {
                        out.append(ALPHABET[(b1 and 0x0F) shl 2])
                    }
                } else {
                    out.append(ALPHABET[(b0 and 0x03) shl 4])
                }
            }
            return out.toString()
        }

        fun decode(s: String): ByteArray {
            val clean = s.trim().filter { it != '=' && it != '\n' && it != '\r' }
            val out = java.io.ByteArrayOutputStream(clean.length * 3 / 4)
            var buffer = 0
            var bits = 0
            for (ch in clean) {
                val code = ch.code
                val value = if (code in 0..255) DECODE_TABLE[code] else -1
                if (value >= 0) {
                    buffer = (buffer shl 6) or value
                    bits += 6
                    if (bits >= 8) {
                        bits -= 8
                        out.write((buffer shr bits) and 0xFF)
                    }
                }
            }
            return out.toByteArray()
        }
    }

    /**
     * Configuration options for QR code rendering.
     */
    data class QrConfig(
        val widthPx: Int = 512,
        val heightPx: Int = 512,
        val foregroundColor: Int = Color.rgb(0, 170, 255), // Signature Plugme Cyan #00AAFF
        val backgroundColor: Int = Color.WHITE,
        val margin: Int = 3,
        val errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
        val embedCenterBadge: Boolean = true
    )

    /**
     * Serializes a user profile into a shareable deep link URL.
     * Format: https://plugmeconnect.app/?d=<URL_SAFE_BASE64_PAYLOAD>
     */
    fun createProfileShareUrl(contact: Contact): String {
        val slimContact = contact.buildSlim()
        val jsonPayload = json.encodeToString(slimContact)
        val base64Payload = Base64Url.encode(jsonPayload.toByteArray(StandardCharsets.UTF_8))
        return "https://plugmeconnect.app/?d=$base64Payload"
    }

    /**
     * Decodes a profile payload from a deep link, scanned QR raw string,
     * or standard digital business card (vCard, MeCard).
     */
    fun decodeProfileFromPayload(payloadOrUrl: String): Contact? {
        val trimmed = payloadOrUrl.trim()
        if (trimmed.isBlank()) return null

        // 1. Check for standard vCard digital business cards (e.g. BEGIN:VCARD)
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.contains("BEGIN:VCARD", ignoreCase = true)) {
            return parseVCard(trimmed)
        }

        // 2. Check for MeCard format (e.g. MECARD:N:...)
        if (trimmed.startsWith("MECARD:", ignoreCase = true)) {
            return parseMeCard(trimmed)
        }

        // 3. Check for Plugme Connect deep link or query param
        val encodedPayload = when {
            trimmed.contains("?d=") -> trimmed.substringAfter("?d=").substringBefore("&")
            trimmed.contains("&d=") -> trimmed.substringAfter("&d=").substringBefore("&")
            else -> trimmed
        }

        return try {
            val bytes = Base64Url.decode(encodedPayload)
            val decodedJson = String(bytes, StandardCharsets.UTF_8)
            json.decodeFromString<Contact>(decodedJson)
        } catch (_: Exception) {
            try {
                json.decodeFromString<Contact>(encodedPayload)
            } catch (_: Exception) {
                // If the QR is a raw social or web URL, extract it into a contact
                if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
                    val platforms = mutableMapOf<String, String>()
                    extractPlatformFromUrlOrValue(trimmed, platforms)
                    val derivedName = trimmed.substringAfter("://").substringBefore('/').removePrefix("www.")
                    Contact(
                        n = derivedName.replaceFirstChar { it.uppercase() },
                        u = derivedName.lowercase().replace("[^a-z0-9_]".toRegex(), "_").take(15),
                        platforms = platforms
                    )
                } else null
            }
        }
    }

    private fun parseVCard(vcard: String): Contact {
        var name = ""
        var email = ""
        var title = ""
        var company = ""
        var phone = ""
        val platforms = mutableMapOf<String, String>()
        val lines = vcard.lines()

        for (line in lines) {
            val trimmed = line.trim()
            val colonIndex = trimmed.indexOf(':')
            if (colonIndex <= 0) continue

            val tag = trimmed.substring(0, colonIndex).uppercase()
            val value = trimmed.substring(colonIndex + 1).trim()
            if (value.isBlank()) continue

            when {
                tag.startsWith("FN") -> if (name.isBlank()) name = value
                tag.startsWith("N") && name.isBlank() -> {
                    val parts = value.split(";").filter { it.isNotBlank() }
                    name = parts.reversed().joinToString(" ").trim()
                }
                tag.startsWith("TITLE") -> if (title.isBlank()) title = value
                tag.startsWith("ORG") -> if (company.isBlank()) company = value
                tag.startsWith("EMAIL") -> if (email.isBlank()) email = value
                tag.startsWith("TEL") -> {
                    phone = value
                    platforms["ph"] = value
                }
                tag.startsWith("URL") || tag.contains("SOCIAL") || tag.contains("X-") -> {
                    extractPlatformFromUrlOrValue(value, platforms)
                }
            }
        }

        val username = if (name.isNotBlank()) {
            name.lowercase().replace("[^a-z0-9_]".toRegex(), "_").take(15).trim('_')
        } else "contact"

        return Contact(
            n = name.ifBlank { "Scanned Contact" },
            u = username.ifBlank { "contact" },
            e = email,
            title = title,
            company = company,
            phone = phone,
            platforms = platforms
        )
    }

    private fun parseMeCard(mecard: String): Contact {
        var name = ""
        var email = ""
        var phone = ""
        val platforms = mutableMapOf<String, String>()

        val content = mecard.removePrefix("MECARD:").removePrefix("mecard:").trimEnd(';')
        val tokens = content.split(";")
        for (token in tokens) {
            val colon = token.indexOf(':')
            if (colon <= 0) continue
            val tag = token.substring(0, colon).trim().uppercase()
            val value = token.substring(colon + 1).trim()
            if (value.isBlank()) continue

            when (tag) {
                "N" -> name = value.replace(";", " ").trim()
                "EMAIL" -> email = value
                "TEL" -> {
                    phone = value
                    platforms["ph"] = value
                }
                "URL" -> extractPlatformFromUrlOrValue(value, platforms)
            }
        }

        val username = if (name.isNotBlank()) {
            name.lowercase().replace("[^a-z0-9_]".toRegex(), "_").take(15).trim('_')
        } else "contact"

        return Contact(
            n = name.ifBlank { "Scanned Contact" },
            u = username.ifBlank { "contact" },
            e = email,
            phone = phone,
            platforms = platforms
        )
    }

    private fun extractPlatformFromUrlOrValue(value: String, platforms: MutableMap<String, String>) {
        val lower = value.lowercase()
        when {
            lower.contains("instagram.com/") -> {
                val handle = value.substringAfter("instagram.com/").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["ig"] = handle
            }
            lower.contains("twitter.com/") || lower.contains("x.com/") -> {
                val handle = (if (lower.contains("twitter.com/")) value.substringAfter("twitter.com/") else value.substringAfter("x.com/")).trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["tw"] = handle
            }
            lower.contains("linkedin.com/in/") -> {
                val handle = value.substringAfter("linkedin.com/in/").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["li"] = handle
            }
            lower.contains("github.com/") -> {
                val handle = value.substringAfter("github.com/").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["gh"] = handle
            }
            lower.contains("youtube.com/@") -> {
                val handle = value.substringAfter("youtube.com/@").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["yt"] = handle
            }
            lower.contains("discord.com/") || lower.contains("discord.gg/") -> {
                platforms["dc"] = value.substringAfterLast('/').trim('/')
            }
            lower.contains("t.me/") -> {
                val handle = value.substringAfter("t.me/").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["tg"] = handle
            }
            lower.contains("tiktok.com/@") -> {
                val handle = value.substringAfter("tiktok.com/@").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["tt"] = handle
            }
            lower.contains("snapchat.com/add/") -> {
                val handle = value.substringAfter("snapchat.com/add/").trim('/').substringBefore('?').substringBefore('/')
                if (handle.isNotBlank()) platforms["sc"] = handle
            }
            lower.startsWith("http://") || lower.startsWith("https://") -> {
                platforms["wb"] = value
            }
            else -> {
                platforms["cu"] = value
            }
        }
    }

    /**
     * Generates a raw ZXing [BitMatrix] from profile information.
     * Pure ZXing Java API, runnable on host JVM unit tests and Android runtimes.
     */
    fun encodeToBitMatrix(
        contact: Contact,
        widthPx: Int = 512,
        heightPx: Int = 512,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
        margin: Int = 3
    ): BitMatrix {
        val shareUrl = createProfileShareUrl(contact)
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to errorCorrectionLevel,
            EncodeHintType.MARGIN to margin
        )
        return MultiFormatWriter().encode(
            shareUrl,
            BarcodeFormat.QR_CODE,
            widthPx,
            heightPx,
            hints
        )
    }

    /**
     * Generates a raw QR Code [Bitmap] from profile information using ZXing.
     * Computes on [Dispatchers.Default] to guarantee smooth UI responsiveness.
     */
    suspend fun generateQrBitmap(
        contact: Contact,
        config: QrConfig = QrConfig()
    ): Bitmap = withContext(Dispatchers.Default) {
        val bitMatrix = encodeToBitMatrix(
            contact = contact,
            widthPx = config.widthPx,
            heightPx = config.heightPx,
            errorCorrectionLevel = config.errorCorrectionLevel,
            margin = config.margin
        )

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) {
                    config.foregroundColor
                } else {
                    config.backgroundColor
                }
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }

        // Overlay center avatar initials badge if enabled
        if (config.embedCenterBadge) {
            drawCenterBadge(bitmap, contact.initials, config.foregroundColor)
        } else {
            bitmap
        }
    }

    /**
     * Draws an avatar badge in the center of the QR code.
     * Because ZXing error correction is set to [ErrorCorrectionLevel.H] (30% redundancy),
     * a center badge up to ~22% of the dimension preserves 100% readability.
     */
    private fun drawCenterBadge(baseBitmap: Bitmap, initials: String, accentColor: Int): Bitmap {
        val canvas = Canvas(baseBitmap)
        val size = baseBitmap.width.toFloat()
        val badgeRadius = size * 0.11f // ~22% diameter
        val centerX = size / 2f
        val centerY = size / 2f

        // White border ring background to isolate badge from QR pixels
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, badgeRadius + 8f, ringPaint)

        // Gradient or accent circle
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, badgeRadius, circlePaint)

        // Initials Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = badgeRadius * 0.95f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val bounds = Rect()
        val textToDraw = initials.take(2).uppercase()
        textPaint.getTextBounds(textToDraw, 0, textToDraw.length, bounds)
        val textY = centerY + (bounds.height() / 2f) - bounds.bottom
        canvas.drawText(textToDraw, centerX, textY, textPaint)

        return baseBitmap
    }

    /**
     * Generates a complete, beautiful digital business card [Bitmap] containing:
     * - Top branding banner ("Plugme Connect")
     * - User Avatar & Name & Handle
     * - Central ZXing QR Code
     * - Platform icons strip
     * - Clear call-to-action footer ("Scan to view full profile & connect")
     */
    suspend fun generateShareableCardBitmap(
        contact: Contact,
        widthPx: Int = 720,
        heightPx: Int = 1040
    ): Bitmap = withContext(Dispatchers.Default) {
        val cardBitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(cardBitmap)

        // 1. Soft background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(245, 247, 251) // #F5F7FB
        }
        canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), bgPaint)

        // 2. White Card Container
        val cardMargin = 40f
        val cardRect = RectF(
            cardMargin,
            cardMargin,
            widthPx - cardMargin,
            heightPx - cardMargin
        )
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, cardBgPaint)

        // 3. Card Border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        // 4. Header: Brand bubble
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 170, 255)
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🫧 PLUGME CONNECT", widthPx / 2f, cardMargin + 65f, brandPaint)

        // 5. User Avatar Bubble
        val avatarCenterX = widthPx / 2f
        val avatarCenterY = cardMargin + 155f
        val avatarRadius = 55f

        val avatarCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 170, 255)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarCirclePaint)

        val avatarTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val avatarBounds = Rect()
        val initials = contact.initials
        avatarTextPaint.getTextBounds(initials, 0, initials.length, avatarBounds)
        canvas.drawText(initials, avatarCenterX, avatarCenterY + (avatarBounds.height() / 2f) - avatarBounds.bottom, avatarTextPaint)

        // 6. Name & Handle
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(26, 29, 41)
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val displayName = contact.n.ifBlank { contact.u.ifBlank { "Plugme Card" } }
        canvas.drawText(displayName, widthPx / 2f, avatarCenterY + avatarRadius + 44f, namePaint)

        val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 170, 255)
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val subtitle = when {
            contact.title.isNotBlank() && contact.company.isNotBlank() -> "${contact.title} • ${contact.company}"
            contact.title.isNotBlank() -> contact.title
            contact.company.isNotBlank() -> contact.company
            else -> "@${contact.u.ifBlank { "plug" }}"
        }
        canvas.drawText(subtitle, widthPx / 2f, avatarCenterY + avatarRadius + 80f, handlePaint)

        // 7. Render QR Code in center
        val qrSize = 440
        val qrBitmap = generateQrBitmap(
            contact,
            QrConfig(
                widthPx = qrSize,
                heightPx = qrSize,
                foregroundColor = Color.rgb(0, 170, 255),
                backgroundColor = Color.WHITE,
                embedCenterBadge = true
            )
        )
        val qrTop = avatarCenterY + avatarRadius + 115f
        val qrLeft = (widthPx - qrSize) / 2f
        canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)

        // 8. Platform preview indicators
        val platforms = contact.platforms.keys.take(5).joinToString("  ") { key ->
            PlatformCatalog.get(key).emoji
        }
        if (platforms.isNotBlank()) {
            val platformsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(platforms, widthPx / 2f, qrTop + qrSize + 50f, platformsPaint)
        }

        // 9. Footer instructions
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(107, 114, 128)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Scan with any camera or Plugme Connect ⚡",
            widthPx / 2f,
            heightPx - cardMargin - 40f,
            footerPaint
        )

        cardBitmap
    }

    /**
     * Saves a QR Code or Card [Bitmap] to the application's cache directory and
     * returns a shareable [Uri] via [FileProvider].
     */
    suspend fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String = "plugme_qr_card.png"
    ): Uri = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "qr_codes").apply {
            if (!exists()) mkdirs()
        }
        val file = File(cacheDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    /**
     * Builds an Android [Intent] for sharing the QR code image and profile link.
     */
    suspend fun createShareIntent(
        context: Context,
        contact: Contact,
        shareCardImage: Boolean = true
    ): Intent = withContext(Dispatchers.Default) {
        val shareUrl = createProfileShareUrl(contact)
        val name = contact.n.ifBlank { contact.u }
        val roleInfo = when {
            contact.title.isNotBlank() && contact.company.isNotBlank() -> " (${contact.title} at ${contact.company})"
            contact.title.isNotBlank() -> " (${contact.title})"
            contact.company.isNotBlank() -> " (${contact.company})"
            else -> ""
        }
        val shareText = "Here's my Plugme Connect card! Connect with $name$roleInfo:\n$shareUrl"

        if (shareCardImage) {
            try {
                val cardBitmap = generateShareableCardBitmap(contact)
                val imageUri = saveBitmapToCache(context, cardBitmap, "plugme_${contact.u.ifBlank { "card" }}.png")

                Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_SUBJECT, "Connect with $name on Plugme")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) {
                // Fallback to text if file creation fails
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Connect with $name on Plugme")
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
            }
        } else {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Connect with $name on Plugme")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
        }
    }
}
