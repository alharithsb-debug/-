package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RadioDto
import com.example.data.model.ReciterDto
import com.example.data.model.SurahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SearchBarField
import com.example.ui.screens.home.SurahCardItem
import com.example.ui.screens.reciters.ReciterCardItem
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun SearchScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.globalSearchQuery.collectAsState()
    val suwarState by viewModel.suwarState.collectAsState()
    val recitersState by viewModel.recitersState.collectAsState()
    val radiosState by viewModel.radiosState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val suwarList = (suwarState as? UiState.Success)?.data ?: emptyList()
    val recitersList = (recitersState as? UiState.Success)?.data ?: emptyList()
    val radiosList = (radiosState as? UiState.Success)?.data ?: emptyList()

    val matchedSuwar = remember(searchQuery, suwarList) {
        if (searchQuery.isBlank()) emptyList()
        else suwarList.filter {
            it.name?.contains(searchQuery, ignoreCase = true) == true ||
                    it.id.toString() == searchQuery.trim()
        }
    }

    val matchedReciters = remember(searchQuery, recitersList) {
        if (searchQuery.isBlank()) emptyList()
        else recitersList.filter {
            it.name?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val matchedRadios = remember(searchQuery, radiosList) {
        if (searchQuery.isBlank()) emptyList()
        else radiosList.filter {
            it.name?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val totalMatches = matchedSuwar.size + matchedReciters.size + matchedRadios.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "البحث الشامل",
            subtitle = if (searchQuery.isNotBlank()) "تم العثور على $totalMatches نتيجة" else "ابحث عن سور، قراء، أو إذاعات",
            onBackClick = { viewModel.navigateTo(ScreenDestination.Home) }
        )

        SearchBarField(
            query = searchQuery,
            onQueryChange = { viewModel.globalSearchQuery.value = it },
            placeholder = "اكتب اسم السورة، القارئ، أو الإذاعة..."
        )

        if (searchQuery.isBlank()) {
            EmptyStateView(
                message = "اكتب كلمة البحث للبحث الفوري في كافة سور القرآن الكريم، القراء، وإذاعات البث المباشر.",
                icon = Icons.Default.Search,
                modifier = Modifier.fillMaxSize()
            )
        } else if (totalMatches == 0) {
            EmptyStateView(
                message = "لم يتم العثور على أي نتائج تطابق \"$searchQuery\".\nجرّب كتابة كلمة أخرى.",
                icon = Icons.Default.SearchOff,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Matched Suwar
                if (matchedSuwar.isNotEmpty()) {
                    item {
                        Text(
                            text = "📖 السور القرآنية (${matchedSuwar.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(matchedSuwar) { surah ->
                        SurahCardItem(
                            surah = surah,
                            isPlaying = playerState.currentTrack?.surahId == surah.id && playerState.isPlaying,
                            onPlayClick = { viewModel.playSurah(surah = surah, allSuwarList = suwarList) },
                            onReadClick = {
                                viewModel.fetchAyatTiming(surah.id)
                                viewModel.navigateTo(ScreenDestination.ReadingAndTiming)
                            },
                            onFavoriteClick = {}
                        )
                    }
                }

                // Matched Reciters
                if (matchedReciters.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🎙️ القراء (${matchedReciters.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(matchedReciters.take(15)) { reciter ->
                        val isFavorite = favorites.any { it.id == "RECITER_${reciter.id}" }
                        ReciterCardItem(
                            reciter = reciter,
                            isFavorite = isFavorite,
                            onClick = {
                                viewModel.setSelectedReciter(reciter)
                                viewModel.navigateTo(ScreenDestination.Reciters)
                            },
                            onFavoriteClick = {}
                        )
                    }
                }

                // Matched Radios
                if (matchedRadios.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "📻 الإذاعات (${matchedRadios.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    items(matchedRadios) { radio ->
                        val isPlaying = playerState.currentTrack?.id == "radio_${radio.id}" && playerState.isPlaying
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.playRadio(radio) },
                            color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Radio,
                                        contentDescription = null,
                                        tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = radio.name ?: "إذاعة القرآن",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "بث إذاعي مباشر",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.playRadio(radio) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.onPrimary,
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
}
