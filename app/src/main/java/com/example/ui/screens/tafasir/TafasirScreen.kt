package com.example.ui.screens.tafasir

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
import androidx.compose.material.icons.outlined.OpenInNew
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
import com.example.data.model.TafsirSurahDto
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.IslamicHeader
import com.example.ui.components.SkeletonCardItem
import com.example.ui.theme.IslamicEmeraldDark
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.ui.viewmodel.UiState

@Composable
fun TafasirScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val suwarState by viewModel.suwarState.collectAsState()
    val tafasirState by viewModel.tafasirState.collectAsState()
    val selectedTafsirSurah by viewModel.selectedTafsirSurah.collectAsState()

    var showSurahDialog by remember { mutableStateOf(false) }
    val suwarList = (suwarState as? UiState.Success)?.data ?: emptyList()
    val currentSurah = suwarList.firstOrNull { it.id == selectedTafsirSurah }
        ?: SurahDto(id = selectedTafsirSurah, name = "سورة رقم $selectedTafsirSurah")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        IslamicHeader(
            title = "تفاسير القرآن الكريم",
            subtitle = "${currentSurah.name ?: "سورة رقم $selectedTafsirSurah"} • التفسير الميسر",
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

        when (val state = tafasirState) {
            is UiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
                ) {
                    items(6) { SkeletonCardItem(height = 110.dp) }
                }
            }
            is UiState.Success -> {
                val soar = state.data.soar ?: emptyList()
                val tafsirName = state.data.name ?: "التفسير الميسر"

                if (soar.isEmpty()) {
                    // Fallback presentation for the surah tafsir
                    TafsirDirectCard(
                        surah = currentSurah,
                        tafsirTitle = tafsirName,
                        onShare = {
                            viewModel.showShareDialog(
                                title = "تفسير ${currentSurah.name}",
                                text = "تفسير وبيان معاني ${currentSurah.name} • $tafsirName",
                                subtitle = "تفاسير القرآن الكريم"
                            )
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(soar, key = { index, s -> s.id ?: index }) { _, item ->
                            TafsirItemCard(
                                item = item,
                                surahName = currentSurah.name ?: "السورة",
                                tafsirTitle = tafsirName,
                                onOpenUrl = { url ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                onShare = {
                                    viewModel.showShareDialog(
                                        title = "${item.name ?: currentSurah.name} — $tafsirName",
                                        text = "تفسير ${item.name ?: currentSurah.name}\n${item.url ?: ""}",
                                        subtitle = "تفاسير القرآن الكريم"
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
                    onRetry = { viewModel.fetchTafasir(selectedTafsirSurah) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showSurahDialog) {
        AlertDialog(
            onDismissRequest = { showSurahDialog = false },
            title = { Text("اختر سورة للتفسير", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
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
                                    viewModel.fetchTafasir(surah.id)
                                    showSurahDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            color = if (surah.id == selectedTafsirSurah) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        ) {
                            Text(
                                text = "${surah.id}. ${surah.name ?: "سورة ${surah.id}"}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (surah.id == selectedTafsirSurah) FontWeight.Bold else FontWeight.Normal,
                                color = if (surah.id == selectedTafsirSurah) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
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
fun TafsirItemCard(
    item: TafsirSurahDto,
    surahName: String,
    tafsirTitle: String,
    onOpenUrl: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = IslamicGoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = item.name ?: surahName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "$tafsirTitle • تبيان وتفسير مقاصد ومعاني سورة $surahName",
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!item.url.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onOpenUrl(item.url) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فتح التفسير الكامل", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TafsirDirectCard(
    surah: SurahDto,
    tafsirTitle: String,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, IslamicGoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تفسير ${surah.name ?: "السورة"}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "$tafsirTitle • تفسير سورة ${if (surah.isMakkia) "مكية" else "مدنية"}، عدد آياتها ${surah.totalVerses ?: "—"} آية.",
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
