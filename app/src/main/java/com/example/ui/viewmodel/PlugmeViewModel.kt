package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirebaseSyncManager
import com.example.data.PlugmeRepository
import com.example.model.Contact
import com.example.model.GameEntry
import com.example.qr.QrCodeHelper
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class VaultFilter {
    ALL,
    RECENT,
    GAMING,
    SOCIAL
}

class PlugmeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlugmeRepository(application)
    val syncManager = FirebaseSyncManager(repository, viewModelScope)

    val profile: StateFlow<Contact> = repository.profileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Contact()
    )

    val vault: StateFlow<List<Contact>> = repository.vaultFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val welcomed: StateFlow<Boolean> = repository.welcomedFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true // Start true to prevent flash, then collected
    )

    val isPro: StateFlow<Boolean> = syncManager.isPro

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter selection state
    private val _selectedFilter = MutableStateFlow(VaultFilter.ALL)
    val selectedFilter: StateFlow<VaultFilter> = _selectedFilter.asStateFlow()

    // Debounced search query flow (180ms)
    @OptIn(FlowPreview::class)
    private val debouncedSearch = _searchQuery
        .debounce(180)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Filtered contacts combining vault, debounced search, and selected filter
    val filteredContacts: StateFlow<List<Contact>> = combine(
        vault,
        debouncedSearch,
        selectedFilter
    ) { list, query, filter ->
        val queryClean = query.trim().lowercase()
        val searched = if (queryClean.isEmpty()) {
            list
        } else {
            list.filter { contact ->
                contact.n.lowercase().contains(queryClean) ||
                contact.u.lowercase().contains(queryClean) ||
                contact.platforms.values.any { it.lowercase().contains(queryClean) } ||
                contact.gms.any { it.n.lowercase().contains(queryClean) || it.i.lowercase().contains(queryClean) }
            }
        }

        when (filter) {
            VaultFilter.ALL -> searched
            VaultFilter.RECENT -> searched.take(6)
            VaultFilter.GAMING -> searched.filter { it.gms.isNotEmpty() }
            VaultFilter.SOCIAL -> searched.filter { it.platforms.isNotEmpty() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // QR Bitmap state & customization
    private val _qrBitmap = MutableStateFlow<Bitmap?>(null)
    val qrBitmap: StateFlow<Bitmap?> = _qrBitmap.asStateFlow()

    private val _selectedQrColor = MutableStateFlow(android.graphics.Color.rgb(0, 170, 255))
    val selectedQrColor: StateFlow<Int> = _selectedQrColor.asStateFlow()

    private val _embedCenterBadge = MutableStateFlow(true)
    val embedCenterBadge: StateFlow<Boolean> = _embedCenterBadge.asStateFlow()

    private val _isGeneratingQr = MutableStateFlow(false)
    val isGeneratingQr: StateFlow<Boolean> = _isGeneratingQr.asStateFlow()

    // Snackbar events
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Selected contact for detail
    private val _selectedContact = MutableStateFlow<Contact?>(null)
    val selectedContact: StateFlow<Contact?> = _selectedContact.asStateFlow()

    // Processed deep links to prevent re-processing on configuration change / rotation
    private val processedDeepLinks = mutableSetOf<String>()

    private var qrJob: Job? = null

    init {
        // Automatically regenerate custom QR bitmap when profile, color, or badge toggles
        viewModelScope.launch {
            combine(profile, _selectedQrColor, _embedCenterBadge) { p, color, badge ->
                Triple(p, color, badge)
            }.collect { (p, color, badge) ->
                if (p.n.isNotBlank() || p.u.isNotBlank()) {
                    regenerateQr(p, color, badge)
                }
            }
        }
    }

    fun setQrColor(colorInt: Int) {
        _selectedQrColor.value = colorInt
    }

    fun toggleCenterBadge() {
        _embedCenterBadge.value = !_embedCenterBadge.value
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelect(filter: VaultFilter) {
        _selectedFilter.value = filter
    }

    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    fun setWelcomed() {
        viewModelScope.launch {
            repository.setWelcomed(true)
        }
    }

    fun showWelcomeAgain() {
        viewModelScope.launch {
            repository.setWelcomed(false)
        }
    }

    fun refreshQr() {
        QrCodeHelper.invalidateCache()
        regenerateQr(profile.value, _selectedQrColor.value, _embedCenterBadge.value)
    }

    private fun regenerateQr(
        contact: Contact,
        color: Int = _selectedQrColor.value,
        badge: Boolean = _embedCenterBadge.value
    ) {
        qrJob?.cancel()
        qrJob = viewModelScope.launch {
            _isGeneratingQr.value = true
            try {
                val bitmap = QrCodeHelper.generateQrBitmap(
                    contact = contact,
                    foregroundColor = color,
                    embedCenterBadge = badge
                )
                _qrBitmap.value = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingQr.value = false
            }
        }
    }

    fun saveProfile(
        name: String,
        username: String,
        email: String,
        title: String = "",
        company: String = "",
        phone: String = "",
        age: Int? = null,
        gender: String = ""
    ) {
        viewModelScope.launch {
            val current = profile.value
            val updated = current.copy(
                n = name.trim(),
                u = username.trim(),
                e = email.trim(),
                title = title.trim(),
                company = company.trim(),
                phone = phone.trim(),
                a = age?.coerceIn(1, 150),
                g = gender.trim()
            )
            repository.saveProfile(updated)
            if (isPro.value) {
                syncManager.syncToCloud()
            }
            _snackbarMessage.emit("✅ Profile saved to local database & QR updated!")
        }
    }

    fun saveSocialsAndGames(platforms: Map<String, String>, games: List<GameEntry>) {
        viewModelScope.launch {
            val maxSocials = if (isPro.value) 25 else 6
            val maxGames = if (isPro.value) 15 else 5

            val trimmedPlatforms = platforms.filter { it.value.isNotBlank() }.entries.take(maxSocials).associate { it.key to it.value.trim() }
            val trimmedGames = games.filter { it.n.isNotBlank() && it.i.isNotBlank() }.take(maxGames)

            repository.updateSocialsAndGaming(trimmedPlatforms, trimmedGames)
            if (isPro.value) {
                syncManager.syncToCloud()
            }
            _snackbarMessage.emit("✨ Links & Games updated!")
        }
    }

    fun saveEditedContact(originalKey: String, contact: Contact, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (originalKey.isBlank()) {
                val added = repository.addContact(contact)
                if (added) {
                    _snackbarMessage.emit("🎉 Added ${contact.n.ifBlank { contact.u }}")
                } else {
                    _snackbarMessage.emit("🔗 Already connected, updated details")
                }
            } else {
                repository.updateContact(originalKey, contact)
                _snackbarMessage.emit("✅ Contact updated")
            }
            if (isPro.value) {
                syncManager.syncToCloud()
            }
            onComplete()
        }
    }

    fun removeContact(contact: Contact, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.removeContact(contact)
            if (isPro.value) {
                syncManager.syncToCloud()
            }
            _snackbarMessage.emit("🗑️ Removed ${contact.n.ifBlank { contact.u }}")
            onComplete()
        }
    }

    /**
     * Handles deep link data: "https://plugmeconnect.app/?d=..."
     * Deduplicates and prevents re-triggering across rotations.
     */
    fun handleDeepLink(urlOrPayload: String) {
        if (urlOrPayload.isBlank() || processedDeepLinks.contains(urlOrPayload)) return
        processedDeepLinks.add(urlOrPayload)

        viewModelScope.launch {
            val contact = QrCodeHelper.decodeFromUrlOrRaw(urlOrPayload)
            if (contact != null && (contact.n.isNotBlank() || contact.u.isNotBlank())) {
                val isNew = repository.addContact(contact)
                if (isPro.value) {
                    syncManager.syncToCloud()
                }
                if (isNew) {
                    _snackbarMessage.emit("🎉 Added ${contact.n.ifBlank { contact.u }}!")
                } else {
                    _snackbarMessage.emit("🔗 Already connected with ${contact.n.ifBlank { contact.u }}")
                }
            } else {
                _snackbarMessage.emit("❌ Could not read contact data")
            }
        }
    }

    /**
     * Handles scanned QR code raw string
     */
    fun handleScannedQr(rawValue: String, onNavigateToVault: () -> Unit) {
        viewModelScope.launch {
            val contact = QrCodeHelper.decodeFromUrlOrRaw(rawValue)
            if (contact != null && (contact.n.isNotBlank() || contact.u.isNotBlank())) {
                val isNew = repository.addContact(contact)
                if (isPro.value) {
                    syncManager.syncToCloud()
                }
                if (isNew) {
                    _snackbarMessage.emit("🎉 Added ${contact.n.ifBlank { contact.u }}!")
                } else {
                    _snackbarMessage.emit("🔗 Already connected with ${contact.n.ifBlank { contact.u }}")
                }
                onNavigateToVault()
            } else {
                _snackbarMessage.emit("❌ Could not read QR code")
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            if (!isPro.value) {
                _snackbarMessage.emit("⚡ Sync is available for Pro accounts")
                return@launch
            }
            val success = syncManager.syncToCloud()
            if (success) {
                _snackbarMessage.emit("☁️ Synced to cloud successfully!")
            } else {
                val currentStatus = syncManager.syncStatus.value ?: "Cloud sync failed. Check connection."
                _snackbarMessage.emit(currentStatus)
            }
        }
    }

    fun wipeAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.wipeAllData()
            _snackbarMessage.emit("🧹 All data wiped. Defaults restored.")
            onComplete()
        }
    }

    fun emitMessage(msg: String) {
        viewModelScope.launch {
            _snackbarMessage.emit(msg)
        }
    }
}
