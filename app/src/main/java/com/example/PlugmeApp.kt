package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class PlugmeApp : Application() {

    companion object {
        const val FIREBASE_API_KEY = "AIzaSyB_N3ynM_y82A4Gmz2LfUt4BFf6xD3IM3g"
        const val FIREBASE_AUTH_DOMAIN = "plugme-ca96c.firebaseapp.com"
        const val FIREBASE_DATABASE_URL = "https://plugme-ca96c-default-rtdb.asia-southeast1.firebasedatabase.app"
        const val FIREBASE_PROJECT_ID = "plugme-ca96c"
        const val FIREBASE_STORAGE_BUCKET = "plugme-ca96c.firebasestorage.app"
        const val FIREBASE_MESSAGING_SENDER_ID = "23197221114"
        const val FIREBASE_APP_ID = "1:23197221114:web:b8d64bbd49307d92ff0943"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(FIREBASE_API_KEY)
                    .setApplicationId(FIREBASE_APP_ID)
                    .setDatabaseUrl(FIREBASE_DATABASE_URL)
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                    .setGcmSenderId(FIREBASE_MESSAGING_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("PlugmeApp", "Firebase successfully initialized with project plugme-ca96c.")
            } else {
                Log.d("PlugmeApp", "Firebase initialized via configuration.")
            }
        } catch (e: Exception) {
            Log.w("PlugmeApp", "Firebase initialization exception: ${e.message}")
        }
    }
}
