package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for locally cached business cards.
 * Provides reactive Flow queries and asynchronous persistence operations.
 */
@Dao
interface SavedCardDao {

    @Query("SELECT * FROM saved_cards WHERE isPersonalCard = 0 ORDER BY timestamp DESC")
    fun getAllSavedCards(): Flow<List<SavedCardEntity>>

    @Query("SELECT * FROM saved_cards WHERE isPersonalCard = 1 LIMIT 1")
    fun getPersonalCard(): Flow<SavedCardEntity?>

    @Query("SELECT * FROM saved_cards WHERE username = :username OR name = :name LIMIT 1")
    suspend fun getCardByUsernameOrName(username: String, name: String): SavedCardEntity?

    @Query("SELECT COUNT(*) FROM saved_cards WHERE isPersonalCard = 0")
    suspend fun getSavedCardsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: SavedCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<SavedCardEntity>)

    @Delete
    suspend fun deleteCard(card: SavedCardEntity)

    @Query("DELETE FROM saved_cards WHERE username = :username OR name = :name")
    suspend fun deleteByUsernameOrName(username: String, name: String)

    @Query("DELETE FROM saved_cards WHERE isPersonalCard = 0")
    suspend fun clearVault()

    @Query("DELETE FROM saved_cards")
    suspend fun clearAll()
}
