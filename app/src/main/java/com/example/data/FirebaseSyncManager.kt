package com.example.data

import android.util.Log
import com.example.PlugmeApp
import com.example.model.Contact
import com.example.model.GameEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed interface CloudRestoreResult {
    object Restored : CloudRestoreResult
    object NotFound : CloudRestoreResult
    object PermissionDenied : CloudRestoreResult
    data class Error(val message: String) : CloudRestoreResult
}

class FirebaseSyncManager(
    private val repository: PlugmeRepository,
    private val scope: CoroutineScope
) {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.w("FirebaseSync", "FirebaseAuth not available: ${e.message}")
        null
    }

    private val db: FirebaseDatabase? = try {
        FirebaseDatabase.getInstance(PlugmeApp.FIREBASE_DATABASE_URL)
    } catch (e: Exception) {
        try {
            FirebaseDatabase.getInstance()
        } catch (e2: Exception) {
            Log.w("FirebaseSync", "FirebaseDatabase not available: ${e2.message}")
            null
        }
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isPro = MutableStateFlow(auth?.currentUser != null)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private var wasSignedIn = auth?.currentUser != null

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                _currentUser.value = user
                val isNowSignedIn = (user != null)
                _isPro.value = isNowSignedIn

                // Only on false -> true transition: perform syncFromCloud
                if (!wasSignedIn && isNowSignedIn) {
                    wasSignedIn = true
                    val uid = user?.uid
                    if (uid != null) {
                        scope.launch {
                            handleInitialSignInSync(uid)
                        }
                    }
                } else if (!isNowSignedIn) {
                    wasSignedIn = false
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseSync", "Failed to attach auth state listener: ${e.message}")
        }
    }

    private suspend fun handleInitialSignInSync(uid: String) {
        // Small delay to allow RTDB websocket connection to attach auth credentials
        delay(300)
        when (val result = syncFromCloud(uid)) {
            is CloudRestoreResult.Restored -> {
                Log.i("FirebaseSync", "Restored existing cloud profile for $uid")
            }
            is CloudRestoreResult.NotFound -> {
                // Fresh user on cloud with allowed permissions -> push local data to cloud
                Log.i("FirebaseSync", "No cloud profile found for $uid, backing up local data...")
                syncToCloud(uid)
            }
            is CloudRestoreResult.PermissionDenied -> {
                Log.w("FirebaseSync", "RTDB read denied by security rules for user $uid. Preserving local data.")
                _syncStatus.value = "⚠️ Database access restricted by rules (check Firebase Console)"
            }
            is CloudRestoreResult.Error -> {
                Log.w("FirebaseSync", "Initial cloud sync failed: ${result.message}")
            }
        }
    }

    /**
     * Pushes local DataStore profile, vault, socials, gaming to Realtime Database.
     */
    suspend fun syncToCloud(explicitUid: String? = null): Boolean = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
        val uid = explicitUid ?: user?.uid ?: return@withContext false
        val database = db ?: return@withContext false

        try {
            // Ensure ID token is available
            try {
                user?.getIdToken(false)?.await()
            } catch (tokenEx: Exception) {
                Log.w("FirebaseSync", "Token fetch note: ${tokenEx.message}")
            }

            val profile = repository.profileFlow.first()
            val vault = repository.vaultFlow.first()

            val payload = mapOf(
                "profile" to mapOf(
                    "n" to profile.n,
                    "u" to profile.u,
                    "e" to profile.e,
                    "title" to profile.title,
                    "company" to profile.company,
                    "phone" to profile.effectivePhone,
                    "a" to (profile.a ?: 0),
                    "g" to profile.g
                ),
                "vault" to vault.map { contact ->
                    mapOf(
                        "n" to contact.n,
                        "u" to contact.u,
                        "e" to contact.e,
                        "title" to contact.title,
                        "company" to contact.company,
                        "phone" to contact.effectivePhone,
                        "a" to (contact.a ?: 0),
                        "g" to contact.g,
                        "platforms" to contact.platforms,
                        "gms" to contact.gms.map { mapOf("n" to it.n, "i" to it.i) }
                    )
                },
                "socials" to profile.platforms,
                "gaming" to profile.gms.map { mapOf("n" to it.n, "i" to it.i) },
                "updatedAt" to ServerValue.TIMESTAMP
            )

            database.reference.child("users").child(uid).setValue(payload).await()
            _syncStatus.value = "☁️ Synced to cloud"
            true
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("permission", ignoreCase = true) || msg.contains("denied", ignoreCase = true)) {
                Log.w("FirebaseSync", "syncToCloud: Realtime Database permission denied for $uid.")
                _syncStatus.value = "⚠️ Database write restricted by rules (check Firebase Console)"
            } else {
                Log.w("FirebaseSync", "syncToCloud error: $msg")
                _syncStatus.value = "⚠️ Sync failed: ${msg.take(40)}"
            }
            false
        }
    }

    /**
     * If snapshot exists -> CLOUD AUTHORITATIVE, overwrite DataStore.
     * If !exists -> return CloudRestoreResult.NotFound.
     */
    suspend fun syncFromCloud(uid: String): CloudRestoreResult = withContext(Dispatchers.IO) {
        val database = db ?: return@withContext CloudRestoreResult.Error("Database unavailable")

        try {
            // Ensure ID token is available
            try {
                auth?.currentUser?.getIdToken(false)?.await()
            } catch (tokenEx: Exception) {
                Log.w("FirebaseSync", "Token fetch note: ${tokenEx.message}")
            }

            val snapshot = database.reference.child("users").child(uid).get().await()
            if (!snapshot.exists()) {
                return@withContext CloudRestoreResult.NotFound
            }

            val profileMap = snapshot.child("profile").value as? Map<*, *>
            val socialsMap = (snapshot.child("socials").value as? Map<*, *>) ?: emptyMap<Any, Any>()
            val gamingList = (snapshot.child("gaming").value as? List<*>) ?: emptyList<Any>()

            val parsedPlatforms = socialsMap.mapNotNull { (k, v) ->
                val key = k?.toString()
                val value = v?.toString()
                if (key != null && value != null) key to value else null
            }.toMap()

            val parsedGaming = gamingList.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                val n = map["n"]?.toString() ?: ""
                val i = map["i"]?.toString() ?: ""
                if (n.isNotBlank()) GameEntry(n, i) else null
            }

            val parsedProfile = Contact(
                n = profileMap?.get("n")?.toString() ?: "",
                u = profileMap?.get("u")?.toString() ?: "",
                e = profileMap?.get("e")?.toString() ?: "",
                title = profileMap?.get("title")?.toString() ?: "",
                company = profileMap?.get("company")?.toString() ?: "",
                phone = profileMap?.get("phone")?.toString() ?: "",
                a = (profileMap?.get("a") as? Number)?.toInt()?.takeIf { it in 1..150 },
                g = profileMap?.get("g")?.toString() ?: "",
                platforms = parsedPlatforms,
                gms = parsedGaming
            )

            val vaultRaw = snapshot.child("vault").value
            val parsedVault = mutableListOf<Contact>()

            if (vaultRaw is List<*>) {
                for (item in vaultRaw) {
                    val map = item as? Map<*, *> ?: continue
                    val cPlatforms = (map["platforms"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                        val kStr = k?.toString()
                        val vStr = v?.toString()
                        if (kStr != null && vStr != null) kStr to vStr else null
                    }?.toMap() ?: emptyMap()

                    val cGaming = (map["gms"] as? List<*>)?.mapNotNull { gItem ->
                        val gMap = gItem as? Map<*, *> ?: return@mapNotNull null
                        val gn = gMap["n"]?.toString() ?: ""
                        val gi = gMap["i"]?.toString() ?: ""
                        if (gn.isNotBlank()) GameEntry(gn, gi) else null
                    } ?: emptyList()

                    parsedVault.add(
                        Contact(
                            n = map["n"]?.toString() ?: "",
                            u = map["u"]?.toString() ?: "",
                            e = map["e"]?.toString() ?: "",
                            title = map["title"]?.toString() ?: "",
                            company = map["company"]?.toString() ?: "",
                            phone = map["phone"]?.toString() ?: "",
                            a = (map["a"] as? Number)?.toInt()?.takeIf { it in 1..150 },
                            g = map["g"]?.toString() ?: "",
                            platforms = cPlatforms,
                            gms = cGaming
                        )
                    )
                }
            }

            repository.overwriteAllFromCloud(parsedProfile, parsedVault)
            _syncStatus.value = "☁️ Cloud data restored"
            CloudRestoreResult.Restored
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("permission", ignoreCase = true) || msg.contains("denied", ignoreCase = true)) {
                Log.w("FirebaseSync", "syncFromCloud: Realtime Database permission denied for $uid.")
                _syncStatus.value = "⚠️ Database access restricted by rules (check Firebase Console)"
                CloudRestoreResult.PermissionDenied
            } else {
                Log.w("FirebaseSync", "syncFromCloud error: $msg")
                CloudRestoreResult.Error(msg)
            }
        }
    }

    suspend fun signIn(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(Exception("Firebase Auth unavailable"))
        try {
            val result = a.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User is null")
            _currentUser.value = user
            _isPro.value = true
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthExceptionMessage(e)))
        }
    }

    suspend fun signUp(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val a = auth ?: return@withContext Result.failure(Exception("Firebase Auth unavailable"))
        try {
            val result = a.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = result.user ?: throw Exception("User is null")
            _currentUser.value = user
            _isPro.value = true
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(Exception(mapAuthExceptionMessage(e)))
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            _currentUser.value = null
            _isPro.value = false
            _syncStatus.value = "Logged out (Free mode)"
        } catch (e: Exception) {
            Log.e("FirebaseSync", "Sign out error", e)
        }
    }

    fun mapAuthExceptionMessage(e: Exception): String {
        val msg = e.message.orEmpty()
        return when {
            e is FirebaseAuthUserCollisionException ||
            msg.contains("already in use", ignoreCase = true) ||
            msg.contains("already-in-use", ignoreCase = true) ||
            msg.contains("already registered", ignoreCase = true) ||
            msg.contains("email_already_in_use", ignoreCase = true) -> {
                "This email address is already in use by another account. Please sign in instead."
            }
            e is FirebaseAuthWeakPasswordException ||
            msg.contains("weak-password", ignoreCase = true) ||
            msg.contains("at least 6 characters", ignoreCase = true) -> {
                "Password must be at least 6 characters."
            }
            e is FirebaseAuthInvalidUserException ||
            msg.contains("user-not-found", ignoreCase = true) ||
            msg.contains("no user record", ignoreCase = true) -> {
                "No account found with this email. Please check your email or sign up."
            }
            e is FirebaseAuthInvalidCredentialsException -> {
                if (msg.contains("password", ignoreCase = true)) {
                    "Incorrect password. Please try again."
                } else if (msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true)) {
                    "Invalid email address format."
                } else {
                    "Invalid email or password credentials."
                }
            }
            e is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Invalid email address format."
                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "This email address is already in use. Please sign in instead."
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again in a few minutes."
                else -> e.localizedMessage ?: "Authentication error."
            }
            msg.contains("invalid-email", ignoreCase = true) || msg.contains("badly formatted", ignoreCase = true) -> {
                "Invalid email address format."
            }
            msg.contains("wrong-password", ignoreCase = true) -> {
                "Incorrect password. Please try again."
            }
            msg.contains("too-many-requests", ignoreCase = true) -> {
                "Too many attempts. Please wait a moment before trying again."
            }
            msg.contains("network-request-failed", ignoreCase = true) -> {
                "Network connection error. Please verify your internet connection."
            }
            else -> e.localizedMessage ?: "Operation failed. Please check your connection and try again."
        }
    }
}
