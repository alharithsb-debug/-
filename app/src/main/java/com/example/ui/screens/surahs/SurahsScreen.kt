package com.example.ui.screens.surahs

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.FavoriteEntity
import com.example.data.model.SurahDto
import com.example.ui.components.*
import com.example.ui.screens.home.SurahCardItem
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun SurahsScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val suwarState by viewModel.suwarState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val searchQuery by viewModel.surahSearchQuery.collectAsState()
    val typeFilter by viewModel.surahTypeFilter.collectAsState() // null = all, 1 = makkia, 0 = madania
    val sortAlphabetical by viewModel.surahSortAlphabetical.collectAsState()

    var selectedSurahDetails by remember { mutableStateOf<SurahDto?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "سور القرآن الكريم",
            subtitle = "114 سورة كاملة مع خيارات الاستماع والقراءة",
            trailingContent = {
                IconButton(
                    onClick = { viewModel.surahSortAlphabetical.value = !sortAlphabetical },
                    modifier = Modifier.testTag("toggle_sort_button")
                ) {
                    Icon(
                        imageVector = if (sortAlphabetical) Icons.Default.SortByAlpha else Icons.Default.FormatListNumbered,
                        contentDescription = "ترتيب",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )

        // Search bar
        SearchBarField(
            query = searchQuery,
            onQueryChange = { viewModel.surahSearchQuery.value = it },
            placeholder = "ابحث عن اسم السورة أو رقمها..."
        )

        // Makki / Madani Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = typeFilter == null,
                onClick = { viewModel.surahTypeFilter.value = null },
                label = { Text("الكل (114)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            FilterChip(
                selected = typeFilter == 1,
                onClick = { viewModel.surahTypeFilter.value = if (typeFilter == 1) null else 1 },
                label = { Text("مكية 🕋") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            FilterChip(
                selected = typeFilter == 0,
                onClick = { viewModel.surahTypeFilter.value = if (typeFilter == 0) null else 0 },
                label = { Text("مدنية 🕌") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }

        when (val state = suwarState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(10) {
                        SkeletonCardItem()
                    }
                }
            }
            is UiState.Success -> {
                val filteredList = state.data.filter { surah ->
                    val matchesQuery = searchQuery.isBlank() ||
                            (surah.name?.contains(searchQuery, ignoreCase = true) == true) ||
                            surah.id.toString() == searchQuery.trim()

                    val matchesType = when (typeFilter) {
                        1 -> surah.isMakkia
                        0 -> !surah.isMakkia
                        else -> true
                    }
                    matchesQuery && matchesType
                }.let { list ->
                    if (sortAlphabetical) {
                        list.sortedBy { it.name }
                    } else {
                        list.sortedBy { it.id }
                    }
                }

                if (filteredList.isEmpty()) {
                    EmptyStateView(
                        message = "لم يتم العثور على أي سورة تطابق بحثك.",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                    ) {
                        items(filteredList, key = { it.id }) { surah ->
                            val isFavorite = favorites.any { it.id == "SURAH_${surah.id}" }
                            SurahListItem(
                                surah = surah,
                                isPlaying = playerState.currentTrack?.surahId == surah.id && playerState.isPlaying,
                                isFavorite = isFavorite,
                                onPlayClick = {
                                    viewModel.playSurah(surah = surah, allSuwarList = state.data)
                                },
                                onReadClick = {
                                    viewModel.fetchAyatTiming(surah.id)
                                    viewModel.navigateTo(ScreenDestination.ReadingAndTiming)
                                },
                                onInfoClick = {
                                    selectedSurahDetails = surah
                                },
                                onFavoriteClick = {
                                    viewModel.toggleFavorite(
                                        FavoriteEntity(
                                            id = "SURAH_${surah.id}",
                                            type = "SURAH",
                                            targetId = surah.id,
                                            title = surah.name ?: "سورة ${surah.id}",
                                            subtitle = if (surah.isMakkia) "مكية" else "مدنية"
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
                    onRetry = { viewModel.fetchSuwar(forceRefresh = true) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Surah Details Dialog
    if (selectedSurahDetails != null) {
        val surah = selectedSurahDetails!!
        Dialog(onDismissRequest = { selectedSurahDetails = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(IslamicGoldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${surah.id}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmeraldDark
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = surah.name ?: "سورة ${surah.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = if (surah.isMakkia) "سورة مكية" else "سورة مدنية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("عدد الآيات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${surah.totalVerses ?: "—"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("صفحة البداية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${surah.startPage ?: surah.page ?: "—"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("صفحة النهاية", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${surah.endPage ?: "—"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedSurahDetails = null
                                viewModel.fetchTadabor(surah.id)
                                viewModel.navigateTo(ScreenDestination.Tadabor)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تدبر السورة")
                        }

                        Button(
                            onClick = {
                                selectedSurahDetails = null
                                viewModel.fetchTafasir(surah.id)
                                viewModel.navigateTo(ScreenDestination.Tafasir)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تفسير السورة", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahListItem(
    surah: SurahDto,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onReadClick: () -> Unit,
    onInfoClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onPlayClick)
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
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.surface)
                    .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.id}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.name ?: "سورة رقم ${surah.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (surah.isMakkia) "مكية" else "مدنية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (surah.totalVerses != null) {
                        Text(
                            text = "• ${surah.totalVerses} آية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Info button
            IconButton(onClick = onInfoClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "تفاصيل",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Favorite button
            IconButton(onClick = onFavoriteClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = "المفضلة",
                    tint = if (isFavorite) IslamicGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Read button
            IconButton(onClick = onReadClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "قراءة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Play / Pause button
            IconButton(
                onClick = onPlayClick,
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
