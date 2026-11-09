package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Contact
import com.example.model.GameEntry
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room database entity for local offline caching of digital business cards.
 * Stores cards locally on device so users can browse, search, and view contacts without internet.
 */
@Entity(tableName = "saved_cards")
data class SavedCardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val title: String = "",
    val company: String = "",
    val phone: String = "",
    val age: Int?,
    val gender: String,
    val platformsJson: String,
    val gamingJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPersonalCard: Boolean = false
) {
    fun toContact(json: Json = defaultJson): Contact {
        val platforms = try {
            json.decodeFromString<Map<String, String>>(platformsJson)
        } catch (_: Exception) {
            emptyMap()
        }
        val gms = try {
            json.decodeFromString<List<GameEntry>>(gamingJson)
        } catch (_: Exception) {
            emptyList()
        }
        return Contact(
            n = name,
            u = username,
            e = email,
            title = title,
            company = company,
            phone = phone.ifBlank { platforms["ph"] ?: "" },
            a = age,
            g = gender,
            platforms = platforms,
            gms = gms
        )
    }

    companion object {
        private val defaultJson = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        fun fromContact(
            contact: Contact,
            isPersonalCard: Boolean = false,
            timestamp: Long = System.currentTimeMillis(),
            json: Json = defaultJson
        ): SavedCardEntity {
            val key = if (isPersonalCard) {
                "me"
            } else {
                contact.u.ifBlank { contact.n.ifBlank { "card_${timestamp}" } }
            }
            return SavedCardEntity(
                id = key,
                name = contact.n,
                username = contact.u,
                email = contact.e,
                title = contact.title,
                company = contact.company,
                phone = contact.effectivePhone,
                age = contact.a,
                gender = contact.g,
                platformsJson = json.encodeToString(contact.platforms),
                gamingJson = json.encodeToString(contact.gms),
                timestamp = timestamp,
                isPersonalCard = isPersonalCard
            )
        }
    }
}
