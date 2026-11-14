package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.HistoricalArticle
import com.example.data.repository.AzkarRepository
import com.example.data.repository.HistoryRepository
import com.example.data.repository.QuranRepository
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary

enum class SearchCategory(val label: String) {
    ALL("All Pages"),
    QURAN("Quran"),
    PRAYERS("Prayers & Qibla"),
    AZKAR("Azkar & Duas"),
    HISTORY("History"),
    TOOLS("Tools")
}

data class GlobalSearchResultItem(
    val id: String,
    val title: String,
    val titleArabic: String? = null,
    val subtitle: String,
    val category: SearchCategory,
    val icon: ImageVector,
    val badgeText: String? = null,
    val onSelect: () -> Unit
)

@Composable
fun GlobalSearchOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    quranRepository: QuranRepository,
    historyRepository: HistoryRepository,
    azkarRepository: AzkarRepository,
    onNavigateToSurah: (Int) -> Unit,
    onNavigateToPrayers: () -> Unit,
    onNavigateToQibla: () -> Unit,
    onNavigateToQuran: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToArticle: (HistoricalArticle) -> Unit,
    onNavigateToAzkarCategory: (String) -> Unit,
    initialQuery: String = ""
) {
    if (!isVisible) return

    var query by remember { mutableStateOf(initialQuery) }
    var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }

    // Preloaded Data for Indexing
    val surahs = remember { quranRepository.getPreloadedSurahList() }
    val articles = remember { historyRepository.articles }
    val azkarList = remember { azkarRepository.getAllAzkar() }

    // Search Engine Logic
    val searchResults = remember(query, selectedCategory) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return@remember emptyList()

        val results = mutableListOf<GlobalSearchResultItem>()

        // 1. Quran Surahs Search
        if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.QURAN) {
            surahs.forEach { surah ->
                val matchesName = surah.englishName.lowercase().contains(q) ||
                        surah.englishNameTranslation.lowercase().contains(q) ||
                        surah.name.lowercase().contains(q) ||
                        surah.number.toString() == q ||
                        "surah ${surah.number}" == q ||
                        "surah ${surah.englishName.lowercase()}" .contains(q)

                if (matchesName) {
                    results.add(
                        GlobalSearchResultItem(
                            id = "surah_${surah.number}",
                            title = "${surah.number}. Surah ${surah.englishName}",
                            titleArabic = surah.name,
                            subtitle = "${surah.englishNameTranslation} • ${surah.revelationType} (${surah.numberOfAyahs} Ayahs)",
                            category = SearchCategory.QURAN,
                            icon = Icons.Default.MenuBook,
                            badgeText = "Surah ${surah.number}",
                            onSelect = {
                                onDismiss()
                                onNavigateToSurah(surah.number)
                            }
                        )
                    )
                }
            }

            // General Quran features
            if ("bookmark".contains(q) || "saved".contains(q) || "last read".contains(q)) {
                results.add(
                    GlobalSearchResultItem(
                        id = "quran_bookmarks",
                        title = "Quran Bookmarks & Saved Verses",
                        subtitle = "View your saved ayah bookmarks and reading history",
                        category = SearchCategory.QURAN,
                        icon = Icons.Default.MenuBook,
                        badgeText = "Page",
                        onSelect = {
                            onDismiss()
                            onNavigateToQuran()
                        }
                    )
                )
            }
        }

        // 2. Prayers & Qibla Search
        if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.PRAYERS) {
            val prayerItems = listOf(
                Pair("fajr", "Fajr Prayer (الفجر)"),
                Pair("dhuhr", "Dhuhr Prayer (الظهر)"),
                Pair("asr", "Asr Prayer (العصر)"),
                Pair("maghrib", "Maghrib Prayer (المغرب)"),
                Pair("isha", "Isha Prayer (العشاء)"),
                Pair("sunrise", "Sunrise Time (الشروق)"),
                Pair("prayer", "Daily Prayer Schedule"),
                Pair("salah", "Salah Timings & Alarm Settings"),
                Pair("method", "Prayer Calculation Method & School Settings"),
                Pair("calendar", "Device Calendar Reminders Sync")
            )

            prayerItems.forEach { (keyword, titleName) ->
                if (titleName.lowercase().contains(q) || keyword.contains(q) || q.contains(keyword)) {
                    results.add(
                        GlobalSearchResultItem(
                            id = "prayer_$keyword",
                            title = titleName,
                            subtitle = "Daily prayer timings, notifications & location schedule",
                            category = SearchCategory.PRAYERS,
                            icon = Icons.Default.Schedule,
                            badgeText = "Prayers",
                            onSelect = {
                                onDismiss()
                                onNavigateToPrayers()
                            }
                        )
                    )
                }
            }

            if ("qibla".contains(q) || "compass".contains(q) || "direction".contains(q) || "makkah".contains(q) || "kaaba".contains(q)) {
                results.add(
                    GlobalSearchResultItem(
                        id = "qibla_compass",
                        title = "Qibla Direction & Compass",
                        subtitle = "Locate precise direction to Kaaba, Makkah Mukarramah",
                        category = SearchCategory.PRAYERS,
                        icon = Icons.Default.CompassCalibration,
                        badgeText = "Compass",
                        onSelect = {
                            onDismiss()
                            onNavigateToQibla()
                        }
                    )
                )
            }
        }

        // 3. Azkar & Duas Search
        if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.AZKAR) {
            val categories = listOf("Morning", "Evening", "After Prayer", "Sleep")
            categories.forEach { cat ->
                if (cat.lowercase().contains(q) || "azkar".contains(q) || "dua".contains(q) || "dhikr".contains(q)) {
                    results.add(
                        GlobalSearchResultItem(
                            id = "azkar_cat_$cat",
                            title = "$cat Azkar & Supplications",
                            subtitle = "Daily $cat remembrances with count trackers",
                            category = SearchCategory.AZKAR,
                            icon = Icons.Default.AutoAwesome,
                            badgeText = "Azkar",
                            onSelect = {
                                onDismiss()
                                onNavigateToAzkarCategory(cat)
                            }
                        )
                    )
                }
            }

            if ("tasbeeh".contains(q) || "counter".contains(q) || "tally".contains(q) || "dhikr".contains(q)) {
                results.add(
                    GlobalSearchResultItem(
                        id = "azkar_tasbeeh",
                        title = "Digital Tasbeeh Counter",
                        subtitle = "Interactive dhikr counter with custom target goals (33x, 100x)",
                        category = SearchCategory.AZKAR,
                        icon = Icons.Default.AutoAwesome,
                        badgeText = "Tool",
                        onSelect = {
                            onDismiss()
                            onNavigateToAzkarCategory("Morning")
                        }
                    )
                )
            }

            if ("hadith".contains(q) || "bukhari".contains(q) || "muslim".contains(q) || "prophet".contains(q) || "inspiration".contains(q) || "saying".contains(q)) {
                results.add(
                    GlobalSearchResultItem(
                        id = "hadith_of_the_day",
                        title = "Hadith of the Day (Daily Inspiration)",
                        titleArabic = "حديث اليوم",
                        subtitle = "Authentic Prophetic sayings from Sahih al-Bukhari, Muslim & Tirmidhi",
                        category = SearchCategory.AZKAR,
                        icon = Icons.Default.AutoAwesome,
                        badgeText = "Daily Hadith",
                        onSelect = {
                            onDismiss()
                        }
                    )
                )
            }

            azkarList.forEach { azkar ->
                if (azkar.translation.lowercase().contains(q) || azkar.category.lowercase().contains(q) || azkar.arabicText.contains(q)) {
                    results.add(
                        GlobalSearchResultItem(
                            id = "azkar_item_${azkar.id}",
                            title = "${azkar.category} Dhikr: ${azkar.translation.take(50)}...",
                            titleArabic = azkar.arabicText,
                            subtitle = "Source: ${azkar.reference} • Target: ${azkar.targetCount}x",
                            category = SearchCategory.AZKAR,
                            icon = Icons.Default.AutoAwesome,
                            badgeText = azkar.category,
                            onSelect = {
                                onDismiss()
                                onNavigateToAzkarCategory(azkar.category)
                            }
                        )
                    )
                }
            }
        }

        // 4. History Search
        if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.HISTORY) {
            articles.forEach { article ->
                val matchesArticle = article.title.lowercase().contains(q) ||
                        article.overview.lowercase().contains(q) ||
                        article.location.lowercase().contains(q) ||
                        article.detailedContent.lowercase().contains(q)

                if (matchesArticle) {
                    results.add(
                        GlobalSearchResultItem(
                            id = "article_${article.id}",
                            title = article.title,
                            titleArabic = article.titleArabic,
                            subtitle = "${article.dateOrYear} • ${article.location}",
                            category = SearchCategory.HISTORY,
                            icon = Icons.Default.History,
                            badgeText = "Article",
                            onSelect = {
                                onDismiss()
                                onNavigateToArticle(article)
                            }
                        )
                    )
                }
            }
        }

        // 5. Tools & Settings Search
        if (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.TOOLS) {
            if ("translation".contains(q) || "language".contains(q) || "english".contains(q) || "urdu".contains(q) || "indonesian".contains(q)) {
                results.add(
                    GlobalSearchResultItem(
                        id = "tool_translation",
                        title = "Global Quran Translation Settings",
                        subtitle = "Switch between 10 global languages (English, Urdu, Indonesian, etc.)",
                        category = SearchCategory.TOOLS,
                        icon = Icons.Default.Translate,
                        badgeText = "Settings",
                        onSelect = {
                            onDismiss()
                            onNavigateToQuran()
                        }
                    )
                )
            }

            if ("reciter".contains(q) || "audio".contains(q) || "mishary".contains(q) || "voice".contains(q) || "listen".contains(q)) {
                results.add(
                    GlobalSearchResultItem(
                        id = "tool_reciter",
                        title = "Quran Reciter & Audio Voice",
                        subtitle = "Select reciter audio stream (Al-Alafasy, Abdul Basit, Al-Shatri, Husary)",
                        category = SearchCategory.TOOLS,
                        icon = Icons.Default.Mic,
                        badgeText = "Audio",
                        onSelect = {
                            onDismiss()
                            onNavigateToQuran()
                        }
                    )
                )
            }
        }

        results.distinctBy { it.id }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.98f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Header Search Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close Search",
                        tint = EmeraldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            text = "Search Surahs, Prayers, Azkar, History...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = EmeraldPrimary
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldSecondary,
                        unfocusedBorderColor = EmeraldPrimary.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SearchCategory.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = GoldSecondary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // When Search Query is Empty -> Show Popular Shortcut Chips
            if (query.trim().isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "POPULAR QUICK SEARCHES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val quickChips = listOf(
                        Pair("Surah Yasin", SearchCategory.QURAN),
                        Pair("Fajr Prayer", SearchCategory.PRAYERS),
                        Pair("Qibla Compass", SearchCategory.PRAYERS),
                        Pair("Morning Azkar", SearchCategory.AZKAR),
                        Pair("Battle of Badr", SearchCategory.HISTORY),
                        Pair("Surah Al-Baqarah", SearchCategory.QURAN),
                        Pair("Digital Tasbeeh", SearchCategory.AZKAR),
                        Pair("Calendar Reminders", SearchCategory.PRAYERS)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickChips) { (chipText, cat) ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = EmeraldPrimary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f)),
                                modifier = Modifier.clickable {
                                    query = chipText
                                    selectedCategory = SearchCategory.ALL
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = chipText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = EmeraldPrimary.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Search any page, surah, prayer, or historical article",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                // Search Results List
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results found for '$query'",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try searching for Surah names, Prayer times, or Azkar keywords",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "FOUND ${searchResults.size} RESULTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults, key = { it.id }) { item ->
                            SearchResultCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(item: GlobalSearchResultItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                color = EmeraldPrimary.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.category.label,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    item.badgeText?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldSecondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                item.titleArabic?.let { title ->
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Text(
                    text = item.subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun String?.isNull_or_Empty(): Boolean = this == null || this.trim().isEmpty()
