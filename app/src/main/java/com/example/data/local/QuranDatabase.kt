package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // e.g. "SURAH_1", "RECITER_5", "RADIO_10"
    val type: String, // SURAH, RECITER, TADABOR, VIDEO, RADIO
    val targetId: Int,
    val title: String,
    val subtitle: String,
    val extraData: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val surahId: Int,
    val surahName: String,
    val reciterId: Int,
    val reciterName: String,
    val moshafName: String,
    val audioUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val progressRatio: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadedSurahEntity(
    @PrimaryKey val id: String, // "${reciterId}_${surahId}"
    val surahId: Int,
    val surahName: String,
    val reciterId: Int,
    val reciterName: String,
    val localFilePath: String,
    val fileSize: Long,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "api_cache")
data class CachedApiResponse(
    @PrimaryKey val endpointKey: String,
    val jsonPayload: String,
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface QuranDao {
    // Favorites
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY timestamp DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    suspend fun isFavoriteSync(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    // History
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    fun getLastListened(): Flow<HistoryEntity?>

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastListenedSync(): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE surahId = :surahId")
    suspend fun deleteHistoryItem(surahId: Int)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedSurahEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getDownload(id: String): DownloadedSurahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadedSurahEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteDownload(id: String)

    @Query("DELETE FROM downloads")
    suspend fun clearDownloads()

    // API Cache
    @Query("SELECT * FROM api_cache WHERE endpointKey = :key")
    suspend fun getCachedApi(key: String): CachedApiResponse?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedApi(cache: CachedApiResponse)

    @Query("DELETE FROM api_cache")
    suspend fun clearCache()
}

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class, DownloadedSurahEntity::class, CachedApiResponse::class],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
}
