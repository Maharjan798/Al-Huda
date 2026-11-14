package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.AyahUiModel
import com.example.data.models.LastReadPositionEntity
import com.example.data.models.QuranBookmarkEntity
import com.example.data.models.SurahDto
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.models.EbookAyahUiModel
import com.example.data.models.QuranEbookPageUiModel

sealed class SurahListUiState {
    object Loading : SurahListUiState()
    data class Success(val surahs: List<SurahDto>) : SurahListUiState()
    data class Error(val message: String) : SurahListUiState()
}

sealed class SurahDetailUiState {
    object Idle : SurahDetailUiState()
    object Loading : SurahDetailUiState()
    data class Success(val surahNumber: Int, val ayahs: List<AyahUiModel>) : SurahDetailUiState()
    data class Error(val message: String) : SurahDetailUiState()
}

sealed class EbookPageUiState {
    object Loading : EbookPageUiState()
    data class Success(val pageModel: QuranEbookPageUiModel) : EbookPageUiState()
    data class Error(val message: String) : EbookPageUiState()
}

class QuranViewModel(
    val quranRepository: QuranRepository,
    val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _surahListState = MutableStateFlow<SurahListUiState>(SurahListUiState.Loading)
    val surahListState: StateFlow<SurahListUiState> = _surahListState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _surahDetailState = MutableStateFlow<SurahDetailUiState>(SurahDetailUiState.Idle)
    val surahDetailState: StateFlow<SurahDetailUiState> = _surahDetailState.asStateFlow()

    private val _ebookPageState = MutableStateFlow<EbookPageUiState>(EbookPageUiState.Loading)
    val ebookPageState: StateFlow<EbookPageUiState> = _ebookPageState.asStateFlow()

    val currentEbookPage: StateFlow<Int> = settingsRepository.lastEbookPage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1
    )

    val ebookTheme: StateFlow<String> = settingsRepository.ebookTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "PARCHMENT"
    )

    val ebookShowTranslation: StateFlow<Boolean> = settingsRepository.ebookShowTranslation.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val ebookFontScale: StateFlow<Float> = settingsRepository.ebookFontScale.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.3f
    )

    val fontScale: StateFlow<Float> = settingsRepository.fontScale.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 1.2f
    )

    val availableTranslations = quranRepository.availableTranslations

    val translationId: StateFlow<String> = settingsRepository.translationId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "en.sahih"
    )

    val reciterId: StateFlow<String> = settingsRepository.reciterId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ar.alafasy"
    )

    val bookmarks: StateFlow<List<QuranBookmarkEntity>> = quranRepository.allBookmarks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lastReadPosition: StateFlow<LastReadPositionEntity?> = quranRepository.lastReadPosition.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private var allSurahs: List<SurahDto> = emptyList()

    fun loadSurahList() {
        viewModelScope.launch {
            _surahListState.value = SurahListUiState.Loading
            val surahs = quranRepository.getSurahList()
            allSurahs = surahs
            _surahListState.value = SurahListUiState.Success(filterSurahs(surahs, _searchQuery.value))
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (allSurahs.isNotEmpty()) {
            _surahListState.value = SurahListUiState.Success(filterSurahs(allSurahs, query))
        }
    }

    private fun filterSurahs(surahs: List<SurahDto>, query: String): List<SurahDto> {
        if (query.trim().isEmpty()) return surahs
        val q = query.trim().lowercase()
        return surahs.filter { surah ->
            surah.englishName.lowercase().contains(q) ||
            surah.englishNameTranslation.lowercase().contains(q) ||
            surah.name.contains(q) ||
            surah.number.toString() == q
        }
    }

    fun loadSurahDetail(surahNumber: Int) {
        viewModelScope.launch {
            _surahDetailState.value = SurahDetailUiState.Loading
            val currentTrans = translationId.value
            val currentReciter = reciterId.value
            val result = quranRepository.getSurahAyahs(surahNumber, currentTrans, currentReciter)
            result.onSuccess { ayahs ->
                _surahDetailState.value = SurahDetailUiState.Success(surahNumber, ayahs)
                val surahName = ayahs.firstOrNull()?.surahName ?: "Surah $surahNumber"
                quranRepository.saveLastRead(surahNumber, surahName, 1)
            }.onFailure {
                _surahDetailState.value = SurahDetailUiState.Error(it.message ?: "Failed to load Surah")
            }
        }
    }

    fun toggleBookmark(ayah: AyahUiModel) {
        viewModelScope.launch {
            quranRepository.toggleBookmark(ayah)
            // refresh detail if open
            val currentDetail = _surahDetailState.value
            if (currentDetail is SurahDetailUiState.Success) {
                val updated = currentDetail.ayahs.map { item ->
                    if (item.numberInSurah == ayah.numberInSurah) {
                        item.copy(isBookmarked = !item.isBookmarked)
                    } else item
                }
                _surahDetailState.value = SurahDetailUiState.Success(currentDetail.surahNumber, updated)
            }
        }
    }

    fun updateFontScale(scale: Float) {
        viewModelScope.launch {
            settingsRepository.setFontScale(scale)
        }
    }

    init {
        loadSurahList()
        loadEbookPage(currentEbookPage.value)
    }

    fun updateTranslation(id: String) {
        viewModelScope.launch {
            settingsRepository.setTranslationId(id)
            val currentDetail = _surahDetailState.value
            if (currentDetail is SurahDetailUiState.Success) {
                loadSurahDetail(currentDetail.surahNumber)
            }
            loadEbookPage(currentEbookPage.value)
        }
    }

    fun updateReciter(id: String) {
        viewModelScope.launch {
            settingsRepository.setReciterId(id)
            val currentDetail = _surahDetailState.value
            if (currentDetail is SurahDetailUiState.Success) {
                loadSurahDetail(currentDetail.surahNumber)
            }
            loadEbookPage(currentEbookPage.value)
        }
    }

    // --- Quran E-Book (Mushaf 604 Pages) Logic ---

    val juzStartPages = mapOf(
        1 to 1, 2 to 22, 3 to 42, 4 to 62, 5 to 82, 6 to 102, 7 to 121, 8 to 142, 9 to 162, 10 to 182,
        11 to 201, 12 to 222, 13 to 242, 14 to 262, 15 to 282, 16 to 302, 17 to 322, 18 to 342, 19 to 362, 20 to 382,
        21 to 402, 22 to 422, 23 to 442, 24 to 462, 25 to 482, 26 to 502, 27 to 522, 28 to 542, 29 to 562, 30 to 582
    )

    val surahStartPages = mapOf(
        1 to 1, 2 to 2, 3 to 50, 4 to 77, 5 to 106, 6 to 128, 7 to 151, 8 to 177, 9 to 187, 10 to 208,
        11 to 221, 12 to 235, 13 to 249, 14 to 255, 15 to 262, 16 to 267, 17 to 282, 18 to 293, 19 to 305, 20 to 312,
        21 to 322, 22 to 332, 23 to 342, 24 to 350, 25 to 359, 26 to 367, 27 to 377, 28 to 385, 29 to 396, 30 to 404,
        31 to 411, 32 to 415, 33 to 418, 34 to 428, 35 to 434, 36 to 440, 37 to 446, 38 to 453, 39 to 458, 40 to 467,
        41 to 477, 42 to 483, 43 to 489, 44 to 496, 45 to 499, 46 to 502, 47 to 507, 48 to 511, 49 to 515, 50 to 518,
        51 to 520, 52 to 523, 53 to 526, 54 to 528, 55 to 531, 56 to 534, 57 to 537, 58 to 542, 59 to 545, 60 to 549,
        61 to 551, 62 to 553, 63 to 554, 64 to 556, 65 to 558, 66 to 560, 67 to 562, 68 to 564, 69 to 566, 70 to 568,
        71 to 570, 72 to 572, 73 to 574, 74 to 575, 75 to 577, 76 to 578, 77 to 580, 78 to 582, 79 to 583, 80 to 585,
        81 to 586, 82 to 587, 83 to 587, 84 to 589, 85 to 590, 86 to 591, 87 to 591, 88 to 592, 89 to 593, 90 to 594,
        91 to 595, 92 to 595, 93 to 596, 94 to 596, 95 to 597, 96 to 597, 97 to 598, 98 to 598, 99 to 599, 100 to 599,
        101 to 600, 102 to 600, 103 to 601, 104 to 601, 105 to 601, 106 to 602, 107 to 602, 108 to 602, 109 to 603, 110 to 603,
        111 to 603, 112 to 604, 113 to 604, 114 to 604
    )

    fun loadEbookPage(pageNumber: Int) {
        val targetPage = pageNumber.coerceIn(1, 604)
        viewModelScope.launch {
            _ebookPageState.value = EbookPageUiState.Loading
            settingsRepository.setLastEbookPage(targetPage)
            val currentTrans = translationId.value
            val currentReciter = reciterId.value
            val result = quranRepository.getPage(targetPage, currentTrans, currentReciter)
            result.onSuccess { pageModel ->
                _ebookPageState.value = EbookPageUiState.Success(pageModel)
            }.onFailure { err ->
                _ebookPageState.value = EbookPageUiState.Error(err.message ?: "Failed to load page $targetPage")
            }
        }
    }

    fun nextEbookPage() {
        val page = currentEbookPage.value
        if (page < 604) {
            loadEbookPage(page + 1)
        }
    }

    fun previousEbookPage() {
        val page = currentEbookPage.value
        if (page > 1) {
            loadEbookPage(page - 1)
        }
    }

    fun jumpToSurahPage(surahNumber: Int) {
        val page = surahStartPages[surahNumber] ?: 1
        loadEbookPage(page)
    }

    fun jumpToJuzPage(juzNumber: Int) {
        val page = juzStartPages[juzNumber] ?: 1
        loadEbookPage(page)
    }

    fun setEbookTheme(theme: String) {
        viewModelScope.launch {
            settingsRepository.setEbookTheme(theme)
        }
    }

    fun toggleEbookShowTranslation() {
        viewModelScope.launch {
            settingsRepository.setEbookShowTranslation(!ebookShowTranslation.value)
        }
    }

    fun updateEbookFontScale(scale: Float) {
        viewModelScope.launch {
            settingsRepository.setEbookFontScale(scale)
        }
    }

    fun toggleEbookBookmark(ayah: EbookAyahUiModel) {
        viewModelScope.launch {
            quranRepository.toggleEbookBookmark(ayah)
            val current = _ebookPageState.value
            if (current is EbookPageUiState.Success) {
                val updatedAyahs = current.pageModel.ayahs.map { item ->
                    if (item.globalNumber == ayah.globalNumber) {
                        item.copy(isBookmarked = !item.isBookmarked)
                    } else item
                }
                _ebookPageState.value = EbookPageUiState.Success(current.pageModel.copy(ayahs = updatedAyahs))
            }
        }
    }
}
