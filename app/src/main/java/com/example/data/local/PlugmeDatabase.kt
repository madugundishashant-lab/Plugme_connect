package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for offline caching of business cards.
 * Enables zero-latency local queries and offline access even with no internet connection.
 */
@Database(entities = [SavedCardEntity::class], version = 2, exportSchema = false)
abstract class PlugmeDatabase : RoomDatabase() {

    abstract fun savedCardDao(): SavedCardDao

    companion object {
        @Volatile
        private var INSTANCE: PlugmeDatabase? = null

        fun getDatabase(context: Context): PlugmeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlugmeDatabase::class.java,
                    "plugme_offline_cache.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
