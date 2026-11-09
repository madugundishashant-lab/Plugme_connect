package com.example.model

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class PlatformInfo(
    val key: String,
    val name: String,
    val emoji: String,
    val baseUrl: String,
    val isPro: Boolean = false,
    val placeholder: String = "username"
)

object PlatformCatalog {
    // 6 Free platforms
    val FREE_PLATFORMS = listOf(
        PlatformInfo("ig", "Instagram", "📸", "https://instagram.com/", placeholder = "username"),
        PlatformInfo("sc", "Snapchat", "👻", "https://snapchat.com/add/", placeholder = "username"),
        PlatformInfo("tw", "X (Twitter)", "𝕏", "https://x.com/", placeholder = "handle"),
        PlatformInfo("tg", "Telegram", "✈️", "https://t.me/", placeholder = "username"),
        PlatformInfo("fb", "Facebook", "📘", "https://facebook.com/", placeholder = "profile.id"),
        PlatformInfo("li", "LinkedIn", "💼", "https://linkedin.com/in/", placeholder = "profile-slug")
    )

    // 19 Pro platforms
    val PRO_PLATFORMS = listOf(
        PlatformInfo("yt", "YouTube", "▶️", "https://youtube.com/@", isPro = true, placeholder = "channel_handle"),
        PlatformInfo("dc", "Discord", "👾", "https://discord.com/users/", isPro = true, placeholder = "user_id or name"),
        PlatformInfo("tt", "TikTok", "🎵", "https://tiktok.com/@", isPro = true, placeholder = "handle"),
        PlatformInfo("gh", "GitHub", "🐙", "https://github.com/", isPro = true, placeholder = "username"),
        PlatformInfo("sp", "Spotify", "🎧", "https://open.spotify.com/user/", isPro = true, placeholder = "user_id"),
        PlatformInfo("tch", "Twitch", "🎮", "https://twitch.tv/", isPro = true, placeholder = "channel"),
        PlatformInfo("rd", "Reddit", "🤖", "https://reddit.com/user/", isPro = true, placeholder = "username"),
        PlatformInfo("pt", "Pinterest", "📌", "https://pinterest.com/", isPro = true, placeholder = "username"),
        PlatformInfo("th", "Threads", "🧵", "https://threads.net/@", isPro = true, placeholder = "handle"),
        PlatformInfo("wa", "WhatsApp", "💬", "https://wa.me/", isPro = true, placeholder = "phone with country code"),
        PlatformInfo("sg", "Signal", "🛡️", "https://signal.me/#p/", isPro = true, placeholder = "username or number"),
        PlatformInfo("bh", "Behance", "🎨", "https://behance.net/", isPro = true, placeholder = "username"),
        PlatformInfo("db", "Dribbble", "🏀", "https://dribbble.com/", isPro = true, placeholder = "username"),
        PlatformInfo("md", "Medium", "✍️", "https://medium.com/@", isPro = true, placeholder = "author"),
        PlatformInfo("pa", "Patreon", "🎁", "https://patreon.com/", isPro = true, placeholder = "creator"),
        PlatformInfo("wb", "Website", "🌐", "", isPro = true, placeholder = "https://example.com"),
        PlatformInfo("em", "Email", "✉️", "", isPro = true, placeholder = "hello@example.com"),
        PlatformInfo("ph", "Phone", "📞", "", isPro = true, placeholder = "+1234567890"),
        PlatformInfo("cu", "Custom Link", "🔗", "", isPro = true, placeholder = "https://link.com/profile")
    )

    val ALL_PLATFORMS = FREE_PLATFORMS + PRO_PLATFORMS

    val MAP: Map<String, PlatformInfo> = ALL_PLATFORMS.associateBy { it.key }

    fun get(key: String): PlatformInfo {
        return MAP[key] ?: PlatformInfo(key, key.uppercase(), "🔗", "", isPro = false)
    }

    /**
     * UrlBuilder:
     * - strip "@", URL-encode handle, prepend base
     * - wb/cu raw (add https:// if missing)
     * - em -> mailto:
     * - ph -> tel:
     */
    fun buildUrl(key: String, rawValue: String): String {
        val trimmed = rawValue.trim()
        if (trimmed.isEmpty()) return ""

        return when (key.lowercase()) {
            "em" -> if (trimmed.startsWith("mailto:", ignoreCase = true)) trimmed else "mailto:$trimmed"
            "ph" -> {
                val cleanPhone = trimmed.replace("[^0-9+]".toRegex(), "")
                if (cleanPhone.startsWith("tel:", ignoreCase = true)) cleanPhone else "tel:$cleanPhone"
            }
            "wb", "cu" -> {
                if (trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true)
                ) {
                    trimmed
                } else {
                    "https://$trimmed"
                }
            }
            "wa" -> {
                val clean = trimmed.removePrefix("@").replace("[^0-9+]".toRegex(), "").removePrefix("+")
                "https://wa.me/$clean"
            }
            else -> {
                val info = get(key)
                val handleWithoutAt = trimmed.removePrefix("@")
                val encoded = try {
                    URLEncoder.encode(handleWithoutAt, StandardCharsets.UTF_8.toString())
                } catch (_: Exception) {
                    handleWithoutAt
                }
                if (info.baseUrl.isNotEmpty()) {
                    "${info.baseUrl}$encoded"
                } else {
                    if (trimmed.startsWith("http://", ignoreCase = true) ||
                        trimmed.startsWith("https://", ignoreCase = true)
                    ) {
                        trimmed
                    } else {
                        "https://$trimmed"
                    }
                }
            }
        }
    }
}
