package com.example.ui.screens.favorites

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.FavoriteEntity
import com.example.data.model.SurahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IslamicHeader
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun FavoritesScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val suwarState by viewModel.suwarState.collectAsState()
    val recitersState by viewModel.recitersState.collectAsState()
    val radiosState by viewModel.radiosState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    var selectedTab by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "قائمة المفضلة",
            subtitle = "${favorites.size} عناصر محفوظة في المفضلة",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) }
        )

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTab == "ALL",
                onClick = { selectedTab = "ALL" },
                label = { Text("الكل (${favorites.size})") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = selectedTab == "SURAH",
                onClick = { selectedTab = "SURAH" },
                label = { Text("السور") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = selectedTab == "RECITER",
                onClick = { selectedTab = "RECITER" },
                label = { Text("القراء") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
            FilterChip(
                selected = selectedTab == "RADIO",
                onClick = { selectedTab = "RADIO" },
                label = { Text("الإذاعات") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        val filteredFavorites = if (selectedTab == "ALL") {
            favorites
        } else {
            favorites.filter { it.type == selectedTab }
        }

        if (filteredFavorites.isEmpty()) {
            EmptyStateView(
                message = "لم تقم بإضافة أي عنصر إلى المفضلة بعد.\nاضغط على أيقونة النجمة ⭐ بجانب السورة أو القارئ أو الإذاعة لحفظها.",
                icon = Icons.Default.StarBorder,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFavorites, key = { it.id }) { fav ->
                    val isPlaying = when (fav.type) {
                        "SURAH" -> playerState.currentTrack?.surahId == fav.targetId && playerState.isPlaying
                        "RADIO" -> playerState.currentTrack?.id == "radio_${fav.targetId}" && playerState.isPlaying
                        else -> false
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                when (fav.type) {
                                    "SURAH" -> {
                                        val suwar = (suwarState as? UiState.Success)?.data ?: emptyList()
                                        val s = suwar.firstOrNull { it.id == fav.targetId }
                                            ?: SurahDto(id = fav.targetId, name = fav.title)
                                        viewModel.playSurah(surah = s, allSuwarList = suwar)
                                    }
                                    "RECITER" -> {
                                        val reciters = (recitersState as? UiState.Success)?.data ?: emptyList()
                                        val r = reciters.firstOrNull { it.id == fav.targetId }
                                        if (r != null) {
                                            viewModel.setSelectedReciter(r)
                                            viewModel.navigateTo(ScreenDestination.Reciters)
                                        }
                                    }
                                    "RADIO" -> {
                                        val radios = (radiosState as? UiState.Success)?.data ?: emptyList()
                                        val radio = radios.firstOrNull { it.id == fav.targetId }
                                        if (radio != null) {
                                            viewModel.playRadio(radio)
                                        }
                                    }
                                }
                            }
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
                                    imageVector = when (fav.type) {
                                        "SURAH" -> Icons.Default.MenuBook
                                        "RECITER" -> Icons.Default.Mic
                                        "RADIO" -> Icons.Default.Radio
                                        else -> Icons.Default.Star
                                    },
                                    contentDescription = null,
                                    tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fav.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = fav.subtitle ?: when (fav.type) {
                                        "SURAH" -> "سورة قرآنية"
                                        "RECITER" -> "قارئ قرآن"
                                        "RADIO" -> "إذاعة بث مباشر"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.removeFavorite(fav.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "حذف من المفضلة",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
