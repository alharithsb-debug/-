package com.example.data.repository

import android.content.Context
import com.example.data.local.DownloadedSurahEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.QuranDao
import com.example.data.model.*
import com.example.data.remote.NetworkModule
import com.example.data.remote.QuranApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class QuranRepository(
    private val context: Context,
    private val quranDao: QuranDao,
    private val apiService: QuranApiService = NetworkModule.apiService
) {
    // In-memory memory cache for fast navigation without re-requesting
    private var cachedSuwar: List<SurahDto>? = null
    private var cachedReciters: List<ReciterDto>? = null
    private var cachedRiwayat: List<RiwayahDto>? = null
    private var cachedRadios: List<RadioDto>? = null
    private var cachedVideos: List<VideoGroupDto>? = null
    private var cachedLanguages: List<LanguageDto>? = null

    // 1. Suwar (Surahs)
    suspend fun getSuwar(language: String = "ar", forceRefresh: Boolean = false): Result<List<SurahDto>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && !cachedSuwar.isNullOrEmpty()) {
            return@withContext Result.success(cachedSuwar!!)
        }
        try {
            val response = apiService.getSuwar(language)
            val list = response.suwar ?: emptyList()
            if (list.isNotEmpty()) {
                cachedSuwar = list
            }
            Result.success(list)
        } catch (e: Exception) {
            if (!cachedSuwar.isNullOrEmpty()) {
                Result.success(cachedSuwar!!)
            } else {
                Result.failure(e)
            }
        }
    }

    // 2. Reciters
    suspend fun getReciters(
        language: String = "ar",
        rewaya: Int? = null,
        forceRefresh: Boolean = false
    ): Result<List<ReciterDto>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && rewaya == null && !cachedReciters.isNullOrEmpty()) {
            return@withContext Result.success(cachedReciters!!)
        }
        try {
            val response = apiService.getReciters(language = language, rewaya = rewaya)
            val list = response.reciters ?: emptyList()
            if (rewaya == null && list.isNotEmpty()) {
                cachedReciters = list
            }
            Result.success(list)
        } catch (e: Exception) {
            if (rewaya == null && !cachedReciters.isNullOrEmpty()) {
                Result.success(cachedReciters!!)
            } else {
                Result.failure(e)
            }
        }
    }

    // 3. Riwayat
    suspend fun getRiwayat(language: String = "ar"): Result<List<RiwayahDto>> = withContext(Dispatchers.IO) {
        if (!cachedRiwayat.isNullOrEmpty()) {
            return@withContext Result.success(cachedRiwayat!!)
        }
        try {
            val response = apiService.getRiwayat(language)
            val list = response.riwayat ?: emptyList()
            if (list.isNotEmpty()) {
                cachedRiwayat = list
            }
            Result.success(list)
        } catch (e: Exception) {
            if (!cachedRiwayat.isNullOrEmpty()) {
                Result.success(cachedRiwayat!!)
            } else {
                Result.failure(e)
            }
        }
    }

    // 4. Languages
    suspend fun getLanguages(): Result<List<LanguageDto>> = withContext(Dispatchers.IO) {
        if (!cachedLanguages.isNullOrEmpty()) {
            return@withContext Result.success(cachedLanguages!!)
        }
        try {
            val response = apiService.getLanguages()
            val list = response.languages ?: emptyList()
            if (list.isNotEmpty()) {
                cachedLanguages = list
            }
            Result.success(list)
        } catch (e: Exception) {
            if (!cachedLanguages.isNullOrEmpty()) {
                Result.success(cachedLanguages!!)
            } else {
                Result.failure(e)
            }
        }
    }

    // 5. Tafasir
    suspend fun getTafasir(
        language: String = "ar",
        tafsirId: Int? = null,
        sura: Int? = null
    ): Result<TafasirContentDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTafasir(language = language, tafsirId = tafsirId, sura = sura)
            Result.success(response.tafasir ?: TafasirContentDto())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 6. Tadabor
    suspend fun getTadabor(sura: Int = 1, language: String = "ar"): Result<List<TadaborAyahDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTadabor(sura = sura, language = language)
            val tadaborMap = response.tadabor
            val list = tadaborMap?.values?.flatten() ?: emptyList()
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 7. Ayat Timing
    suspend fun getAyatTiming(surah: Int, read: Int): Result<List<AyahTimingDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAyatTiming(surah = surah, read = read)
            Result.success(response.timings ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 8. Videos
    suspend fun getVideos(language: String = "ar", forceRefresh: Boolean = false): Result<List<VideoGroupDto>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && !cachedVideos.isNullOrEmpty()) {
            return@withContext Result.success(cachedVideos!!)
        }
        try {
            val response = apiService.getVideos(language)
            val list = response.videos ?: emptyList()
            if (list.isNotEmpty()) {
                cachedVideos = list
            }
            Result.success(list)
        } catch (e: Exception) {
            if (!cachedVideos.isNullOrEmpty()) {
                Result.success(cachedVideos!!)
            } else {
                Result.failure(e)
            }
        }
    }

    // 9. Radios
    suspend fun getRadios(language: String = "ar", forceRefresh: Boolean = false): Result<List<RadioDto>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && !cachedRadios.isNullOrEmpty()) {
            return@withContext Result.success(cachedRadios!!)
        }
        try {
            val response = apiService.getRadios(language)
            val list = response.radios ?: emptyList()
            if (list.isNotEmpty()) {
                cachedRadios = list
            }
            Result.success(list)
        } catch (e: Exception) {
            if (!cachedRadios.isNullOrEmpty()) {
                Result.success(cachedRadios!!)
            } else {
                Result.failure(e)
            }
        }
    }

    // Favorites
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = quranDao.getAllFavorites()
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>> = quranDao.getFavoritesByType(type)
    fun isFavorite(id: String): Flow<Boolean> = quranDao.isFavorite(id)
    suspend fun isFavoriteSync(id: String): Boolean = quranDao.isFavoriteSync(id)

    suspend fun toggleFavorite(favorite: FavoriteEntity) = withContext(Dispatchers.IO) {
        if (quranDao.isFavoriteSync(favorite.id)) {
            quranDao.deleteFavorite(favorite.id)
        } else {
            quranDao.insertFavorite(favorite)
        }
    }

    suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO) {
        quranDao.deleteFavorite(id)
    }

    // History
    fun getAllHistory(): Flow<List<HistoryEntity>> = quranDao.getAllHistory()
    fun getLastListened(): Flow<HistoryEntity?> = quranDao.getLastListened()
    suspend fun getLastListenedSync(): HistoryEntity? = quranDao.getLastListenedSync()

    suspend fun recordHistory(history: HistoryEntity) = withContext(Dispatchers.IO) {
        quranDao.insertHistory(history)
    }

    suspend fun deleteHistoryItem(surahId: Int) = withContext(Dispatchers.IO) {
        quranDao.deleteHistoryItem(surahId)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        quranDao.clearHistory()
    }

    // Downloads
    fun getAllDownloads(): Flow<List<DownloadedSurahEntity>> = quranDao.getAllDownloads()
    suspend fun getDownload(id: String): DownloadedSurahEntity? = quranDao.getDownload(id)

    suspend fun downloadSurahAudio(
        reciterId: Int,
        reciterName: String,
        surahId: Int,
        surahName: String,
        audioUrl: String,
        onProgress: (Float) -> Unit
    ): Result<DownloadedSurahEntity> = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(audioUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download audio: ${response.code}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty body"))
            val contentLength = body.contentLength()

            val downloadsDir = File(context.filesDir, "quran_audio").apply { mkdirs() }
            val destFile = File(downloadsDir, "${reciterId}_${surahId}.mp3")

            body.byteStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead: Long = 0
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            onProgress(totalRead.toFloat() / contentLength.toFloat())
                        }
                    }
                    output.flush()
                }
            }

            val entity = DownloadedSurahEntity(
                id = "${reciterId}_${surahId}",
                surahId = surahId,
                surahName = surahName,
                reciterId = reciterId,
                reciterName = reciterName,
                localFilePath = destFile.absolutePath,
                fileSize = destFile.length()
            )
            quranDao.insertDownload(entity)
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDownload(id: String) = withContext(Dispatchers.IO) {
        val download = quranDao.getDownload(id)
        if (download != null) {
            try {
                File(download.localFilePath).delete()
            } catch (_: Exception) {}
            quranDao.deleteDownload(id)
        }
    }

    suspend fun clearAllDownloads() = withContext(Dispatchers.IO) {
        try {
            val downloadsDir = File(context.filesDir, "quran_audio")
            downloadsDir.deleteRecursively()
        } catch (_: Exception) {}
        quranDao.clearDownloads()
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        cachedSuwar = null
        cachedReciters = null
        cachedRiwayat = null
        cachedRadios = null
        cachedVideos = null
        cachedLanguages = null
        quranDao.clearCache()
    }
}
