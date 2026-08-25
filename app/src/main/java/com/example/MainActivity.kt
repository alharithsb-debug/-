package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MiniPlayer
import com.example.ui.components.ShareCardDialog
import com.example.ui.screens.downloads.DownloadsScreen
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.history.HistoryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.player.NowPlayingScreen
import com.example.ui.screens.radios.RadiosScreen
import com.example.ui.screens.reading.AyatReadingScreen
import com.example.ui.screens.reciters.RecitersScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.surahs.SurahsScreen
import com.example.ui.screens.tadabor.TadaborScreen
import com.example.ui.screens.tafasir.TafasirScreen
import com.example.ui.screens.videos.VideosScreen
import com.example.ui.theme.IslamicEmeraldCardLight
import com.example.ui.theme.IslamicGoldPrimary
import com.example.ui.theme.QuranAppTheme
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.ScreenDestination

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val destination: ScreenDestination,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val fullPlayerVisible by viewModel.fullPlayerVisible.collectAsState()
            val playerState by viewModel.playerState.collectAsState()
            val shareDialogData by viewModel.shareDialogData.collectAsState()

            QuranAppTheme(darkTheme = isDarkMode) {
                // Enforce RTL for Arabic-first experience
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (currentScreen is ScreenDestination.Splash) {
                            SplashScreen(
                                onSplashFinished = {
                                    viewModel.navigateTo(ScreenDestination.Home)
                                }
                            )
                        } else {
                            MainAppScaffold(
                                viewModel = viewModel,
                                currentScreen = currentScreen,
                                fullPlayerVisible = fullPlayerVisible,
                                onBackPress = {
                                    if (fullPlayerVisible) {
                                        viewModel.setFullPlayerVisible(false)
                                    } else if (currentScreen != ScreenDestination.Home) {
                                        viewModel.navigateTo(ScreenDestination.Home)
                                    }
                                }
                            )
                        }

                        // Share Card Dialog
                        shareDialogData?.let { data ->
                            ShareCardDialog(
                                shareData = data,
                                onDismiss = { viewModel.dismissShareDialog() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: QuranViewModel,
    currentScreen: ScreenDestination,
    fullPlayerVisible: Boolean,
    onBackPress: () -> Unit
) {
    val playerState by viewModel.playerState.collectAsState()

    BackHandler(enabled = fullPlayerVisible || currentScreen != ScreenDestination.Home) {
        onBackPress()
    }

    val navItems = listOf(
        NavItem("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, ScreenDestination.Home, "nav_home"),
        NavItem("السور", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, ScreenDestination.Surahs, "nav_surahs"),
        NavItem("القراء", Icons.Filled.Mic, Icons.Outlined.Mic, ScreenDestination.Reciters, "nav_reciters"),
        NavItem("الإذاعات", Icons.Filled.Radio, Icons.Outlined.Radio, ScreenDestination.Radios, "nav_radios"),
        NavItem("المفضلة", Icons.Filled.Star, Icons.Outlined.StarBorder, ScreenDestination.Favorites, "nav_favorites"),
        NavItem("الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings, ScreenDestination.Settings, "nav_settings")
    )

    val isTopLevelNav = navItems.any { it.destination::class == currentScreen::class }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Content
        Scaffold(
            bottomBar = {
                if (isTopLevelNav) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    ) {
                        navItems.forEach { item ->
                            val isSelected = item.destination::class == currentScreen::class
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.navigateTo(item.destination) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isTopLevelNav) innerPadding.calculateBottomPadding() else 0.dp)
            ) {
                Crossfade(
                    targetState = currentScreen,
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        is ScreenDestination.Home -> HomeScreen(viewModel = viewModel)
                        is ScreenDestination.Surahs -> SurahsScreen(viewModel = viewModel)
                        is ScreenDestination.Reciters -> RecitersScreen(viewModel = viewModel)
                        is ScreenDestination.ReadingAndTiming -> AyatReadingScreen(viewModel = viewModel)
                        is ScreenDestination.Tafasir -> TafasirScreen(viewModel = viewModel)
                        is ScreenDestination.Tadabor -> TadaborScreen(viewModel = viewModel)
                        is ScreenDestination.Videos -> VideosScreen(viewModel = viewModel)
                        is ScreenDestination.Radios -> RadiosScreen(viewModel = viewModel)
                        is ScreenDestination.Favorites -> FavoritesScreen(viewModel = viewModel)
                        is ScreenDestination.Downloads -> DownloadsScreen(viewModel = viewModel)
                        is ScreenDestination.History -> HistoryScreen(viewModel = viewModel)
                        is ScreenDestination.Settings -> SettingsScreen(viewModel = viewModel)
                        is ScreenDestination.Search -> SearchScreen(viewModel = viewModel)
                        is ScreenDestination.Splash -> {}
                    }
                }
            }
        }

        // Floating Mini Player docked above Bottom Bar
        if (playerState.currentTrack != null && !fullPlayerVisible) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = if (isTopLevelNav) 80.dp else 16.dp
                    )
            ) {
                MiniPlayer(
                    playerState = playerState,
                    onPlayPause = { viewModel.playerManager.togglePlayPause() },
                    onExpand = { viewModel.setFullPlayerVisible(true) },
                    onClose = { viewModel.playerManager.stop() }
                )
            }
        }

        // Full Screen Now Playing Layer Animated Transition
        AnimatedVisibility(
            visible = fullPlayerVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                viewModel = viewModel,
                onDismiss = { viewModel.setFullPlayerVisible(false) }
            )
        }
    }
}
