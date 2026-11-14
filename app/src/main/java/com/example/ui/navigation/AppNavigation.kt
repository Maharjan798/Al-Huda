package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.AzkarToolsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayersScreen
import com.example.ui.screens.QiblaCompassScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SurahDetailScreen
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.components.GlobalSearchOverlay
import com.example.ui.screens.components.MiniAudioPlayer
import com.example.ui.theme.EmeraldPillBg
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NaturalBackground
import com.example.ui.viewmodels.AudioPlayerViewModel
import com.example.ui.viewmodels.AzkarViewModel
import com.example.ui.viewmodels.HadithViewModel
import com.example.ui.viewmodels.HistoryViewModel
import com.example.ui.viewmodels.PrayerViewModel
import com.example.ui.viewmodels.QiblaViewModel
import com.example.ui.viewmodels.QuranViewModel

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Prayers : BottomNavItem("prayers", "Prayers", Icons.Default.Schedule)
    object Quran : BottomNavItem("quran", "Quran", Icons.Default.MenuBook)
    object History : BottomNavItem("history", "History", Icons.Default.History)
    object Azkar : BottomNavItem("azkar", "Azkar", Icons.Default.AutoAwesome)
}

@Composable
fun AppNavigation(
    prayerViewModel: PrayerViewModel,
    quranViewModel: QuranViewModel,
    qiblaViewModel: QiblaViewModel,
    historyViewModel: HistoryViewModel,
    azkarViewModel: AzkarViewModel,
    audioViewModel: AudioPlayerViewModel,
    hadithViewModel: HadithViewModel? = null
) {
    val navController = rememberNavController()
    var isSearchOverlayOpen by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Prayers,
        BottomNavItem.Quran,
        BottomNavItem.History,
        BottomNavItem.Azkar
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        floatingActionButton = {
            val isBottomBarVisible = currentRoute in listOf("home", "prayers", "quran", "history", "azkar")
            if (isBottomBarVisible && !isSearchOverlayOpen) {
                FloatingActionButton(
                    onClick = { isSearchOverlayOpen = true },
                    containerColor = EmeraldPrimary,
                    contentColor = GoldSecondary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Any Page"
                    )
                }
            }
        },
        bottomBar = {
            val isBottomBarVisible = currentRoute in listOf("home", "prayers", "quran", "history", "azkar")
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = NaturalBackground,
                    contentColor = EmeraldPrimary
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title
                                )
                            },
                            label = {
                                Text(
                                    text = item.title.uppercase(),
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldPrimary,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldPillBg,
                                unselectedIconColor = EmeraldPrimary.copy(alpha = 0.4f),
                                unselectedTextColor = EmeraldPrimary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Scaffold(
            modifier = Modifier.padding(innerPadding),
            bottomBar = {
                MiniAudioPlayer(
                    audioViewModel = audioViewModel,
                    onPlayerClick = {
                        val currentDetail = quranViewModel.surahDetailState.value
                        if (currentDetail is com.example.ui.viewmodels.SurahDetailUiState.Success) {
                            navController.navigate("surah_detail/${currentDetail.surahNumber}")
                        } else {
                            navController.navigate("quran")
                        }
                    }
                )
            }
        ) { mainContentPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(mainContentPadding)
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreen(
                        prayerViewModel = prayerViewModel,
                        quranViewModel = quranViewModel,
                        audioViewModel = audioViewModel,
                        hadithViewModel = hadithViewModel,
                        onNavigateToPrayers = { navController.navigate(BottomNavItem.Prayers.route) },
                        onNavigateToQibla = { navController.navigate("qibla") },
                        onNavigateToQuran = { navController.navigate(BottomNavItem.Quran.route) },
                        onNavigateToSurah = { surahNum -> navController.navigate("surah_detail/$surahNum") },
                        onNavigateToHistory = { navController.navigate(BottomNavItem.History.route) },
                        onNavigateToAzkar = { navController.navigate(BottomNavItem.Azkar.route) },
                        onOpenSearch = { isSearchOverlayOpen = true },
                        onNavigateToEbook = { page -> navController.navigate("quran_ebook?page=$page") }
                    )
                }

                composable(BottomNavItem.Prayers.route) {
                    PrayersScreen(
                        prayerViewModel = prayerViewModel,
                        onNavigateToQibla = { navController.navigate("qibla") }
                    )
                }

                composable("qibla") {
                    QiblaCompassScreen(
                        qiblaViewModel = qiblaViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(BottomNavItem.Quran.route) {
                    QuranScreen(
                        quranViewModel = quranViewModel,
                        onSurahClick = { surahNum ->
                            navController.navigate("surah_detail/$surahNum")
                        },
                        onNavigateToEbook = { page ->
                            navController.navigate("quran_ebook?page=$page")
                        }
                    )
                }

                composable(
                    route = "quran_ebook?page={page}",
                    arguments = listOf(navArgument("page") {
                        type = NavType.IntType
                        defaultValue = 1
                    })
                ) { backStackEntry ->
                    val page = backStackEntry.arguments?.getInt("page") ?: 1
                    com.example.ui.screens.QuranEbookScreen(
                        quranViewModel = quranViewModel,
                        audioViewModel = audioViewModel,
                        initialPage = page,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "surah_detail/{surahNumber}",
                    arguments = listOf(navArgument("surahNumber") { type = NavType.IntType })
                ) { backStackEntry ->
                    val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                    SurahDetailScreen(
                        surahNumber = surahNumber,
                        quranViewModel = quranViewModel,
                        audioViewModel = audioViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(BottomNavItem.History.route) {
                    HistoryScreen(
                        historyViewModel = historyViewModel,
                        onArticleClick = { article ->
                            historyViewModel.selectArticle(article)
                            navController.navigate("article_detail")
                        }
                    )
                }

                composable("article_detail") {
                    val article = historyViewModel.selectedArticle.collectAsState().value
                    if (article != null) {
                        ArticleDetailScreen(
                            article = article,
                            onBackClick = { navController.popBackStack() }
                        )
                    } else {
                        navController.popBackStack()
                    }
                }

                composable(BottomNavItem.Azkar.route) {
                    AzkarToolsScreen(
                        azkarViewModel = azkarViewModel
                    )
                }
            }
        }

        GlobalSearchOverlay(
            isVisible = isSearchOverlayOpen,
            onDismiss = { isSearchOverlayOpen = false },
            quranRepository = quranViewModel.quranRepository,
            historyRepository = historyViewModel.historyRepository,
            azkarRepository = azkarViewModel.azkarRepository,
            onNavigateToSurah = { surahNum ->
                navController.navigate("surah_detail/$surahNum")
            },
            onNavigateToPrayers = {
                navController.navigate(BottomNavItem.Prayers.route)
            },
            onNavigateToQibla = {
                navController.navigate("qibla")
            },
            onNavigateToQuran = {
                navController.navigate(BottomNavItem.Quran.route)
            },
            onNavigateToHistory = {
                navController.navigate(BottomNavItem.History.route)
            },
            onNavigateToArticle = { article ->
                historyViewModel.selectArticle(article)
                navController.navigate("article_detail")
            },
            onNavigateToAzkarCategory = { category ->
                azkarViewModel.selectCategory(category)
                navController.navigate(BottomNavItem.Azkar.route)
            }
        )
    }
}
