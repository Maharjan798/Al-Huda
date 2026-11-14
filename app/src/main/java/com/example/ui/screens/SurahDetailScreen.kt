package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AyahUiModel
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.AudioPlayerState
import com.example.ui.viewmodels.AudioPlayerViewModel
import com.example.ui.viewmodels.QuranViewModel
import com.example.ui.viewmodels.SurahDetailUiState

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(
    surahNumber: Int,
    quranViewModel: QuranViewModel,
    audioViewModel: AudioPlayerViewModel,
    onBackClick: () -> Unit
) {
    val surahDetailState by quranViewModel.surahDetailState.collectAsState()
    val fontScale by quranViewModel.fontScale.collectAsState()
    val translationId by quranViewModel.translationId.collectAsState()
    val reciterId by quranViewModel.reciterId.collectAsState()
    val playerState by audioViewModel.playerState.collectAsState()
    val isRepeatEnabled by audioViewModel.isRepeatEnabled.collectAsState()
    val playbackSpeed by audioViewModel.playbackSpeed.collectAsState()
    val currentPlayingIndex by audioViewModel.currentPlayingIndex.collectAsState()

    var selectedTafsirAyah by remember { mutableStateOf<AyahUiModel?>(null) }
    var showTranslationDropdown by remember { mutableStateOf(false) }
    var showReciterDropdown by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(surahNumber) {
        quranViewModel.loadSurahDetail(surahNumber)
    }

    // Auto-scroll to current playing verse when voice mode is active
    LaunchedEffect(currentPlayingIndex, playerState) {
        if (playerState is AudioPlayerState.Playing || playerState is AudioPlayerState.Buffering) {
            val detail = surahDetailState
            if (detail is SurahDetailUiState.Success && detail.surahNumber == surahNumber) {
                // Offset by 2 for Bismillah and Voice Mode card headers
                val scrollIndex = (currentPlayingIndex + 2).coerceIn(0, detail.ayahs.size + 1)
                lazyListState.animateScrollToItem(scrollIndex)
            }
        }
    }

    val availableTranslations = quranViewModel.availableTranslations

    val reciters = listOf(
        Pair("ar.alafasy", "Mishary Rashid Alafasy"),
        Pair("ar.abdulbasitmurattal", "Abdul Basit Abdul Samad"),
        Pair("ar.shaatree", "Abu Bakr Al-Shatri"),
        Pair("ar.husary", "Mahmoud Khalil Al-Husary")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (val state = surahDetailState) {
                        is SurahDetailUiState.Success -> state.ayahs.firstOrNull()?.surahName ?: "Surah $surahNumber"
                        else -> "Surah $surahNumber"
                    }
                    Text(
                        text = titleText,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                ),
                actions = {
                    // Font Scale Down
                    IconButton(onClick = {
                        if (fontScale > 0.9f) quranViewModel.updateFontScale(fontScale - 0.1f)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Font Smaller",
                            tint = GoldSecondary
                        )
                    }

                    // Font Scale Up
                    IconButton(onClick = {
                        if (fontScale < 2.0f) quranViewModel.updateFontScale(fontScale + 0.1f)
                    }) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Font Larger",
                            tint = GoldSecondary
                        )
                    }

                    // Translation Selector
                    Box {
                        IconButton(onClick = { showTranslationDropdown = true }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translation",
                                tint = GoldSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showTranslationDropdown,
                            onDismissRequest = { showTranslationDropdown = false }
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
                                        showTranslationDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Repeat Ayah Loop Toggle
                    IconButton(onClick = { audioViewModel.toggleRepeat() }) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat Loop",
                            tint = if (isRepeatEnabled) GoldSecondary else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = surahDetailState) {
                is SurahDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GoldSecondary)
                    }
                }
                is SurahDetailUiState.Success -> {
                    val currentReciterName = reciters.firstOrNull { it.first == reciterId }?.second ?: "Mishary Alafasy"
                    val isSurahPlaying = when (playerState) {
                        is AudioPlayerState.Playing -> true
                        is AudioPlayerState.Buffering -> true
                        else -> false
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Voice Mode Hero Control Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = EmeraldPrimary
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = CircleShape,
                                                color = GoldSecondary.copy(alpha = 0.2f),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Mic,
                                                        contentDescription = "Voice Mode",
                                                        tint = GoldSecondary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "VOICE MODE • RECITATION",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = GoldSecondary,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                                Text(
                                                    text = currentReciterName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.9f),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }

                                        // Reciter Selector Dropdown Button
                                        Box {
                                            IconButton(onClick = { showReciterDropdown = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Change Reciter",
                                                    tint = GoldSecondary
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = showReciterDropdown,
                                                onDismissRequest = { showReciterDropdown = false }
                                            ) {
                                                reciters.forEach { (id, name) ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = name,
                                                                fontWeight = if (id == reciterId) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (id == reciterId) GoldSecondary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                        },
                                                        onClick = {
                                                            quranViewModel.updateReciter(id)
                                                            showReciterDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Play / Pause Full Surah Button
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = GoldSecondary,
                                            modifier = Modifier.clickable {
                                                if (isSurahPlaying) {
                                                    audioViewModel.togglePlayPause()
                                                } else {
                                                    audioViewModel.playAyahList(state.ayahs, 0)
                                                }
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isSurahPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                    contentDescription = "Play Full Surah",
                                                    tint = EmeraldPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isSurahPlaying) "Pause Recitation" else "Play Full Surah",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Speed Selector Chips (0.75x, 1.0x, 1.25x, 1.5x)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                                                val isSelected = playbackSpeed == speed
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelected) GoldSecondary else Color.White.copy(alpha = 0.15f),
                                                    modifier = Modifier.clickable { audioViewModel.setPlaybackSpeed(speed) }
                                                ) {
                                                    Text(
                                                        text = "${speed}x",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) EmeraldPrimary else Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bismillah Header (except for Surah At-Tawbah #9)
                        if (surahNumber != 9) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp, top = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = EmeraldPrimary
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = GoldSecondary,
                                            fontSize = (24 * fontScale).sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        itemsIndexed(state.ayahs) { index, ayah ->
                            val isCurrentPlaying = when (playerState) {
                                is AudioPlayerState.Playing -> (playerState as AudioPlayerState.Playing).currentAyah.numberInSurah == ayah.numberInSurah
                                is AudioPlayerState.Paused -> (playerState as AudioPlayerState.Paused).currentAyah.numberInSurah == ayah.numberInSurah
                                is AudioPlayerState.Buffering -> (playerState as AudioPlayerState.Buffering).currentAyah.numberInSurah == ayah.numberInSurah
                                else -> false
                            }

                            AyahCardItem(
                                ayah = ayah,
                                fontScale = fontScale,
                                isPlaying = isCurrentPlaying,
                                onPlayClick = {
                                    audioViewModel.playAyahList(state.ayahs, index)
                                },
                                onBookmarkClick = {
                                    quranViewModel.toggleBookmark(ayah)
                                },
                                onTafsirClick = {
                                    selectedTafsirAyah = ayah
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
                is SurahDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
            }
        }
    }

    // Tafsir Modal Dialog
    if (selectedTafsirAyah != null) {
        AlertDialog(
            onDismissRequest = { selectedTafsirAyah = null },
            title = {
                Text(
                    text = "Tafsir - Ayah ${selectedTafsirAyah!!.numberInSurah}",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = selectedTafsirAyah!!.arabicText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = GoldSecondary,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = selectedTafsirAyah!!.tafsirText ?: "Tafsir commentary unavailable for this verse.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTafsirAyah = null }) {
                    Text("Close", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun AyahCardItem(
    ayah: AyahUiModel,
    fontScale: Float,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onTafsirClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) EmeraldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Action Header: Ayah Number, Play, Bookmark, Tafsir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = EmeraldPrimary,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = ayah.numberInSurah.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTafsirClick) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Tafsir",
                            tint = GoldSecondary
                        )
                    }

                    IconButton(onClick = onBookmarkClick) {
                        Icon(
                            imageVector = if (ayah.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = GoldSecondary
                        )
                    }

                    IconButton(onClick = onPlayClick) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play Audio",
                            tint = EmeraldPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Uthmani Arabic Text
            Text(
                text = ayah.arabicText,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = (22 * fontScale).sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = (36 * fontScale).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Translation Text
            Text(
                text = ayah.translationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (14 * fontScale).sp,
                lineHeight = (20 * fontScale).sp
            )
        }
    }
}
