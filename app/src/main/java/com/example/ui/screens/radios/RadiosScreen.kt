package com.example.ui.screens.radios

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
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FavoriteEntity
import com.example.data.model.RadioDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SearchBarField
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun RadiosScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val radiosState by viewModel.radiosState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.radioSearchQuery.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "إذاعات القرآن الكريم",
            subtitle = "بث مباشر على مدار الساعة بأصوات أشهر القراء",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) }
        )

        SearchBarField(
            query = searchQuery,
            onQueryChange = { viewModel.radioSearchQuery.value = it },
            placeholder = "ابحث عن اسم الإذاعة أو القارئ..."
        )

        when (val state = radiosState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(8) { SkeletonCardItem(height = 85.dp) }
                }
            }
            is UiState.Success -> {
                val filtered = state.data.filter { radio ->
                    searchQuery.isBlank() || (radio.name?.contains(searchQuery, ignoreCase = true) == true)
                }

                if (filtered.isEmpty()) {
                    EmptyStateView(
                        message = "لم يتم العثور على أي إذاعة تطابق بحثك.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered, key = { it.id }) { radio ->
                            val isCurrentRadio = playerState.currentTrack?.id == "radio_${radio.id}"
                            val isPlaying = isCurrentRadio && playerState.isPlaying
                            val isFavorite = favorites.any { it.id == "RADIO_${radio.id}" || it.id == "radio_${radio.id}" }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { viewModel.playRadio(radio) }
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
                                            imageVector = Icons.Default.Radio,
                                            contentDescription = null,
                                            tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = radio.name ?: "إذاعة القرآن الكريم",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isPlaying) "🔴 بث مباشر نشط الآن" else "بث مباشر 24/7",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.toggleFavorite(
                                                FavoriteEntity(
                                                    id = "RADIO_${radio.id}",
                                                    type = "RADIO",
                                                    targetId = radio.id,
                                                    title = radio.name ?: "إذاعة القرآن",
                                                    subtitle = "بث مباشر"
                                                )
                                            )
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "المفضلة",
                                            tint = if (isFavorite) IslamicGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { viewModel.playRadio(radio) },
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
            is UiState.Error -> {
                ErrorRetryView(
                    message = state.message,
                    onRetry = { viewModel.fetchRadios(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
