package com.example.ui.screens.reading

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AyahTimingDto
import com.example.data.model.SurahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyatReadingScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val suwarState by viewModel.suwarState.collectAsState()
    val timingState by viewModel.timingState.collectAsState()
    val timingSurah by viewModel.timingSurah.collectAsState()
    val fontScale by viewModel.readingFontScale.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    var showSurahPicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val currentSuwarList = (suwarState as? UiState.Success)?.data ?: emptyList()
    val currentSurah = currentSuwarList.firstOrNull { it.id == timingSurah }
        ?: SurahDto(id = timingSurah, name = "سورة رقم $timingSurah")

    // Determine current active verse based on audio player position if playing this surah
    val activeAyahIndex = remember(playerState.currentPositionMs, timingState) {
        if (timingState is UiState.Success && playerState.currentTrack?.surahId == timingSurah) {
            val timings = (timingState as UiState.Success<List<AyahTimingDto>>).data
            timings.indexOfFirst {
                val start = it.startTimeMs ?: 0L
                val end = it.endTimeMs ?: Long.MAX_VALUE
                playerState.currentPositionMs in start..end
            }
        } else {
            -1
        }
    }

    // Auto-scroll to active ayah
    LaunchedEffect(activeAyahIndex) {
        if (activeAyahIndex >= 0) {
            coroutineScope.launch {
                listState.animateScrollToItem(activeAyahIndex.coerceAtLeast(0))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "المصحف وتوقيت الآيات",
            subtitle = "${currentSurah.name ?: "سورة رقم $timingSurah"} • مزامنة التلاوة مع الآيات",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) },
            trailingContent = {
                // Surah chooser button
                Button(
                    onClick = { showSurahPicker = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("تغيير السورة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        )

        // Reading Tools Toolbar (Font scale slider & Tafsir / Tadabor quick links)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatSize,
                        contentDescription = "حجم الخط",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.setReadingFontScale(it) },
                        valueRange = 0.9f..1.8f,
                        modifier = Modifier.width(130.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = {
                            viewModel.fetchTafasir(timingSurah)
                            viewModel.navigateTo(ScreenDestination.Tafasir)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("التفسير", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    TextButton(
                        onClick = {
                            viewModel.fetchTadabor(timingSurah)
                            viewModel.navigateTo(ScreenDestination.Tadabor)
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("التدبر", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Bismillah Banner if not At-Tawbah (Surah 9)
        if (timingSurah != 9) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (20 * fontScale).sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }

        when (val state = timingState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(6) {
                        SkeletonCardItem(height = 90.dp)
                    }
                }
            }
            is UiState.Success -> {
                val timings = state.data
                if (timings.isEmpty()) {
                    // Fallback to Surah Verses representation
                    val totalVerses = currentSurah.totalVerses ?: 10
                    val dummyAyahs = (1..totalVerses).map { AyahTimingDto(id = it, ayah = it, startTimeMs = (it - 1) * 4000L, endTimeMs = it * 4000L) }
                    AyatList(
                        timings = dummyAyahs,
                        surah = currentSurah,
                        activeAyahIndex = activeAyahIndex,
                        fontScale = fontScale,
                        listState = listState,
                        onAyahClick = { ayahTiming ->
                            ayahTiming.startTimeMs?.let { ms ->
                                if (playerState.currentTrack?.surahId == currentSurah.id) {
                                    viewModel.playerManager.seekTo(ms)
                                } else {
                                    viewModel.playSurah(surah = currentSurah, allSuwarList = currentSuwarList, startPositionMs = ms)
                                }
                            }
                        },
                        onShareAyah = { ayahNum ->
                            viewModel.showShareDialog(
                                title = "${currentSurah.name} — الآية $ayahNum",
                                text = "﴿ الآية $ayahNum من سورة ${currentSurah.name} ﴾",
                                subtitle = "سورة ${if (currentSurah.isMakkia) "مكية" else "مدنية"}"
                            )
                        },
                        onCopyAyah = { ayahNum ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Ayah", "${currentSurah.name} — الآية $ayahNum")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الآية $ayahNum", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    AyatList(
                        timings = timings,
                        surah = currentSurah,
                        activeAyahIndex = activeAyahIndex,
                        fontScale = fontScale,
                        listState = listState,
                        onAyahClick = { ayahTiming ->
                            ayahTiming.startTimeMs?.let { ms ->
                                if (playerState.currentTrack?.surahId == currentSurah.id) {
                                    viewModel.playerManager.seekTo(ms)
                                } else {
                                    viewModel.playSurah(surah = currentSurah, allSuwarList = currentSuwarList, startPositionMs = ms)
                                }
                            }
                        },
                        onShareAyah = { ayahNum ->
                            viewModel.showShareDialog(
                                title = "${currentSurah.name} — الآية $ayahNum",
                                text = "﴿ الآية $ayahNum من سورة ${currentSurah.name} ﴾",
                                subtitle = "سورة ${if (currentSurah.isMakkia) "مكية" else "مدنية"}"
                            )
                        },
                        onCopyAyah = { ayahNum ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Ayah", "${currentSurah.name} — الآية $ayahNum")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ الآية $ayahNum", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
            is UiState.Error -> {
                ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.fetchAyatTiming(timingSurah) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Surah Chooser Modal Sheet / Dialog
    if (showSurahPicker) {
        AlertDialog(
            onDismissRequest = { showSurahPicker = false },
            title = { Text("اختر سورة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    itemsIndexed(currentSuwarList) { _, surah ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.fetchAyatTiming(surah.id)
                                    showSurahPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            color = if (surah.id == timingSurah) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${surah.id}. ${surah.name ?: "سورة ${surah.id}"}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (surah.id == timingSurah) FontWeight.Bold else FontWeight.Normal,
                                    color = if (surah.id == timingSurah) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (surah.isMakkia) "مكية" else "مدنية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSurahPicker = false }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
fun AyatList(
    timings: List<AyahTimingDto>,
    surah: SurahDto,
    activeAyahIndex: Int,
    fontScale: Float,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAyahClick: (AyahTimingDto) -> Unit,
    onShareAyah: (Int) -> Unit,
    onCopyAyah: (Int) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(timings) { index, timing ->
            val ayahNumber = timing.ayah ?: (index + 1)
            val isActive = activeAyahIndex == index

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAyahClick(timing) }
                    .border(
                        1.5.dp,
                        if (isActive) IslamicGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        RoundedCornerShape(16.dp)
                    ),
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isActive) 6.dp else 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Ayah Number Badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) IslamicGoldPrimary else MaterialTheme.colorScheme.surface
                                )
                                .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$ayahNumber",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) IslamicEmeraldDark else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Actions (Play, Copy, Share)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onCopyAyah(ayahNumber) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onShareAyah(ayahNumber) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Share, contentDescription = "مشاركة", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onAyahClick(timing) }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.VolumeUp else Icons.Default.PlayCircle,
                                    contentDescription = "تشغيل من هذه الآية",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ayah representation
                    Text(
                        text = "﴿ الآية رقم $ayahNumber من سورة ${surah.name ?: ""} ﴾",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = (17 * fontScale).sp,
                            lineHeight = (26 * fontScale).sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (timing.startTimeMs != null && timing.startTimeMs > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⏱️ التوقيت: ${timing.startTimeMs / 1000} ثانية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
