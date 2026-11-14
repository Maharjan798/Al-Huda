package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.QuranBookmarkEntity
import com.example.data.models.SurahDto
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSecondary
import com.example.ui.viewmodels.QuranViewModel
import com.example.ui.viewmodels.SurahListUiState

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Translate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    quranViewModel: QuranViewModel,
    onSurahClick: (Int) -> Unit,
    onNavigateToEbook: (Int) -> Unit = {}
) {
    val surahListState by quranViewModel.surahListState.collectAsState()
    val searchQuery by quranViewModel.searchQuery.collectAsState()
    val bookmarks by quranViewModel.bookmarks.collectAsState()
    val reciterId by quranViewModel.reciterId.collectAsState()
    val translationId by quranViewModel.translationId.collectAsState()
    val currentEbookPage by quranViewModel.currentEbookPage.collectAsState()
    val availableTranslations = quranViewModel.availableTranslations

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showReciterDropdown by remember { mutableStateOf(false) }
    var showTranslationDropdown by remember { mutableStateOf(false) }

    val currentTranslation = availableTranslations.find { it.id == translationId } ?: availableTranslations.first()

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
                    Text(
                        text = "Quran Kareem (القرآن الكريم)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EmeraldPrimary
                ),
                actions = {
                    // Translation Language Selector
                    Box {
                        IconButton(onClick = { showTranslationDropdown = true }) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Select Translation Language",
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

                    // Reciter Selector
                    Box {
                        IconButton(onClick = { showReciterDropdown = true }) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Select Reciter",
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
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { quranViewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search Surah name or number...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GoldSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { quranViewModel.onSearchQueryChanged("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldSecondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Global Translation Language Banner Chip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { showTranslationDropdown = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = currentTranslation.flagEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GLOBAL TRANSLATION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${currentTranslation.languageName} • ${currentTranslation.translatorName}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EmeraldPrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "Switch",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }


            // Quran E-Book Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { onNavigateToEbook(currentEbookPage) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldSecondary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldSecondary.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Quran E-Book",
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "COMPLETE QURAN E-BOOK (المصحف)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Read 604-Page Mushaf • Hindi & Arabic",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Resume reading from Page $currentEbookPage",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GoldSecondary
                    ) {
                        Text(
                            text = "Open E-Book",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Tabs: All 114 Surahs / E-Book / 30 Juz / Bookmarks
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = GoldSecondary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = GoldSecondary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Surahs",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "E-Book",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            text = "30 Juz",
                            fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = {
                        Text(
                            text = "Bookmarks (${bookmarks.size})",
                            fontWeight = if (selectedTabIndex == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    when (val state = surahListState) {
                    is SurahListUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldSecondary)
                        }
                    }
                    is SurahListUiState.Success -> {
                        val featuredVoiceSurahs = listOf(
                            Triple(36, "Surah Yasin", "يس"),
                            Triple(67, "Surah Al-Mulk", "الملك"),
                            Triple(55, "Surah Ar-Rahman", "الرحمن"),
                            Triple(18, "Surah Al-Kahf", "الكهف"),
                            Triple(2, "Surah Al-Baqarah", "البقرة")
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (searchQuery.isEmpty()) {
                                item {
                                    Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Mic,
                                                    contentDescription = "Voice Mode",
                                                    tint = GoldSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "VOICE MODE • QUICK RECITATIONS",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EmeraldPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            items(featuredVoiceSurahs) { (number, title, arabic) ->
                                                Card(
                                                    modifier = Modifier
                                                        .width(150.dp)
                                                        .clickable { onSurahClick(number) },
                                                    shape = RoundedCornerShape(18.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = EmeraldPrimary
                                                    ),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = arabic,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = GoldSecondary,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = GoldSecondary,
                                                                modifier = Modifier.size(26.dp)
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.PlayArrow,
                                                                        contentDescription = "Play Recitation",
                                                                        tint = EmeraldPrimary,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Text(
                                                            text = title,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = "Surah #$number",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White.copy(alpha = 0.7f),
                                                            fontSize = 10.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }

                            items(state.surahs) { surah ->
                                SurahRowItem(
                                    surah = surah,
                                    onClick = { onSurahClick(surah.number) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    is SurahListUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            1 -> {
                // Quran E-Book Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onNavigateToEbook(currentEbookPage) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary),
                            border = androidx.compose.foundation.BorderStroke(2.dp, GoldSecondary)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "Quran E-Book",
                                    tint = GoldSecondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "المصحف الشريف (Quran E-Book)",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldSecondary
                                )
                                Text(
                                    text = "Complete 604-Page Standard Madani Mushaf",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = GoldSecondary,
                                    modifier = Modifier.clickable { onNavigateToEbook(currentEbookPage) }
                                ) {
                                    Text(
                                        text = "Resume Reading • Page $currentEbookPage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "QUICK JUMP TO KEY SURAHS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary.copy(alpha = 0.6f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        val keySurahs = listOf(
                            Triple(1, "Al-Fatihah", 1),
                            Triple(2, "Al-Baqarah", 2),
                            Triple(18, "Al-Kahf", 293),
                            Triple(36, "Ya-Sin", 440),
                            Triple(55, "Ar-Rahman", 531),
                            Triple(67, "Al-Mulk", 562),
                            Triple(112, "Al-Ikhlas", 604)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 14.dp)
                        ) {
                            items(keySurahs) { (surahNum, name, page) ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable { onNavigateToEbook(page) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldPrimary)
                                        Text(text = "p. $page", fontSize = 10.sp, color = GoldSecondary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "BROWSE BY PAGE (1 TO 604)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary.copy(alpha = 0.6f),
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    // Display sample page ranges / milestones
                    val pageMilestones = (1..604 step 10).toList()
                    items(pageMilestones) { startPage ->
                        val endPage = (startPage + 9).coerceAtMost(604)
                        val juz = ((startPage - 1) / 20) + 1
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToEbook(startPage) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0D0A3A2A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = juz.toString(),
                                                color = GoldSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Pages $startPage – $endPage",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = "Juz $juz • Authentic Madani Mushaf",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = "Open p.$startPage",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // 30 Juz Tab
                val juzItems = listOf(
                    Triple(1, "Alif Lam Meem (الم)", 1),
                    Triple(2, "Sayaqulu (سَيَقُولُ)", 22),
                    Triple(3, "Tilka-r-Rusul (تِلْكَ ٱلرُّسُلُ)", 42),
                    Triple(4, "Lan Tanaloo (لَن تَنَالُوا۟)", 62),
                    Triple(5, "Wal Muhsanat (وَٱلْمُحْصَنَـٰتُ)", 82),
                    Triple(6, "La Yuhibbullah (لَا يُحِبُّ ٱللَّهُ)", 102),
                    Triple(7, "Wa Iza Samiu (وَإِذَا سَمِعُوا۟)", 121),
                    Triple(8, "Wa Law Annana (وَلَوْ أَنَّنَا)", 142),
                    Triple(9, "Qalal Malao (قَالَ ٱلْمَلَأُ)", 162),
                    Triple(10, "Wa A'lamu (وَٱعْلَمُوٓا۟)", 182),
                    Triple(11, "Yatazeroona (يَعْتَذِرُونَ)", 201),
                    Triple(12, "Wa Mamin Da'abah (وَمَا مِن دَآبَّةٍ)", 222),
                    Triple(13, "Wa Ma Ubrioo (وَمَآ أُبَرِّئُ)", 242),
                    Triple(14, "Rubama (رُّبَمَا)", 262),
                    Triple(15, "Subhanallazi (سُبْحَـٰنَ ٱلَّذِى)", 282),
                    Triple(16, "Qala Alam (قَالَ أَلَمْ)", 302),
                    Triple(17, "Iqtaraba (ٱقْتَرَبَ)", 322),
                    Triple(18, "Qad Aflaha (قَدْ أَفْلَحَ)", 342),
                    Triple(19, "Wa Qalallazina (وَقَالَ ٱلَّذِينَ)", 362),
                    Triple(20, "Amman Khalaqa (أَمَّنْ خَلَقَ)", 382),
                    Triple(21, "Utlu Ma Oohiya (ٱتْلُ مَآ أُوحِىَ)", 402),
                    Triple(22, "Wa Manyaqnut (وَمَن يَقْنُتْ)", 422),
                    Triple(23, "Wa Maliya (وَمَا لِىَ)", 442),
                    Triple(24, "Faman Azlamu (فَمَنْ أَظْلَمُ)", 462),
                    Triple(25, "Ilayhi Yuraddu (إِلَيْهِ يُرَدُّ)", 482),
                    Triple(26, "Ha'a Meem (حم)", 502),
                    Triple(27, "Qala Fama Khatbukum (قَالَ فَمَا خَطْبُكُمْ)", 522),
                    Triple(28, "Qadd Sami'Allah (قَدْ سَمِعَ ٱللَّهُ)", 542),
                    Triple(29, "Tabarakallazi (تَبَـٰرَكَ ٱلَّذِى)", 562),
                    Triple(30, "Amma Yatasa'aloon (عَمَّ يَتَسَآءَلُونَ)", 582)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(juzItems) { (juzNumber, name, startPage) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigateToEbook(startPage) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0D0A3A2A))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = juzNumber.toString(),
                                                color = GoldSecondary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = "Juz $juzNumber",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldPrimary
                                        )
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GoldSecondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Page $startPage",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Bookmarks Tab
                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "No Bookmarks",
                                tint = GoldSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Bookmarked Ayahs yet.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(bookmarks) { bookmark ->
                            BookmarkRowItem(
                                bookmark = bookmark,
                                onClick = { onSurahClick(bookmark.surahNumber) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun SurahRowItem(
    surah: SurahDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x0D0A3A2A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = EmeraldPrimary,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = surah.number.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.englishName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${surah.englishNameTranslation} • ${surah.revelationType} (${surah.numberOfAyahs} v)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = surah.name,
                style = MaterialTheme.typography.titleLarge,
                color = GoldSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun BookmarkRowItem(
    bookmark: QuranBookmarkEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bookmark.surahName} • Ayah ${bookmark.ayahNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarked",
                    tint = GoldSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bookmark.arabicText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = bookmark.translationText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
