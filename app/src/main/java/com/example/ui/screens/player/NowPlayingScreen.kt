package com.example.ui.screens.player

import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode as AnimationRepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FavoriteEntity
import com.example.player.RepeatMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    viewModel: QuranViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.playerState.collectAsState()
    val track = playerState.currentTrack
    val favorites by viewModel.favorites.collectAsState()

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    val isFavorite = track?.let { t ->
        favorites.any { it.id == "SURAH_${t.surahId}" || it.id == t.id }
    } ?: false

    val infiniteTransition = rememberInfiniteTransition(label = "aura")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = AnimationRepeatMode.Reverse
        ),
        label = "aura"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_now_playing")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "تصغير",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "مشغل القرآن الكريم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        if (track != null) {
                            viewModel.showShareDialog(
                                title = track.surahName,
                                text = "استمع الآن إلى ${track.surahName} بصوت القارئ ${track.reciterName} عبر تطبيق القرآن الكريم | Quran Voice.",
                                subtitle = track.moshafName
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Artwork Card
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(if (playerState.isPlaying) auraScale else 1f)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                IslamicGoldPrimary.copy(alpha = 0.45f),
                                IslamicEmeraldCard,
                                IslamicEmeraldDark
                            )
                        )
                    )
                    .border(3.dp, IslamicGoldGradient, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        imageVector = if (track?.isRadio == true) Icons.Default.Radio else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = track?.surahName?.ifBlank { "القرآن الكريم" } ?: "القرآن الكريم",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGoldSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (track?.surahId != null && track.surahId > 0) {
                        Text(
                            text = "سورة رقم ${track.surahId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicSoftWhite.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Track Meta
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = track?.surahName?.ifBlank { "إذاعة القرآن الكريم" } ?: "القرآن الكريم",
                    style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = track?.reciterName?.ifBlank { "بث مباشر" } ?: "Quran Voice",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                if (!track?.moshafName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track!!.moshafName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = playerState.progressRatio,
                    onValueChange = { ratio ->
                        val newPos = (ratio * playerState.durationMs).toLong()
                        viewModel.playerManager.seekTo(newPos)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audio_seek_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(playerState.currentPositionMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(playerState.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Media Controls (Prev, -10s, Play/Pause, +10s, Next)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Surah
                IconButton(
                    onClick = { viewModel.playerManager.skipPrevious() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "السورة السابقة",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Rewind 10s
                IconButton(
                    onClick = { viewModel.playerManager.seekBackward(10000L) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "تراجع 10 ثوان",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Play / Pause Main Button
                Surface(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.playerManager.togglePlayPause() }
                        .shadow(12.dp, CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (playerState.isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                // Forward 10s
                IconButton(
                    onClick = { viewModel.playerManager.seekForward(10000L) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "تقديم 10 ثوان",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Next Surah
                IconButton(
                    onClick = { viewModel.playerManager.skipNext() },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "السورة التالية",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Secondary Controls (Speed, Repeat, Sleep Timer, Favorite, Volume)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Playback Speed
                IconButton(onClick = { showSpeedDialog = true }) {
                    Text(
                        text = "${playerState.playbackSpeed}x",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Repeat Mode Toggle
                IconButton(
                    onClick = {
                        val nextMode = when (playerState.repeatMode) {
                            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
                            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
                            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
                        }
                        viewModel.playerManager.setRepeatMode(nextMode)
                    }
                ) {
                    Icon(
                        imageVector = when (playerState.repeatMode) {
                            RepeatMode.REPEAT_ONE -> Icons.Default.RepeatOne
                            RepeatMode.REPEAT_ALL -> Icons.Default.Repeat
                            RepeatMode.OFF -> Icons.Outlined.Repeat
                        },
                        contentDescription = "تكرار",
                        tint = if (playerState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sleep Timer
                IconButton(onClick = { showSleepTimerDialog = true }) {
                    BadgedBox(
                        badge = {
                            if (playerState.sleepTimerMinutesRemaining != null) {
                                Badge { Text("${playerState.sleepTimerMinutesRemaining}m") }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "مؤقت النوم",
                            tint = if (playerState.sleepTimerMinutesRemaining != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite
                IconButton(
                    onClick = {
                        if (track != null) {
                            viewModel.toggleFavorite(
                                FavoriteEntity(
                                    id = if (track.isRadio) track.id else "SURAH_${track.surahId}",
                                    type = if (track.isRadio) "RADIO" else "SURAH",
                                    targetId = track.surahId,
                                    title = track.surahName,
                                    subtitle = track.reciterName
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) IslamicGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Playback Speed Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
        Dialog(onDismissRequest = { showSpeedDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "سرعة التشغيل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    speeds.forEach { speed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.playerManager.setPlaybackSpeed(speed)
                                    showSpeedDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${speed}x ${if (speed == 1.0f) "(افتراضي)" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (playerState.playbackSpeed == speed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        val timers = listOf(
            0 to "إيقاف المؤقت",
            15 to "15 دقيقة",
            30 to "30 دقيقة",
            45 to "45 دقيقة",
            60 to "ساعة واحدة"
        )
        Dialog(onDismissRequest = { showSleepTimerDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "مؤقت النوم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    timers.forEach { (minutes, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.playerManager.setSleepTimer(if (minutes == 0) null else minutes)
                                    showSleepTimerDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if ((minutes == 0 && playerState.sleepTimerMinutesRemaining == null) ||
                                (minutes > 0 && playerState.sleepTimerMinutesRemaining != null && playerState.sleepTimerMinutesRemaining!! <= minutes && playerState.sleepTimerMinutesRemaining!! > minutes - 15)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
