package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SuwarResponse(
    @Json(name = "suwar") val suwar: List<SurahDto>? = null
)

@JsonClass(generateAdapter = true)
data class SurahDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "makkia") val makkia: Int? = null,
    @Json(name = "type") val type: Int? = null,
    @Json(name = "page") val page: Int? = null,
    @Json(name = "start_page") val startPage: Int? = null,
    @Json(name = "end_page") val endPage: Int? = null,
    @Json(name = "total_verses") val totalVerses: Int? = null
) {
    val isMakkia: Boolean get() = makkia == 1 || type == 0
}

@JsonClass(generateAdapter = true)
data class RecitersResponse(
    @Json(name = "reciters") val reciters: List<ReciterDto>? = null
)

@JsonClass(generateAdapter = true)
data class ReciterDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "letter") val letter: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "moshaf") val moshaf: List<MoshafDto>? = null
)

@JsonClass(generateAdapter = true)
data class MoshafDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "server") val server: String? = null,
    @Json(name = "surah_total") val surahTotal: Int? = null,
    @Json(name = "moshaf_type") val moshafType: Int? = null,
    @Json(name = "surah_list") val surahList: String? = null
) {
    fun hasSurah(surahId: Int): Boolean {
        if (surahList.isNullOrBlank()) return true
        val list = surahList.split(",").mapNotNull { it.trim().toIntOrNull() }
        return list.isEmpty() || list.contains(surahId)
    }

    fun getAudioUrl(surahId: Int): String {
        val serverUrl = (server ?: "").trimEnd('/')
        val formattedNumber = String.format("%03d", surahId)
        return "$serverUrl/$formattedNumber.mp3"
    }
}

@JsonClass(generateAdapter = true)
data class RiwayatResponse(
    @Json(name = "riwayat") val riwayat: List<RiwayahDto>? = null
)

@JsonClass(generateAdapter = true)
data class RiwayahDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class LanguagesResponse(
    @Json(name = "language") val languages: List<LanguageDto>? = null
)

@JsonClass(generateAdapter = true)
data class LanguageDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "native") val native: String? = null,
    @Json(name = "locale") val locale: String? = null
)

@JsonClass(generateAdapter = true)
data class TafasirResponse(
    @Json(name = "tafasir") val tafasir: TafasirContentDto? = null
)

@JsonClass(generateAdapter = true)
data class TafasirContentDto(
    @Json(name = "name") val name: String? = null,
    @Json(name = "soar") val soar: List<TafsirSurahDto>? = null
)

@JsonClass(generateAdapter = true)
data class TafsirSurahDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "sura_id") val suraId: Int? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class TadaborResponse(
    @Json(name = "tadabor") val tadabor: Map<String, List<TadaborAyahDto>>? = null
)

@JsonClass(generateAdapter = true)
data class TadaborAyahDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "sura_id") val suraId: Int? = null,
    @Json(name = "aya_id") val ayaId: Int? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class VideosResponse(
    @Json(name = "videos") val videos: List<VideoGroupDto>? = null
)

@JsonClass(generateAdapter = true)
data class VideoGroupDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "reciter_name") val reciterName: String? = null,
    @Json(name = "videos") val videos: List<VideoItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class VideoItemDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "video_thumb_url") val videoThumbUrl: String? = null,
    @Json(name = "video_type") val videoType: Int? = null,
    @Json(name = "video_title") val videoTitle: String? = null
)

@JsonClass(generateAdapter = true)
data class RadiosResponse(
    @Json(name = "radios") val radios: List<RadioDto>? = null
)

@JsonClass(generateAdapter = true)
data class RadioDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "recent_date") val recentDate: String? = null
)

@JsonClass(generateAdapter = true)
data class AyatTimingResponse(
    @Json(name = "ayah_timing") val timings: List<AyahTimingDto>? = null
)

@JsonClass(generateAdapter = true)
data class AyahTimingDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "ayah") val ayah: Int? = null,
    @Json(name = "start_time") val startTimeMs: Long? = null,
    @Json(name = "end_time") val endTimeMs: Long? = null
)
