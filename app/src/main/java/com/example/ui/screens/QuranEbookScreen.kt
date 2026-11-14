package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AyahUiModel
import com.example.data.models.EbookAyahUiModel
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.AudioPlayerState
import com.example.ui.viewmodels.AudioPlayerViewModel
import com.example.ui.viewmodels.EbookPageUiState
import com.example.ui.viewmodels.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranEbookScreen(
    quranViewModel: QuranViewModel,
    audioViewModel: AudioPlayerViewModel,
    initialPage: Int = 1,
    onBackClick: () -> Unit
) {
    val ebookPageState by quranViewModel.ebookPageState.collectAsState()
    val currentEbookPage by quranViewModel.currentEbookPage.collectAsState()
    val ebookTheme by quranViewModel.ebookTheme.collectAsState()
    val ebookShowTranslation by quranViewModel.ebookShowTranslation.collectAsState()
    val ebookFontScale by quranViewModel.ebookFontScale.collectAsState()
    val translationId by quranViewModel.translationId.collectAsState()
    val reciterId by quranViewModel.reciterId.collectAsState()
    val availableTranslations = quranViewModel.availableTranslations
    val playerState by audioViewModel.playerState.collectAsState()
    val currentPlayingIndex by audioViewModel.currentPlayingIndex.collectAsState()

    var showJumpModal by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }
    var showTranslationMenu by remember { mutableStateOf(false) }
    var sliderPageValue by remember { mutableFloatStateOf(currentEbookPage.toFloat()) }
    var isDraggingSlider by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(initialPage) {
        if (initialPage in 1..604 && initialPage != currentEbookPage) {
            quranViewModel.loadEbookPage(initialPage)
        }
    }

    LaunchedEffect(currentEbookPage) {
        if (!isDraggingSlider) {
            sliderPageValue = currentEbookPage.toFloat()
        }
        lazyListState.scrollToItem(0)
    }

    // Colors according to E-Book reading theme
    val (bgColor, cardColor, textArColor, textTrColor) = when (ebookTheme) {
        "NIGHT" -> Quad(
            Color(0xFF101614),
            Color(0xFF18221E),
            Color(0xFFF3E7C4),
            Color(0xFFC7D3CD)
        )
        "DAY" -> Quad(
            Color(0xFFFAFAFA),
            Color(0xFFFFFFFF),
            Color(0xFF1B2A22),
            Color(0xFF37474F)
        )
        else -> Quad( // PARCHMENT (warm classic Quran paper)
            Color(0xFFFBF7EE),
            Color(0xFFFFFDF8),
            Color(0xFF241C10),
            Color(0xFF4A3E2C)
        )
    }

    val currentTranslation = availableTranslations.find { it.id == translationId } ?: availableTranslations.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Quran E-Book (المصحف الشريف)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Page $currentEbookPage of 604 • Authentic 15-Line Mushaf",
                            fontSize = 11.sp,
                            color = GoldSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Jump to Page / Surah / Juz Modal
                    IconButton(onClick = { showJumpModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Jump to Page or Surah",
                            tint = GoldSecondary
                        )
                    }

                    // Reading Theme Palette
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Reading Theme",
                                tint = GoldSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("📜 Parchment (Warm Paper)") },
                                onClick = {
                                    quranViewModel.setEbookTheme("PARCHMENT")
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🌙 Night Mode (Dark Emerald)") },
                                onClick = {
                                    quranViewModel.setEbookTheme("NIGHT")
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("☀️ Day Mode (Clean White)") },
                                onClick = {
                                    quranViewModel.setEbookTheme("DAY")
                                    showThemeMenu = false
                                }
                            )
                        }
                    }

                    // Translation Language Selector
                    Box {
                        IconButton(onClick = { showTranslationMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Select Translation",
                                tint = GoldSecondary
                            )
                        }
                        DropdownMenu(
                            expanded = showTranslationMenu,
                            onDismissRequest = { showTranslationMenu = false }
                        ) {
                            availableTranslations.forEach { trans ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = trans.flagEmoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = trans.languageName,
                                                    fontWeight = if (trans.id == translationId) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (trans.id == translationId) GoldSecondary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = trans.translatorName,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        quranViewModel.updateTranslation(trans.id)
                                        showTranslationMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                )
            )
        },
        bottomBar = {
            // E-Book Reader Controls (Page Slider & Navigation)
            Surface(
                color = cardColor,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Slider & Page indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page ${sliderPageValue.toInt()} / 604",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textTrColor
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Dual translation toggle pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (ebookShowTranslation) GoldSecondary.copy(alpha = 0.2f) else textTrColor.copy(alpha = 0.1f),
                                modifier = Modifier.clickable { quranViewModel.toggleEbookShowTranslation() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (ebookShowTranslation) "${currentTranslation.flagEmoji} Dual View" else "📖 Arabic Only",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ebookShowTranslation) EmeraldPrimary else textTrColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Font size buttons
                            IconButton(
                                onClick = { quranViewModel.updateEbookFontScale(ebookFontScale - 0.15f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textTrColor)
                            }
                            IconButton(
                                onClick = { quranViewModel.updateEbookFontScale(ebookFontScale + 0.15f) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("A+", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textTrColor)
                            }
                        }
                    }

                    Slider(
                        value = sliderPageValue,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderPageValue = it
                        },
                        onValueChangeFinished = {
                            isDraggingSlider = false
                            quranViewModel.loadEbookPage(sliderPageValue.toInt())
                        },
                        valueRange = 1f..604f,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldSecondary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = textTrColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    )

                    // Page Back / Forward and Listen Page Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page button
                        TextButton(
                            onClick = { quranViewModel.previousEbookPage() },
                            enabled = currentEbookPage > 1
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Page",
                                tint = if (currentEbookPage > 1) EmeraldPrimary else textTrColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Prev Page",
                                color = if (currentEbookPage > 1) EmeraldPrimary else textTrColor.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Play Entire Page Audio recitation
                        val isPlayingThisPage = playerState is AudioPlayerState.Playing
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = EmeraldPrimary,
                            modifier = Modifier.clickable {
                                val state = ebookPageState
                                if (state is EbookPageUiState.Success) {
                                    val playlist = state.pageModel.ayahs.map { it.toAyahUiModel() }
                                    if (isPlayingThisPage) {
                                        audioViewModel.togglePlayPause()
                                    } else {
                                        audioViewModel.playAyahList(playlist, 0)
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isPlayingThisPage) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Page Audio",
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPlayingThisPage) "Pause Recitation" else "Listen Page",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Next Page button
                        TextButton(
                            onClick = { quranViewModel.nextEbookPage() },
                            enabled = currentEbookPage < 604
                        ) {
                            Text(
                                text = "Next Page",
                                color = if (currentEbookPage < 604) EmeraldPrimary else textTrColor.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Page",
                                tint = if (currentEbookPage < 604) EmeraldPrimary else textTrColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
        ) {
            when (val state = ebookPageState) {
                is EbookPageUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading Page $currentEbookPage...",
                                color = textTrColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is EbookPageUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Could not load online page",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    color = textTrColor,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = { quranViewModel.loadEbookPage(currentEbookPage) }) {
                                    Text("Retry", fontWeight = FontWeight.Bold, color = GoldSecondary)
                                }
                            }
                        }
                    }
                }
                is EbookPageUiState.Success -> {
                    val page = state.pageModel

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Classical Quran Header Medallion
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = cardColor,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "جُزْءُ ${page.juzNumber} (Juz ${page.juzNumber})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldSecondary
                                        )
                                        Text(
                                            text = page.primarySurahNameEn,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldPrimary.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
                                    ) {
                                        Text(
                                            text = "ص ${page.pageNumber}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = page.primarySurahNameAr,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "سُورَة رقم ${page.primarySurahNumber}",
                                            fontSize = 11.sp,
                                            color = textTrColor.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }

                        // Page Ayahs List
                        itemsIndexed(page.ayahs) { index, ayah ->
                            // Surah opening banner if this verse begins a new Surah on this page
                            if (ayah.isNewSurahStart) {
                                SurahHeaderCalligraphyBanner(
                                    surahNumber = ayah.surahNumber,
                                    surahNameAr = ayah.surahNameAr,
                                    surahNameEn = ayah.surahNameEn
                                )
                            }

                            // Ayah card
                            EbookAyahItem(
                                ayah = ayah,
                                isPlaying = (playerState is AudioPlayerState.Playing && currentPlayingIndex == index),
                                showTranslation = ebookShowTranslation,
                                fontScale = ebookFontScale,
                                cardColor = cardColor,
                                textArColor = textArColor,
                                textTrColor = textTrColor,
                                onPlayClick = {
                                    val playlist = page.ayahs.map { it.toAyahUiModel() }
                                    audioViewModel.playAyahList(playlist, index)
                                },
                                onBookmarkClick = {
                                    quranViewModel.toggleEbookBookmark(ayah)
                                }
                            )
                        }

                        // Page Bottom Footer
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "— نهاية صفحة ${page.pageNumber} —",
                                    fontSize = 12.sp,
                                    color = textTrColor.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to Jump to any Page (1..604), Surah (1..114), or Juz (1..30)
    if (showJumpModal) {
        EbookJumpDialog(
            currentPage = currentEbookPage,
            surahList = quranViewModel.quranRepository.getPreloadedSurahList(),
            onDismiss = { showJumpModal = false },
            onJumpToPage = { page ->
                quranViewModel.loadEbookPage(page)
                showJumpModal = false
            },
            onJumpToSurah = { surahNum ->
                quranViewModel.jumpToSurahPage(surahNum)
                showJumpModal = false
            },
            onJumpToJuz = { juzNum ->
                quranViewModel.jumpToJuzPage(juzNum)
                showJumpModal = false
            }
        )
    }
}

@Composable
private fun SurahHeaderCalligraphyBanner(
    surahNumber: Int,
    surahNameAr: String,
    surahNameEn: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EmeraldPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, GoldSecondary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surahNameAr,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GoldSecondary
            )
            Text(
                text = "$surahNumber. $surahNameEn",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            // Bismillah calligraphy (except for Surah At-Tawbah 9)
            if (surahNumber != 9) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EbookAyahItem(
    ayah: EbookAyahUiModel,
    isPlaying: Boolean,
    showTranslation: Boolean,
    fontScale: Float,
    cardColor: Color,
    textArColor: Color,
    textTrColor: Color,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val highlightBg by animateColorAsState(
        targetValue = if (isPlaying) GoldSecondary.copy(alpha = 0.18f) else cardColor,
        label = "highlight"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = highlightBg),
        shape = RoundedCornerShape(12.dp),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Verse top badge and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "${ayah.surahNumber}:${ayah.numberInSurah}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.VolumeUp,
                            contentDescription = "Play Verse Audio",
                            tint = if (isPlaying) GoldSecondary else textTrColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (ayah.isBookmarked) GoldSecondary else textTrColor.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sacred Arabic Uthmani Text with ornamental end symbol
            Text(
                text = "${ayah.arabicText} ۝${ayah.numberInSurah}",
                fontSize = (22 * fontScale).sp,
                fontWeight = FontWeight.Normal,
                color = textArColor,
                textAlign = TextAlign.Right,
                lineHeight = (36 * fontScale).sp,
                modifier = Modifier.fillMaxWidth()
            )

            // Translation text in selected language (Hindi, English, etc.)
            AnimatedVisibility(visible = showTranslation && ayah.translationText.isNotBlank()) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ayah.translationText,
                        fontSize = (14 * fontScale.coerceIn(0.9f, 1.3f)).sp,
                        color = textTrColor,
                        lineHeight = (22 * fontScale.coerceIn(0.9f, 1.3f)).sp
                    )
                }
            }
        }
    }
}

@Composable
private fun EbookJumpDialog(
    currentPage: Int,
    surahList: List<com.example.data.models.SurahDto>,
    onDismiss: () -> Unit,
    onJumpToPage: (Int) -> Unit,
    onJumpToSurah: (Int) -> Unit,
    onJumpToJuz: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pageInputText by remember { mutableStateOf(currentPage.toString()) }

    val juzStartPages = listOf(
        1 to 1, 2 to 22, 3 to 42, 4 to 62, 5 to 82, 6 to 102, 7 to 121, 8 to 142, 9 to 162, 10 to 182,
        11 to 201, 12 to 222, 13 to 242, 14 to 262, 15 to 282, 16 to 302, 17 to 322, 18 to 342, 19 to 362, 20 to 382,
        21 to 402, 22 to 422, 23 to 442, 24 to 462, 25 to 482, 26 to 502, 27 to 522, 28 to 542, 29 to 562, 30 to 582
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Jump to Quran Position",
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    contentColor = GoldSecondary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldSecondary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Page (1..604)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Surah (114)") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Juz (30)") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        Column {
                            Text("Enter Page Number (1 to 604):", fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = pageInputText,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        pageInputText = input
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf(1, 50, 106, 293, 440, 562, 604).forEach { samplePage ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = EmeraldPrimary.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable { onJumpToPage(samplePage) }
                                    ) {
                                        Text(
                                            text = "p.$samplePage",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(modifier = Modifier.height(280.dp)) {
                            itemsIndexed(surahList) { _, surah ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJumpToSurah(surah.number) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${surah.number}. ${surah.englishName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = surah.englishNameTranslation,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Text(
                                        text = surah.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        LazyColumn(modifier = Modifier.height(280.dp)) {
                            itemsIndexed(juzStartPages) { _, (juzNum, pageNum) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJumpToJuz(juzNum) }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Juz $juzNum (الجزء $juzNum)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = EmeraldPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GoldSecondary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Starts Page $pageNum",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                TextButton(
                    onClick = {
                        val p = pageInputText.toIntOrNull() ?: currentPage
                        onJumpToPage(p.coerceIn(1, 604))
                    }
                ) {
                    Text("Go to Page", fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun EbookAyahUiModel.toAyahUiModel(): AyahUiModel = AyahUiModel(
    numberInSurah = this.numberInSurah,
    globalAyahNumber = this.globalNumber,
    arabicText = this.arabicText,
    translationText = this.translationText,
    tafsirText = null,
    audioUrl = this.audioUrl,
    surahNumber = this.surahNumber,
    surahName = this.surahNameAr,
    isBookmarked = this.isBookmarked
)

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
