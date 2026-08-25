package com.example.data.remote

import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object QuranApiConfig {
    const val BASE_URL = "https://mp3quran.net/api/v3/"
    const val VIDEOS_BASE_URL = "https://www.mp3quran.net/api/v3/"

    // Exact Endpoints mandated by user request:
    const val LANGUAGES_ENDPOINT = "languages"
    const val SUWAR_ENDPOINT = "suwar"
    const val RIWAYAT_ENDPOINT = "riwayat"
    const val RECITERS_ENDPOINT = "reciters"
    const val TAFASIR_ENDPOINT = "tafasir"
    const val TADABOR_ENDPOINT = "tadabor"
    const val AYAT_TIMING_ENDPOINT = "ayat_timing"
    const val VIDEOS_ENDPOINT = "videos"
    const val RADIOS_ENDPOINT = "radios"
}

interface QuranApiService {

    @GET(QuranApiConfig.LANGUAGES_ENDPOINT)
    suspend fun getLanguages(): LanguagesResponse

    @GET(QuranApiConfig.SUWAR_ENDPOINT)
    suspend fun getSuwar(
        @Query("language") language: String = "ar"
    ): SuwarResponse

    @GET(QuranApiConfig.RIWAYAT_ENDPOINT)
    suspend fun getRiwayat(
        @Query("language") language: String = "ar"
    ): RiwayatResponse

    @GET(QuranApiConfig.RECITERS_ENDPOINT)
    suspend fun getReciters(
        @Query("language") language: String = "ar",
        @Query("rewaya") rewaya: Int? = null,
        @Query("reciter") reciter: Int? = null
    ): RecitersResponse

    @GET(QuranApiConfig.TAFASIR_ENDPOINT)
    suspend fun getTafasir(
        @Query("language") language: String = "ar",
        @Query("tafsir") tafsirId: Int? = null,
        @Query("sura") sura: Int? = null
    ): TafasirResponse

    @GET(QuranApiConfig.TADABOR_ENDPOINT)
    suspend fun getTadabor(
        @Query("sura") sura: Int = 1,
        @Query("language") language: String = "ar"
    ): TadaborResponse

    @GET(QuranApiConfig.AYAT_TIMING_ENDPOINT)
    suspend fun getAyatTiming(
        @Query("surah") surah: Int,
        @Query("read") read: Int
    ): AyatTimingResponse

    @GET(QuranApiConfig.VIDEOS_ENDPOINT)
    suspend fun getVideos(
        @Query("language") language: String = "ar"
    ): VideosResponse

    @GET(QuranApiConfig.RADIOS_ENDPOINT)
    suspend fun getRadios(
        @Query("language") language: String = "ar"
    ): RadiosResponse
}

object NetworkModule {
    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val apiService: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(QuranApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .build()
            .create(QuranApiService::class.java)
    }
}
