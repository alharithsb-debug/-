package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.QuranApplication
import com.example.data.local.DownloadedSurahEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.HistoryEntity
import com.example.data.model.*
import com.example.data.repository.QuranRepository
import com.example.player.AudioPlayerManager
import com.example.player.NowPlayingTrack
import com.example.player.PlayerState
import com.example.player.RepeatMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class ScreenDestination {
    object Splash : ScreenDestination()
    object Home : ScreenDestination()
    object Surahs : ScreenDestination()
    object Reciters : ScreenDestination()
    object Favorites : ScreenDestination()
    object Settings : ScreenDestination()
    object ReadingAndTiming : ScreenDestination()
    object Tafasir : ScreenDestination()
    object Tadabor : ScreenDestination()
    object Videos : ScreenDestination()
    object Radios : ScreenDestination()
    object History : ScreenDestination()
    object Downloads : ScreenDestination()
    object Search : ScreenDestination()
}

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class ShareData(
    val title: String,
    val text: String,
    val subtitle: String? = null
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuranRepository = (application as QuranApplication).repository
    val playerManager: AudioPlayerManager = (application as QuranApplication).playerManager

    val playerState: StateFlow<PlayerState> = playerManager.playerState

    // Navigation & UI Sheets
    private val _currentScreen = MutableStateFlow<ScreenDestination>(ScreenDestination.Splash)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _fullPlayerVisible = MutableStateFlow(false)
    val fullPlayerVisible: StateFlow<Boolean> = _fullPlayerVisible.asStateFlow()

    private val _shareDialogData = MutableStateFlow<ShareData?>(null)
    val shareDialogData: StateFlow<ShareData?> = _shareDialogData.asStateFlow()

    // Settings
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appLanguage = MutableStateFlow("ar")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _readingFontScale = MutableStateFlow(1.15f)
    val readingFontScale: StateFlow<Float> = _readingFontScale.asStateFlow()

    // API Data
    private val _suwarState = MutableStateFlow<UiState<List<SurahDto>>>(UiState.Loading)
    val suwarState: StateFlow<UiState<List<SurahDto>>> = _suwarState.asStateFlow()

    private val _recitersState = MutableStateFlow<UiState<List<ReciterDto>>>(UiState.Loading)
    val recitersState: StateFlow<UiState<List<ReciterDto>>> = _recitersState.asStateFlow()

    private val _riwayatState = MutableStateFlow<UiState<List<RiwayahDto>>>(UiState.Loading)
    val riwayatState: StateFlow<UiState<List<RiwayahDto>>> = _riwayatState.asStateFlow()

    private val _languagesState = MutableStateFlow<UiState<List<LanguageDto>>>(UiState.Loading)
    val languagesState: StateFlow<UiState<List<LanguageDto>>> = _languagesState.asStateFlow()

    private val _radiosState = MutableStateFlow<UiState<List<RadioDto>>>(UiState.Loading)
    val radiosState: StateFlow<UiState<List<RadioDto>>> = _radiosState.asStateFlow()

    private val _videosState = MutableStateFlow<UiState<List<VideoGroupDto>>>(UiState.Loading)
    val videosState: StateFlow<UiState<List<VideoGroupDto>>> = _videosState.asStateFlow()

    // Interactive Modules
    private val _tadaborSurah = MutableStateFlow(1)
    val tadaborSurah: StateFlow<Int> = _tadaborSurah.asStateFlow()

    private val _tadaborState = MutableStateFlow<UiState<List<TadaborAyahDto>>>(UiState.Loading)
    val tadaborState: StateFlow<UiState<List<TadaborAyahDto>>> = _tadaborState.asStateFlow()

    private val _selectedTafsirSurah = MutableStateFlow(1)
    val selectedTafsirSurah: StateFlow<Int> = _selectedTafsirSurah.asStateFlow()

    private val _tafasirState = MutableStateFlow<UiState<TafasirContentDto>>(UiState.Loading)
    val tafasirState: StateFlow<UiState<TafasirContentDto>> = _tafasirState.asStateFlow()

    private val _timingSurah = MutableStateFlow(1)
    val timingSurah: StateFlow<Int> = _timingSurah.asStateFlow()

    private val _timingState = MutableStateFlow<UiState<List<AyahTimingDto>>>(UiState.Loading)
    val timingState: StateFlow<UiState<List<AyahTimingDto>>> = _timingState.asStateFlow()

    // Filters and search states
    val surahSearchQuery = MutableStateFlow("")
    val surahTypeFilter = MutableStateFlow<Int?>(null) // null = all, 1 = makkia, 0 = madania
    val surahSortAlphabetical = MutableStateFlow(false)

    val reciterSearchQuery = MutableStateFlow("")
    val selectedRiwayahId = MutableStateFlow<Int?>(null)

    val radioSearchQuery = MutableStateFlow("")
    val globalSearchQuery = MutableStateFlow("")

    // Selected Reciter for viewing their full Surahs list
    private val _selectedReciter = MutableStateFlow<ReciterDto?>(null)
    val selectedReciter: StateFlow<ReciterDto?> = _selectedReciter.asStateFlow()

    // Downloads in-progress map
    private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgressMap: StateFlow<Map<String, Float>> = _downloadProgressMap.asStateFlow()

    // Room DB Flow states
    val favorites: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastListened: StateFlow<HistoryEntity?> = repository.getLastListened()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val downloads: StateFlow<List<DownloadedSurahEntity>> = repository.getAllDownloads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        fetchSuwar()
        fetchReciters()
        fetchRiwayat()
        fetchLanguages()
        fetchRadios()
        fetchVideos()
        fetchTadabor(1)
        fetchTafasir(1)
        fetchAyatTiming(1, 5)
    }

    fun navigateTo(screen: ScreenDestination) {
        _currentScreen.value = screen
    }

    fun setFullPlayerVisible(visible: Boolean) {
        _fullPlayerVisible.value = visible
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }

    fun setAppLanguage(lang: String) {
        _appLanguage.value = lang
        // Refresh APIs with new language
        fetchSuwar(lang, forceRefresh = true)
        fetchReciters(lang, forceRefresh = true)
        fetchRiwayat(lang)
        fetchRadios(lang)
        fetchVideos(lang)
        fetchTafasir(_selectedTafsirSurah.value, lang)
        fetchTadabor(_tadaborSurah.value, lang)
    }

    fun setReadingFontScale(scale: Float) {
        _readingFontScale.value = scale.coerceIn(0.8f, 2.2f)
    }

    fun setSelectedReciter(reciter: ReciterDto?) {
        _selectedReciter.value = reciter
    }

    fun showShareDialog(title: String, text: String, subtitle: String? = null) {
        _shareDialogData.value = ShareData(title, text, subtitle)
    }

    fun dismissShareDialog() {
        _shareDialogData.value = null
    }

    // Fetch APIs
    fun fetchSuwar(lang: String = _appLanguage.value, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _suwarState.value = UiState.Loading
            repository.getSuwar(lang, forceRefresh)
                .onSuccess { _suwarState.value = UiState.Success(it) }
                .onFailure { _suwarState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل السور") }
        }
    }

    fun fetchReciters(lang: String = _appLanguage.value, rewaya: Int? = selectedRiwayahId.value, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _recitersState.value = UiState.Loading
            repository.getReciters(lang, rewaya, forceRefresh)
                .onSuccess { _recitersState.value = UiState.Success(it) }
                .onFailure { _recitersState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل القراء") }
        }
    }

    fun fetchRiwayat(lang: String = _appLanguage.value) {
        viewModelScope.launch {
            _riwayatState.value = UiState.Loading
            repository.getRiwayat(lang)
                .onSuccess { _riwayatState.value = UiState.Success(it) }
                .onFailure { _riwayatState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل الروايات") }
        }
    }

    fun fetchLanguages() {
        viewModelScope.launch {
            _languagesState.value = UiState.Loading
            repository.getLanguages()
                .onSuccess { _languagesState.value = UiState.Success(it) }
                .onFailure { _languagesState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل اللغات") }
        }
    }

    fun fetchRadios(lang: String = _appLanguage.value, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _radiosState.value = UiState.Loading
            repository.getRadios(lang, forceRefresh)
                .onSuccess { _radiosState.value = UiState.Success(it) }
                .onFailure { _radiosState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل الإذاعات") }
        }
    }

    fun fetchVideos(lang: String = _appLanguage.value, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _videosState.value = UiState.Loading
            repository.getVideos(lang, forceRefresh)
                .onSuccess { _videosState.value = UiState.Success(it) }
                .onFailure { _videosState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل الفيديوهات") }
        }
    }

    fun fetchTadabor(suraId: Int, lang: String = _appLanguage.value) {
        _tadaborSurah.value = suraId
        viewModelScope.launch {
            _tadaborState.value = UiState.Loading
            repository.getTadabor(suraId, lang)
                .onSuccess { _tadaborState.value = UiState.Success(it) }
                .onFailure { _tadaborState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل تدبر السورة") }
        }
    }

    fun fetchTafasir(suraId: Int, lang: String = _appLanguage.value, tafsirId: Int? = null) {
        _selectedTafsirSurah.value = suraId
        viewModelScope.launch {
            _tafasirState.value = UiState.Loading
            repository.getTafasir(lang, tafsirId, suraId)
                .onSuccess { _tafasirState.value = UiState.Success(it) }
                .onFailure { _tafasirState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل التفسير") }
        }
    }

    fun fetchAyatTiming(surah: Int, read: Int = 5) {
        _timingSurah.value = surah
        viewModelScope.launch {
            _timingState.value = UiState.Loading
            repository.getAyatTiming(surah, read)
                .onSuccess { _timingState.value = UiState.Success(it) }
                .onFailure { _timingState.value = UiState.Error(it.localizedMessage ?: "تعذر تحميل توقيت الآيات") }
        }
    }

    // Playback Helpers
    fun playSurah(
        surah: SurahDto,
        reciter: ReciterDto? = null,
        moshaf: MoshafDto? = null,
        allSuwarList: List<SurahDto> = emptyList(),
        startPositionMs: Long = 0L
    ) {
        // Choose default reciter if none provided: e.g. Mishary Rashid Alafasy or first reciter
        val targetReciter = reciter ?: (_recitersState.value as? UiState.Success)?.data?.firstOrNull { it.id == 54 || it.name?.contains("العفاسي") == true }
            ?: (_recitersState.value as? UiState.Success)?.data?.firstOrNull()
            ?: ReciterDto(id = 54, name = "مشاري راشد العفاسي", moshaf = listOf(MoshafDto(id = 1, name = "حفص عن عاصم - مرتل", server = "https://server8.mp3quran.net/afs/")))

        val targetMoshaf = moshaf ?: targetReciter.moshaf?.firstOrNull()
            ?: MoshafDto(id = 1, name = "حفص عن عاصم", server = "https://server8.mp3quran.net/afs/")

        val audioUrl = targetMoshaf.getAudioUrl(surah.id)

        // Check if downloaded
        val downloadKey = "${targetReciter.id}_${surah.id}"
        val localDownload = downloads.value.firstOrNull { it.id == downloadKey }
        val finalUrl = localDownload?.localFilePath ?: audioUrl

        val currentTrack = NowPlayingTrack(
            id = downloadKey,
            surahId = surah.id,
            surahName = surah.name ?: "سورة رقم ${surah.id}",
            reciterId = targetReciter.id,
            reciterName = targetReciter.name ?: "القارئ",
            moshafName = targetMoshaf.name ?: "المصحف",
            audioUrl = finalUrl,
            isRadio = false,
            isDownloaded = localDownload != null
        )

        // Build playlist queue for auto-next
        val playlistQueue = if (allSuwarList.isNotEmpty()) {
            allSuwarList.map { s ->
                val sDownload = downloads.value.firstOrNull { it.id == "${targetReciter.id}_${s.id}" }
                NowPlayingTrack(
                    id = "${targetReciter.id}_${s.id}",
                    surahId = s.id,
                    surahName = s.name ?: "سورة رقم ${s.id}",
                    reciterId = targetReciter.id,
                    reciterName = targetReciter.name ?: "القارئ",
                    moshafName = targetMoshaf.name ?: "المصحف",
                    audioUrl = sDownload?.localFilePath ?: targetMoshaf.getAudioUrl(s.id),
                    isRadio = false,
                    isDownloaded = sDownload != null
                )
            }
        } else {
            listOf(currentTrack)
        }

        playerManager.playTrack(currentTrack, playlistQueue, startPositionMs)
    }

    fun playRadio(radio: RadioDto) {
        val streamUrl = radio.url ?: return
        val track = NowPlayingTrack(
            id = "radio_${radio.id}",
            surahId = 0,
            surahName = radio.name ?: "إذاعة القرآن الكريم",
            reciterId = 0,
            reciterName = "بث إذاعي مباشر",
            moshafName = "إذاعات القرآن",
            audioUrl = streamUrl,
            isRadio = true
        )
        playerManager.playTrack(track, listOf(track))
    }

    fun playDownloadedSurah(download: DownloadedSurahEntity) {
        val track = NowPlayingTrack(
            id = download.id,
            surahId = download.surahId,
            surahName = download.surahName,
            reciterId = download.reciterId,
            reciterName = download.reciterName,
            moshafName = "تشغيل دون اتصال",
            audioUrl = download.localFilePath,
            isRadio = false,
            isDownloaded = true
        )
        val playlist = downloads.value.map { d ->
            NowPlayingTrack(
                id = d.id,
                surahId = d.surahId,
                surahName = d.surahName,
                reciterId = d.reciterId,
                reciterName = d.reciterName,
                moshafName = "تشغيل دون اتصال",
                audioUrl = d.localFilePath,
                isRadio = false,
                isDownloaded = true
            )
        }
        playerManager.playTrack(track, playlist)
    }

    fun resumeLastListened() {
        val last = lastListened.value ?: return
        val suwar = (_suwarState.value as? UiState.Success)?.data ?: emptyList()
        val surah = suwar.firstOrNull { it.id == last.surahId } ?: SurahDto(id = last.surahId, name = last.surahName)
        val reciter = (_recitersState.value as? UiState.Success)?.data?.firstOrNull { it.id == last.reciterId }
            ?: ReciterDto(id = last.reciterId, name = last.reciterName)

        val track = NowPlayingTrack(
            id = "${last.reciterId}_${last.surahId}",
            surahId = last.surahId,
            surahName = last.surahName,
            reciterId = last.reciterId,
            reciterName = last.reciterName,
            moshafName = last.moshafName,
            audioUrl = last.audioUrl,
            isRadio = false
        )
        playerManager.playTrack(track, listOf(track), last.positionMs)
    }

    // Favorites
    fun toggleFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(favorite)
        }
    }

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }

    fun isFavorite(id: String): Flow<Boolean> = repository.isFavorite(id)

    // History
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteHistoryItem(surahId: Int) {
        viewModelScope.launch {
            repository.deleteHistoryItem(surahId)
        }
    }

    // Downloads
    fun downloadSurah(surah: SurahDto, reciter: ReciterDto, moshaf: MoshafDto) {
        val key = "${reciter.id}_${surah.id}"
        val audioUrl = moshaf.getAudioUrl(surah.id)
        viewModelScope.launch {
            _downloadProgressMap.value = _downloadProgressMap.value + (key to 0.05f)
            repository.downloadSurahAudio(
                reciterId = reciter.id,
                reciterName = reciter.name ?: "القارئ",
                surahId = surah.id,
                surahName = surah.name ?: "سورة ${surah.id}",
                audioUrl = audioUrl,
                onProgress = { progress ->
                    _downloadProgressMap.value = _downloadProgressMap.value + (key to progress)
                }
            ).onSuccess {
                _downloadProgressMap.value = _downloadProgressMap.value - key
            }.onFailure {
                _downloadProgressMap.value = _downloadProgressMap.value - key
            }
        }
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch {
            repository.deleteDownload(id)
        }
    }

    fun clearAllDownloads() {
        viewModelScope.launch {
            repository.clearAllDownloads()
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            loadInitialData()
        }
    }
}
