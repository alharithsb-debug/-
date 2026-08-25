package com.example.player

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.CountDownTimer
import com.example.data.local.HistoryEntity
import com.example.data.repository.QuranRepository
import com.example.service.AudioPlaybackService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class NowPlayingTrack(
    val id: String, // e.g. "reciterId_surahId" or "radio_1"
    val surahId: Int = 0,
    val surahName: String = "",
    val reciterId: Int = 0,
    val reciterName: String = "",
    val moshafName: String = "",
    val audioUrl: String = "",
    val isRadio: Boolean = false,
    val isDownloaded: Boolean = false
)

enum class RepeatMode {
    OFF, REPEAT_ONE, REPEAT_ALL
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentTrack: NowPlayingTrack? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.REPEAT_ALL,
    val sleepTimerMinutesRemaining: Int? = null,
    val volume: Float = 1.0f,
    val errorMessage: String? = null
) {
    val progressRatio: Float
        get() = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

class AudioPlayerManager private constructor(private val appContext: Context) {

    companion object {
        @Volatile
        private var instance: AudioPlayerManager? = null

        fun getInstance(context: Context): AudioPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: AudioPlayerManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var repository: QuranRepository? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    // Playlist queue for auto-next
    private var playlist: List<NowPlayingTrack> = emptyList()
    private var currentPlaylistIndex: Int = -1

    private var tickerJob: Job? = null
    private var sleepTimer: CountDownTimer? = null

    fun initRepository(repo: QuranRepository) {
        this.repository = repo
    }

    fun playTrack(
        track: NowPlayingTrack,
        playlistQueue: List<NowPlayingTrack> = listOf(track),
        startPositionMs: Long = 0L
    ) {
        this.playlist = playlistQueue
        this.currentPlaylistIndex = playlistQueue.indexOfFirst { it.id == track.id }.let { if (it >= 0) it else 0 }

        _playerState.value = _playerState.value.copy(
            currentTrack = track,
            isBuffering = true,
            errorMessage = null,
            currentPositionMs = startPositionMs
        )

        startForegroundService()
        prepareAndPlay(track.audioUrl, startPositionMs)
    }

    private fun prepareAndPlay(urlOrPath: String, seekToMs: Long = 0L) {
        scope.launch(Dispatchers.IO) {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )

                    // Check if local file exists
                    val localFile = File(urlOrPath)
                    if (localFile.exists()) {
                        setDataSource(localFile.absolutePath)
                    } else {
                        setDataSource(urlOrPath)
                    }

                    setOnPreparedListener { mp ->
                        scope.launch(Dispatchers.Main) {
                            if (seekToMs > 0) {
                                mp.seekTo(seekToMs.toInt())
                            }
                            applyPlaybackSpeed(_playerState.value.playbackSpeed)
                            mp.start()
                            _playerState.value = _playerState.value.copy(
                                isPlaying = true,
                                isBuffering = false,
                                durationMs = if (mp.duration > 0) mp.duration.toLong() else 0L
                            )
                            startProgressTicker()
                            updateServiceNotification()
                        }
                    }

                    setOnCompletionListener {
                        scope.launch(Dispatchers.Main) {
                            onTrackCompleted()
                        }
                    }

                    setOnErrorListener { _, what, extra ->
                        scope.launch(Dispatchers.Main) {
                            _playerState.value = _playerState.value.copy(
                                isPlaying = false,
                                isBuffering = false,
                                errorMessage = "تعذر تشغيل الملف الصوتي ($what, $extra)"
                            )
                            stopProgressTicker()
                        }
                        true
                    }

                    prepareAsync()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "خطأ في تهيئة المشغل: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
            stopProgressTicker()
            recordHistorySnapshot()
        } else {
            mp.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
            startProgressTicker()
        }
        updateServiceNotification()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            val target = positionMs.coerceIn(0L, _playerState.value.durationMs.coerceAtLeast(1L))
            mp.seekTo(target.toInt())
            _playerState.value = _playerState.value.copy(currentPositionMs = target)
        }
    }

    fun seekForward(offsetMs: Long = 10000L) {
        val current = _playerState.value.currentPositionMs
        val target = (current + offsetMs).coerceAtMost(_playerState.value.durationMs)
        seekTo(target)
    }

    fun seekBackward(offsetMs: Long = 10000L) {
        val current = _playerState.value.currentPositionMs
        val target = (current - offsetMs).coerceAtLeast(0L)
        seekTo(target)
    }

    fun skipNext() {
        if (playlist.isEmpty()) return
        val nextIndex = currentPlaylistIndex + 1
        if (nextIndex < playlist.size) {
            playTrack(playlist[nextIndex], playlist)
        } else if (_playerState.value.repeatMode == RepeatMode.REPEAT_ALL) {
            playTrack(playlist[0], playlist)
        }
    }

    fun skipPrevious() {
        if (playlist.isEmpty()) return
        val prevIndex = currentPlaylistIndex - 1
        if (prevIndex >= 0) {
            playTrack(playlist[prevIndex], playlist)
        } else {
            seekTo(0)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply {
                    this.speed = speed
                }
            } catch (_: Exception) {}
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _playerState.value = _playerState.value.copy(repeatMode = mode)
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
        _playerState.value = _playerState.value.copy(volume = clamped)
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimer?.cancel()
        sleepTimer = null
        if (minutes == null || minutes <= 0) {
            _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = null)
            return
        }

        _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = minutes)
        val totalMs = minutes * 60 * 1000L

        sleepTimer = object : CountDownTimer(totalMs, 60000L) {
            override fun onTick(millisUntilFinished: Long) {
                val minsLeft = (millisUntilFinished / 60000L).toInt() + 1
                _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = minsLeft)
            }

            override fun onFinish() {
                _playerState.value = _playerState.value.copy(sleepTimerMinutesRemaining = null)
                pauseAndStop()
            }
        }.start()
    }

    private fun onTrackCompleted() {
        when (_playerState.value.repeatMode) {
            RepeatMode.REPEAT_ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _playerState.value = _playerState.value.copy(isPlaying = true)
                startProgressTicker()
            }
            RepeatMode.REPEAT_ALL -> {
                skipNext()
            }
            RepeatMode.OFF -> {
                _playerState.value = _playerState.value.copy(isPlaying = false, currentPositionMs = _playerState.value.durationMs)
                stopProgressTicker()
                recordHistorySnapshot()
            }
        }
        updateServiceNotification()
    }

    private fun startProgressTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        val current = mp.currentPosition.toLong()
                        val duration = if (mp.duration > 0) mp.duration.toLong() else _playerState.value.durationMs
                        _playerState.value = _playerState.value.copy(
                            currentPositionMs = current,
                            durationMs = duration
                        )
                        // Save history checkpoint periodically
                        if (current % 10000L < 1000L) {
                            recordHistorySnapshot()
                        }
                    }
                }
                delay(800L)
            }
        }
    }

    private fun stopProgressTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun recordHistorySnapshot() {
        val track = _playerState.value.currentTrack ?: return
        if (track.isRadio || track.surahId == 0) return
        val current = _playerState.value.currentPositionMs
        val dur = _playerState.value.durationMs
        val ratio = if (dur > 0) current.toFloat() / dur.toFloat() else 0f

        scope.launch(Dispatchers.IO) {
            repository?.recordHistory(
                HistoryEntity(
                    surahId = track.surahId,
                    surahName = track.surahName,
                    reciterId = track.reciterId,
                    reciterName = track.reciterName,
                    moshafName = track.moshafName,
                    audioUrl = track.audioUrl,
                    positionMs = current,
                    durationMs = dur,
                    progressRatio = ratio
                )
            )
        }
    }

    fun pauseAndStop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause()
        }
        stopProgressTicker()
        recordHistorySnapshot()
        _playerState.value = _playerState.value.copy(isPlaying = false)
        updateServiceNotification()
    }

    fun stop() {
        stopAndClose()
    }

    fun stopAndClose() {
        stopProgressTicker()
        sleepTimer?.cancel()
        recordHistorySnapshot()
        mediaPlayer?.release()
        mediaPlayer = null
        _playerState.value = PlayerState()
        stopForegroundService()
    }

    private fun startForegroundService() {
        try {
            val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun stopForegroundService() {
        try {
            val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_STOP
            }
            appContext.stopService(intent)
        } catch (_: Exception) {}
    }

    private fun updateServiceNotification() {
        try {
            val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_UPDATE
            }
            appContext.startService(intent)
        } catch (_: Exception) {}
    }
}
