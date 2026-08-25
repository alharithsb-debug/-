package com.example.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.HistoryEntity
import com.example.data.model.SurahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IslamicHeader
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.history.collectAsState()
    val suwarState by viewModel.suwarState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "سجل الاستماع",
            subtitle = "${history.size} تلاوة مستمع إليها مؤخرًا",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) },
            trailingContent = {
                if (history.isNotEmpty()) {
                    IconButton(onClick = { showClearHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "مسح السجل",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        if (history.isEmpty()) {
            EmptyStateView(
                message = "سجل الاستماع فارغ حاليًا.\nستظهر هنا التلاوات التي تستمع إليها مع حفظ موضع التوقف تلقائيًا.",
                icon = Icons.Outlined.History,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { "${it.reciterId}_${it.surahId}_${it.timestamp}" }) { item ->
                    val isPlaying = playerState.currentTrack?.surahId == item.surahId &&
                            playerState.currentTrack?.reciterId == item.reciterId &&
                            playerState.isPlaying

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                val suwar = (suwarState as? UiState.Success)?.data ?: emptyList()
                                val s = suwar.firstOrNull { it.id == item.surahId }
                                    ?: SurahDto(id = item.surahId, name = item.surahName)
                                viewModel.playSurah(surah = s, allSuwarList = suwar, startPositionMs = item.positionMs)
                            }
                            .border(
                                1.dp,
                                if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(18.dp)
                            ),
                        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.surface)
                                        .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.surahId}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.surahName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${item.reciterName} • ${formatDate(item.timestamp)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteHistoryItem(item.surahId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "حذف من السجل",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = {
                                        val suwar = (suwarState as? UiState.Success)?.data ?: emptyList()
                                        val s = suwar.firstOrNull { it.id == item.surahId }
                                            ?: SurahDto(id = item.surahId, name = item.surahName)
                                        viewModel.playSurah(surah = s, allSuwarList = suwar, startPositionMs = item.positionMs)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                        tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { item.progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("مسح سجل الاستماع") },
            text = { Text("هل تريد بالتأكيد حذف كل السجلات؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("مسح الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
