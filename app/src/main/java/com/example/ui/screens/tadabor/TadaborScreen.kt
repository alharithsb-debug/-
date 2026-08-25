package com.example.ui.screens.tadabor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SurahDto
import com.example.data.model.TadaborAyahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun TadaborScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val suwarState by viewModel.suwarState.collectAsState()
    val tadaborState by viewModel.tadaborState.collectAsState()
    val tadaborSurah by viewModel.tadaborSurah.collectAsState()

    var showSurahDialog by remember { mutableStateOf(false) }
    val suwarList = (suwarState as? UiState.Success)?.data ?: emptyList()
    val currentSurah = suwarList.firstOrNull { it.id == tadaborSurah }
        ?: SurahDto(id = tadaborSurah, name = "سورة رقم $tadaborSurah")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "تدبر القرآن الكريم",
            subtitle = "${currentSurah.name ?: "سورة رقم $tadaborSurah"} • وقفات إيمانية وتأملات",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) },
            trailingContent = {
                Button(
                    onClick = { showSurahDialog = true },
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

        when (val state = tadaborState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(6) { SkeletonCardItem(height = 130.dp) }
                }
            }
            is UiState.Success -> {
                val list = state.data
                if (list.isEmpty()) {
                    EmptyStateView(
                        message = "لا تتوفر وقفات تدبر لهذه السورة حاليًا.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(list, key = { index, item -> item.id ?: index }) { _, item ->
                            TadaborCardItem(
                                item = item,
                                surahName = currentSurah.name ?: "السورة",
                                onShare = {
                                    viewModel.showShareDialog(
                                        title = "تدبر: آية ${item.ayaId ?: 1} — ${currentSurah.name}",
                                        text = "﴿ الآية ${item.ayaId ?: 1} ﴾\n\nالوقفة والتدبر:\n${item.text ?: ""}",
                                        subtitle = "تدبر القرآن الكريم"
                                    )
                                },
                                onWatchVideo = { videoUrl ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.fetchTadabor(tadaborSurah) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showSurahDialog) {
        AlertDialog(
            onDismissRequest = { showSurahDialog = false },
            title = { Text("اختر سورة للتدبر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(suwarList) { surah ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.fetchTadabor(surah.id)
                                    showSurahDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            color = if (surah.id == tadaborSurah) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(
                                text = "${surah.id}. ${surah.name ?: "سورة ${surah.id}"}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (surah.id == tadaborSurah) FontWeight.Bold else FontWeight.Normal,
                                color = if (surah.id == tadaborSurah) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSurahDialog = false }) { Text("إغلاق") }
            }
        )
    }
}

@Composable
fun TadaborCardItem(
    item: TadaborAyahDto,
    surahName: String,
    onShare: () -> Unit,
    onWatchVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "وقفة تدبر • $surahName (الآية ${item.ayaId ?: 1})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            if (!item.text.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!item.videoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onWatchVideo(item.videoUrl) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.PlayCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مشاهدة المقطع المرئي", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
