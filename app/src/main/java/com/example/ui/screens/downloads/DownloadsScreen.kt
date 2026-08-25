package com.example.ui.screens.downloads

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
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IslamicHeader
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun DownloadsScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val downloads by viewModel.downloads.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val totalSizeMb = remember(downloads) {
        val totalBytes = downloads.sumOf { it.fileSize }
        if (totalBytes > 0L) {
            String.format("%.1f MB", totalBytes / (1024.0 * 1024.0))
        } else {
            "0 MB"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "التنزيلات (دون إنترنت)",
            subtitle = "${downloads.size} سور محملة • المساحة المستخدمة: $totalSizeMb",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) },
            trailingContent = {
                if (downloads.isNotEmpty()) {
                    IconButton(onClick = { showDeleteAllDialog = true }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "حذف الكل",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )

        if (downloads.isEmpty()) {
            EmptyStateView(
                message = "لا توجد سور محملة حاليًا.\nيمكنك تحميل السور من قائمة القراء للاستماع إليها دون الحاجة للاتصال بالإنترنت.",
                icon = Icons.Outlined.DownloadDone,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(downloads, key = { it.id }) { item ->
                    val isPlaying = playerState.currentTrack?.id == item.id && playerState.isPlaying

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { viewModel.playDownloadedSurah(item) }
                            .border(
                                1.dp,
                                if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(18.dp)
                            ),
                        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
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
                                val mb = (item.fileSize / (1024.0 * 1024.0))
                                Text(
                                    text = "${item.reciterName} • ${String.format("%.1f MB", mb)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteDownload(item.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "حذف الملف",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            IconButton(
                                onClick = { viewModel.playDownloadedSurah(item) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف" else "تشغيل",
                                    tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("حذف جميع التنزيلات") },
            text = { Text("هل أنت متأكد من رغبتك في حذف جميع التلاوات المحملة لتفريغ المساحة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllDownloads()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، احذف الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
