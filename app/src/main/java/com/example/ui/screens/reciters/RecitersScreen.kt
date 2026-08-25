package com.example.ui.screens.reciters

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.model.MoshafDto
import com.example.data.model.ReciterDto
import com.example.data.model.SurahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SearchBarField
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.IslamicEmeraldCard
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun RecitersScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val recitersState by viewModel.recitersState.collectAsState()
    val riwayatState by viewModel.riwayatState.collectAsState()
    val suwarState by viewModel.suwarState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val downloadProgressMap by viewModel.downloadProgressMap.collectAsState()

    val searchQuery by viewModel.reciterSearchQuery.collectAsState()
    val selectedRiwayahId by viewModel.selectedRiwayahId.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()

    var activeMoshafIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "قراء القرآن الكريم",
            subtitle = "أصوات عذبة من مختلف دول العالم الإسلامي"
        )

        // Search Bar
        SearchBarField(
            query = searchQuery,
            onQueryChange = { viewModel.reciterSearchQuery.value = it },
            placeholder = "ابحث عن اسم القارئ..."
        )

        // Riwayat Horizontal Filter Chips
        when (val rState = riwayatState) {
            is UiState.Success -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedRiwayahId == null,
                            onClick = {
                                viewModel.selectedRiwayahId.value = null
                                viewModel.fetchReciters(rewaya = null)
                            },
                            label = { Text("جميع الروايات") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    items(rState.data) { riwayah ->
                        FilterChip(
                            selected = selectedRiwayahId == riwayah.id,
                            onClick = {
                                val newId = if (selectedRiwayahId == riwayah.id) null else riwayah.id
                                viewModel.selectedRiwayahId.value = newId
                                viewModel.fetchReciters(rewaya = newId)
                            },
                            label = { Text(riwayah.name ?: "رواية ${riwayah.id}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
            else -> {}
        }

        // Reciters List
        when (val state = recitersState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(8) {
                        SkeletonCardItem(height = 90.dp)
                    }
                }
            }
            is UiState.Success -> {
                val filtered = state.data.filter { reciter ->
                    searchQuery.isBlank() || (reciter.name?.contains(searchQuery, ignoreCase = true) == true)
                }

                if (filtered.isEmpty()) {
                    EmptyStateView(
                        message = "لم يتم العثور على أي قارئ يطابق بحثك.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                    ) {
                        items(filtered, key = { it.id }) { reciter ->
                            val isFavorite = favorites.any { it.id == "RECITER_${reciter.id}" }
                            ReciterCardItem(
                                reciter = reciter,
                                isFavorite = isFavorite,
                                onClick = {
                                    activeMoshafIndex = 0
                                    viewModel.setSelectedReciter(reciter)
                                },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(
                                        FavoriteEntity(
                                            id = "RECITER_${reciter.id}",
                                            type = "RECITER",
                                            targetId = reciter.id,
                                            title = reciter.name ?: "القارئ",
                                            subtitle = "${reciter.moshaf?.size ?: 0} مصاحف متوفرة"
                                        )
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
                    onRetry = { viewModel.fetchReciters(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Reciter Moshaf & Surahs Dialog
    if (selectedReciter != null) {
        val reciter = selectedReciter!!
        val moshafs = reciter.moshaf ?: emptyList()
        val currentMoshaf = moshafs.getOrNull(activeMoshafIndex) ?: moshafs.firstOrNull()
        val allSuwar = (suwarState as? UiState.Success)?.data ?: emptyList()

        Dialog(onDismissRequest = { viewModel.setSelectedReciter(null) }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Reciter Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(IslamicGoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = IslamicEmeraldDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = reciter.name ?: "القارئ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentMoshaf?.name ?: "المصحف"} • ${currentMoshaf?.surahTotal ?: 114} سورة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.setSelectedReciter(null) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    // Moshaf Switcher Tabs if multiple
                    if (moshafs.size > 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(moshafs.indices.toList()) { index ->
                                val m = moshafs[index]
                                FilterChip(
                                    selected = activeMoshafIndex == index,
                                    onClick = { activeMoshafIndex = index },
                                    label = { Text(m.name ?: "مصحف ${index + 1}", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Surahs available for this reciter
                    val availableSuwar = if (currentMoshaf != null) {
                        allSuwar.filter { currentMoshaf.hasSurah(it.id) }
                    } else {
                        allSuwar
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(availableSuwar) { surah ->
                            val isPlaying = playerState.currentTrack?.surahId == surah.id &&
                                    playerState.currentTrack?.reciterId == reciter.id &&
                                    playerState.isPlaying
                            val downloadKey = "${reciter.id}_${surah.id}"
                            val downloadProgress = downloadProgressMap[downloadKey]

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (currentMoshaf != null) {
                                            viewModel.playSurah(
                                                surah = surah,
                                                reciter = reciter,
                                                moshaf = currentMoshaf,
                                                allSuwarList = availableSuwar
                                            )
                                        }
                                    },
                                color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                tonalElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${surah.id}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(28.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = surah.name ?: "سورة ${surah.id}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (surah.isMakkia) "مكية" else "مدنية",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Download button / progress
                                    if (downloadProgress != null) {
                                        CircularProgressIndicator(
                                            progress = { downloadProgress },
                                            modifier = Modifier.size(28.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(
                                            onClick = {
                                                if (currentMoshaf != null) {
                                                    viewModel.downloadSurah(surah, reciter, currentMoshaf)
                                                }
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Download,
                                                contentDescription = "تحميل",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Play icon
                                    IconButton(
                                        onClick = {
                                            if (currentMoshaf != null) {
                                                viewModel.playSurah(
                                                    surah = surah,
                                                    reciter = reciter,
                                                    moshaf = currentMoshaf,
                                                    allSuwarList = availableSuwar
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
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
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReciterCardItem(
    reciter: ReciterDto,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                RoundedCornerShape(18.dp)
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(IslamicGoldPrimary.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )
                    .border(1.dp, IslamicGoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reciter.name ?: "القارئ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${reciter.moshaf?.size ?: 0} مصاحف وروايات متاحة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onFavoriteClick, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "المفضلة",
                    tint = if (isFavorite) IslamicGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
