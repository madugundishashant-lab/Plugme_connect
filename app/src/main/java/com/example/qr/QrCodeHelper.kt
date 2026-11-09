package com.example.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.example.model.Contact

/**
 * Convenience wrapper and memory cache for QR code operations in Plugme Connect.
 * Delegates core rendering and serialization to [ProfileQrGenerator].
 */
object QrCodeHelper {

    private var cachedProfileSignature: String? = null
    private var cachedBitmap: Bitmap? = null

    fun encodeProfileToUrl(contact: Contact): String {
        return ProfileQrGenerator.createProfileShareUrl(contact)
    }

    fun decodeFromUrlOrRaw(input: String): Contact? {
        return ProfileQrGenerator.decodeProfileFromPayload(input)
    }

    suspend fun generateQrBitmap(
        contact: Contact,
        widthPx: Int = 512,
        heightPx: Int = 512,
        foregroundColor: Int = android.graphics.Color.rgb(0, 170, 255),
        embedCenterBadge: Boolean = true
    ): Bitmap {
        val shareUrl = "${encodeProfileToUrl(contact)}_c${foregroundColor}_b${embedCenterBadge}"

        // Check in-memory cache
        val current = cachedBitmap
        if (cachedProfileSignature == shareUrl && current != null && !current.isRecycled) {
            return current
        }

        val config = ProfileQrGenerator.QrConfig(
            widthPx = widthPx,
            heightPx = heightPx,
            foregroundColor = foregroundColor,
            embedCenterBadge = embedCenterBadge
        )
        val bitmap = ProfileQrGenerator.generateQrBitmap(contact, config)
        cachedProfileSignature = shareUrl
        cachedBitmap = bitmap
        return bitmap
    }

    suspend fun createShareIntent(
        context: Context,
        contact: Contact,
        shareCardImage: Boolean = true
    ): Intent {
        return ProfileQrGenerator.createShareIntent(context, contact, shareCardImage)
    }

    fun invalidateCache() {
        cachedProfileSignature = null
        cachedBitmap = null
    }
}
