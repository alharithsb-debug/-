package com.example.ui.screens.home

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FavoriteEntity
import com.example.data.model.ReciterDto
import com.example.data.model.SurahDto
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

data class DashboardCategory(
    val title: String,
    val icon: ImageVector,
    val destination: ScreenDestination,
    val badge: String? = null
)

@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val suwarState by viewModel.suwarState.collectAsState()
    val recitersState by viewModel.recitersState.collectAsState()
    val lastListened by viewModel.lastListened.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    val categories = listOf(
        DashboardCategory("استماع القرآن", Icons.Default.Headphones, ScreenDestination.Surahs),
        DashboardCategory("المصحف وتوقيت الآيات", Icons.Default.MenuBook, ScreenDestination.ReadingAndTiming),
        DashboardCategory("القراء", Icons.Default.Mic, ScreenDestination.Reciters),
        DashboardCategory("التفاسير", Icons.Default.AutoStories, ScreenDestination.Tafasir),
        DashboardCategory("تدبر القرآن", Icons.Default.Lightbulb, ScreenDestination.Tadabor),
        DashboardCategory("فيديوهات القرآن", Icons.Default.Tv, ScreenDestination.Videos),
        DashboardCategory("إذاعات القرآن", Icons.Default.Radio, ScreenDestination.Radios),
        DashboardCategory("المفضلة", Icons.Default.Star, ScreenDestination.Favorites),
        DashboardCategory("التنزيلات", Icons.Default.Download, ScreenDestination.Downloads),
        DashboardCategory("سجل الاستماع", Icons.Default.History, ScreenDestination.History),
        DashboardCategory("البحث الشامل", Icons.Default.Search, ScreenDestination.Search)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Islamic Welcome Header Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        listOf(IslamicGoldPrimary.copy(alpha = 0.5f), IslamicEmeraldCardLight)
                    )
                )
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "السلام عليكم ورحمة الله وبركاته",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "استمع إلى القرآن الكريم",
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 24.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تلاوات عطرة، تدبر، تفسير، وإذاعات مباشرة بأصوات كبار القراء",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // "متابعة الاستماع" Continue Listening Card
        if (lastListened != null) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(
                        text = "🎧 تابع من حيث توقفت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.resumeLastListened() }
                            .border(
                                1.dp,
                                IslamicGoldPrimary.copy(alpha = 0.4f),
                                RoundedCornerShape(20.dp)
                            ),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                listOf(IslamicGoldPrimary.copy(alpha = 0.4f), Color.Transparent)
                                            )
                                        )
                                        .border(1.dp, IslamicGoldPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = IslamicGoldPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lastListened!!.surahName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${lastListened!!.reciterName} — ${lastListened!!.moshafName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { viewModel.resumeLastListened() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("resume_listening_button")
                                ) {
                                    Text("متابعة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { lastListened!!.progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
        }

        // Quick Categories Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "أقسام التطبيق",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 2-column or 3-column category grid cards
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunked = categories.chunked(3)
                    chunked.forEach { rowCategories ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowCategories.forEach { category ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { viewModel.navigateTo(category.destination) }
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                            RoundedCornerShape(18.dp)
                                        ),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = category.title,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = category.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            // Fill remaining space if chunk has fewer than 3
                            repeat(3 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Featured Reciters Carousel
        item {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎙️ نخبة من كبار القراء",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { viewModel.navigateTo(ScreenDestination.Reciters) }) {
                        Text("عرض الكل", color = MaterialTheme.colorScheme.primary)
                    }
                }

                when (val state = recitersState) {
                    is UiState.Loading -> {
                        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                            repeat(3) {
                                SkeletonCardItem(height = 90.dp, modifier = Modifier.width(160.dp))
                            }
                        }
                    }
                    is UiState.Success -> {
                        val featured = state.data.take(12)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featured) { reciter ->
                                Surface(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(130.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            viewModel.setSelectedReciter(reciter)
                                            viewModel.navigateTo(ScreenDestination.Reciters)
                                        }
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                            RoundedCornerShape(20.dp)
                                        ),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 4.dp
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
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
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = reciter.name ?: "القارئ",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        // Silent fallback or mini notice
                    }
                }
            }
        }

        // Popular Surahs Quick List
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📖 تلاوات مختارة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { viewModel.navigateTo(ScreenDestination.Surahs) }) {
                        Text("جميع السور (114)", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        when (val state = suwarState) {
            is UiState.Loading -> {
                items(5) {
                    SkeletonCardItem()
                }
            }
            is UiState.Success -> {
                val popularSurahIds = listOf(1, 2, 18, 36, 55, 56, 67)
                val popularSuwar = state.data.filter { popularSurahIds.contains(it.id) }
                items(popularSuwar) { surah ->
                    SurahCardItem(
                        surah = surah,
                        isPlaying = playerState.currentTrack?.surahId == surah.id && playerState.isPlaying,
                        onPlayClick = {
                            viewModel.playSurah(surah = surah, allSuwarList = state.data)
                        },
                        onReadClick = {
                            viewModel.fetchAyatTiming(surah.id)
                            viewModel.navigateTo(ScreenDestination.ReadingAndTiming)
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
            is UiState.Error -> {
                item {
                    ErrorRetryView(
                        message = state.message,
                        onRetry = { viewModel.fetchSuwar() }
                    )
                }
            }
        }
    }
}

@Composable
fun SurahCardItem(
    surah: SurahDto,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onReadClick: () -> Unit,
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Badge with Islamic diamond outline
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        IslamicGoldPrimary.copy(alpha = 0.6f),
                        CircleShape
                    ),
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
                    if (surah.totalVerses != null && surah.totalVerses > 0) {
                        Text(
                            text = "• ${surah.totalVerses} آية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Read Button
            IconButton(
                onClick = onReadClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "قراءة وتفسير",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play / Pause Icon Button
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) IslamicGoldPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                    .testTag("play_surah_${surah.id}")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                    tint = if (isPlaying) IslamicEmeraldDark else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
