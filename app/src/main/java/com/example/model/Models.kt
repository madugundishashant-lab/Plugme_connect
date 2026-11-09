package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class GameEntry(
    val n: String = "", // game name
    val i: String = ""  // gamer tag / id
)

@Serializable
data class Contact(
    val n: String = "",                    // Name
    val u: String = "",                    // Username
    val e: String = "",                    // Email
    val title: String = "",                // Job Title
    val company: String = "",              // Company
    val phone: String = "",                // Phone Number
    val a: Int? = null,                    // Age (1..150)
    val g: String = "",                    // Gender
    val platforms: Map<String, String> = emptyMap(), // Platform key -> handle
    val gms: List<GameEntry> = emptyList() // Gaming entries
) {
    val effectivePhone: String
        get() = phone.ifBlank { platforms["ph"] ?: "" }

    val initials: String
        get() {
            val parts = n.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
                parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(1).uppercase()
                u.isNotBlank() -> u.take(2).uppercase()
                else -> "PL"
            }
        }

    fun buildSlim(): Contact {
        val cleanPhone = phone.trim().ifBlank { platforms["ph"]?.trim() ?: "" }
        val updatedPlatforms = platforms.filter { it.value.isNotBlank() }.toMutableMap()
        if (cleanPhone.isNotBlank() && !updatedPlatforms.containsKey("ph")) {
            updatedPlatforms["ph"] = cleanPhone
        }
        return Contact(
            n = n.trim(),
            u = u.trim(),
            e = e.trim(),
            title = title.trim(),
            company = company.trim(),
            phone = cleanPhone,
            a = a?.coerceIn(1, 150),
            g = g.trim(),
            platforms = updatedPlatforms,
            gms = gms.filter { it.n.isNotBlank() && it.i.isNotBlank() }
        )
    }
}

@Serializable
data class SocialLink(
    val k: String,
    val v: String
)

data class StatBubbleData(
    val label: String,
    val count: Int,
    val emoji: String
)
