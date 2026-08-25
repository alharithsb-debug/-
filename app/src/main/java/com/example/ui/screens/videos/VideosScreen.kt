package com.example.ui.screens.videos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VideoGroupDto
import com.example.data.model.VideoItemDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.IslamicEmeraldCard
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun VideosScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val videosState by viewModel.videosState.collectAsState()
    var selectedGroupIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "فيديوهات القرآن الكريم",
            subtitle = "تلاوات مرئية خاشعة ومقاطع تدبرية عالية الجودة",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) }
        )

        when (val state = videosState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(6) { SkeletonCardItem(height = 140.dp) }
                }
            }
            is UiState.Success -> {
                val groups = state.data
                if (groups.isEmpty()) {
                    EmptyStateView(
                        message = "لا تتوفر فيديوهات حاليًا.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val activeGroup = groups.getOrNull(selectedGroupIndex) ?: groups.first()
                    val videoList = activeGroup.videos ?: emptyList()
                    val currentReciterName = activeGroup.reciterName ?: "قارئ القرآن"

                    // Group Selector Chips
                    if (groups.size > 1) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(groups.size) { index ->
                                val grp = groups[index]
                                FilterChip(
                                    selected = selectedGroupIndex == index,
                                    onClick = { selectedGroupIndex = index },
                                    label = { Text(grp.reciterName ?: "مجموعة ${index + 1}", fontSize = 13.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(videoList, key = { index, v -> v.id ?: index }) { _, video ->
                            VideoCardItem(
                                video = video,
                                reciterName = currentReciterName,
                                onClick = {
                                    val videoUrl = video.videoUrl
                                    if (!videoUrl.isNullOrBlank()) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                                        context.startActivity(intent)
                                    }
                                },
                                onShare = {
                                    viewModel.showShareDialog(
                                        title = video.videoTitle ?: "تلاوة قرآنية مرئية",
                                        text = "${video.videoTitle ?: ""}\nبصوت القارئ: $currentReciterName\n\n${video.videoUrl ?: ""}",
                                        subtitle = "فيديوهات القرآن الكريم"
                                    )
                                }
                            )
                        }
                    }
                }
            }
            is UiState.Error -> {
                ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.fetchVideos(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun VideoCardItem(
    video: VideoItemDto,
    reciterName: String,
    onClick: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Thumbnail Frame / Art banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(IslamicEmeraldCard, IslamicEmeraldDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(IslamicGoldPrimary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل الفيديو",
                        tint = IslamicEmeraldDark,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "HD 1080p",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = IslamicGoldPrimary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(IslamicEmeraldDark.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = video.videoTitle ?: "تلاوة خاشعة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "القارئ: $reciterName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
