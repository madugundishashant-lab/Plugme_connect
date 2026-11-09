package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.PlugmeDatabase
import com.example.data.local.SavedCardEntity
import com.example.model.Contact
import com.example.model.GameEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

val Context.dataStore by preferencesDataStore(name = "plugme_datastore")

/**
 * Repository adhering to the Android Repository Pattern.
 * Coordinates local caching via Room SQLite Database for 100% offline access,
 * with DataStore preferences for auxiliary flags and lightweight settings.
 */
class PlugmeRepository(
    private val context: Context,
    private val database: PlugmeDatabase = PlugmeDatabase.getDatabase(context)
) {
    private val savedCardDao = database.savedCardDao()
    private val dataStore = context.dataStore

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    companion object {
        val KEY_ME = stringPreferencesKey("plugme_me")
        val KEY_VAULT = stringPreferencesKey("plugme_vault")
        val KEY_SOCIALS = stringPreferencesKey("plugme_socials")
        val KEY_GAMING = stringPreferencesKey("plugme_gaming")
        val KEY_WELCOMED = booleanPreferencesKey("plugme_welcomed")
        val KEY_CLEAN_INITIALIZED = booleanPreferencesKey("plugme_clean_initialized_v2")
    }

    init {
        // Asynchronously ensure all pre-existing demo data is cleared on launch
        CoroutineScope(Dispatchers.IO).launch {
            checkAndSeedDefaults()
        }
    }

    private suspend fun checkAndSeedDefaults() = withContext(Dispatchers.IO) {
        try {
            val prefs = dataStore.data.first()
            val isClean = prefs[KEY_CLEAN_INITIALIZED] ?: false

            if (!isClean) {
                // Clear any pre-existing demo data from previous sessions or template seeds
                savedCardDao.clearAll()
                dataStore.edit { preferences ->
                    preferences.clear()
                    preferences[KEY_CLEAN_INITIALIZED] = true
                    preferences[KEY_WELCOMED] = false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val welcomedFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            prefs[KEY_WELCOMED] ?: false
        }

    suspend fun setWelcomed(welcomed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_WELCOMED] = welcomed
        }
    }

    /**
     * Reactive Flow of personal profile card, read from Room SQLite cache.
     * Returns a blank Contact() if the user has not created their profile yet.
     */
    val profileFlow: Flow<Contact> = savedCardDao.getPersonalCard()
        .map { entity ->
            entity?.toContact(json) ?: Contact()
        }

    /**
     * Reactive stream of saved business cards backed directly by the Room Database.
     * Starts with a clean empty list until real contacts are scanned or added.
     */
    val vaultFlow: Flow<List<Contact>> = savedCardDao.getAllSavedCards()
        .map { entities ->
            entities.map { it.toContact(json) }
        }

    suspend fun saveProfile(profile: Contact) = withContext(Dispatchers.IO) {
        val slim = profile.buildSlim()
        savedCardDao.insertCard(
            SavedCardEntity.fromContact(slim, isPersonalCard = true, json = json)
        )
        dataStore.edit { prefs ->
            prefs[KEY_ME] = json.encodeToString(slim)
            prefs[KEY_SOCIALS] = json.encodeToString(slim.platforms)
            prefs[KEY_GAMING] = json.encodeToString(slim.gms)
        }
    }

    suspend fun updateSocialsAndGaming(platforms: Map<String, String>, gms: List<GameEntry>) {
        val current = profileFlow.first()
        val updated = current.copy(
            platforms = platforms.filter { it.value.isNotBlank() },
            gms = gms.filter { it.n.isNotBlank() && it.i.isNotBlank() }
        ).buildSlim()
        saveProfile(updated)
    }

    /**
     * Add contact with deduplication by username or name.
     * Caches immediately into Room SQLite database.
     * Returns true if newly added, false if merged with existing.
     */
    suspend fun addContact(newContact: Contact): Boolean = withContext(Dispatchers.IO) {
        val currentVault = vaultFlow.first().toMutableList()
        val slim = newContact.buildSlim()

        val existingIndex = currentVault.indexOfFirst {
            (slim.u.isNotBlank() && it.u.equals(slim.u, ignoreCase = true)) ||
            (slim.n.isNotBlank() && it.n.equals(slim.n, ignoreCase = true))
        }

        return@withContext if (existingIndex >= 0) {
            val existing = currentVault[existingIndex]
            val mergedPlatforms = existing.platforms.toMutableMap().apply {
                putAll(slim.platforms)
            }
            val mergedGms = (existing.gms + slim.gms).distinctBy { "${it.n.lowercase()}_${it.i.lowercase()}" }
            val merged = existing.copy(
                n = if (slim.n.isNotBlank()) slim.n else existing.n,
                u = if (slim.u.isNotBlank()) slim.u else existing.u,
                e = if (slim.e.isNotBlank()) slim.e else existing.e,
                title = if (slim.title.isNotBlank()) slim.title else existing.title,
                company = if (slim.company.isNotBlank()) slim.company else existing.company,
                phone = if (slim.phone.isNotBlank()) slim.phone else existing.phone,
                a = slim.a ?: existing.a,
                g = if (slim.g.isNotBlank()) slim.g else existing.g,
                platforms = mergedPlatforms,
                gms = mergedGms
            )
            currentVault[existingIndex] = merged
            savedCardDao.insertCard(
                SavedCardEntity.fromContact(merged, isPersonalCard = false, json = json)
            )
            dataStore.edit { prefs ->
                prefs[KEY_VAULT] = json.encodeToString(currentVault)
            }
            false
        } else {
            currentVault.add(0, slim)
            savedCardDao.insertCard(
                SavedCardEntity.fromContact(slim, isPersonalCard = false, json = json)
            )
            dataStore.edit { prefs ->
                prefs[KEY_VAULT] = json.encodeToString(currentVault)
            }
            true
        }
    }

    suspend fun updateContact(originalUsernameOrName: String, updatedContact: Contact) = withContext(Dispatchers.IO) {
        val currentVault = vaultFlow.first().toMutableList()
        val index = currentVault.indexOfFirst {
            it.u.equals(originalUsernameOrName, ignoreCase = true) ||
            it.n.equals(originalUsernameOrName, ignoreCase = true)
        }
        if (index >= 0) {
            val slim = updatedContact.buildSlim()
            currentVault[index] = slim
            if (originalUsernameOrName.isNotBlank() && !originalUsernameOrName.equals(slim.u, ignoreCase = true)) {
                savedCardDao.deleteByUsernameOrName(originalUsernameOrName, originalUsernameOrName)
            }
            savedCardDao.insertCard(
                SavedCardEntity.fromContact(slim, isPersonalCard = false, json = json)
            )
            dataStore.edit { prefs ->
                prefs[KEY_VAULT] = json.encodeToString(currentVault)
            }
        }
    }

    suspend fun removeContact(target: Contact) = withContext(Dispatchers.IO) {
        val currentVault = vaultFlow.first().toMutableList()
        currentVault.removeAll {
            (target.u.isNotBlank() && it.u.equals(target.u, ignoreCase = true)) ||
            (target.n.isNotBlank() && it.n.equals(target.n, ignoreCase = true))
        }
        savedCardDao.deleteByUsernameOrName(target.u, target.n)
        dataStore.edit { prefs ->
            prefs[KEY_VAULT] = json.encodeToString(currentVault)
        }
    }

    suspend fun overwriteAllFromCloud(profile: Contact, vault: List<Contact>) = withContext(Dispatchers.IO) {
        savedCardDao.clearVault()
        val cardEntities = vault.map {
            SavedCardEntity.fromContact(it.buildSlim(), isPersonalCard = false, json = json)
        }
        savedCardDao.insertCards(cardEntities)

        val profileEntity = SavedCardEntity.fromContact(profile.buildSlim(), isPersonalCard = true, json = json)
        savedCardDao.insertCard(profileEntity)

        dataStore.edit { prefs ->
            prefs[KEY_ME] = json.encodeToString(profile.buildSlim())
            prefs[KEY_SOCIALS] = json.encodeToString(profile.platforms)
            prefs[KEY_GAMING] = json.encodeToString(profile.gms)
            prefs[KEY_VAULT] = json.encodeToString(vault.map { it.buildSlim() })
        }
    }

    suspend fun wipeAllData() = withContext(Dispatchers.IO) {
        savedCardDao.clearAll()
        dataStore.edit { prefs ->
            prefs.clear()
            prefs[KEY_CLEAN_INITIALIZED] = true
        }
    }
}
