package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.data.models.HistoricalArticle
import com.example.data.models.HistoricalPhase
import com.example.data.repository.HistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel(
    val historyRepository: HistoryRepository
) : ViewModel() {

    val phases: List<HistoricalPhase> = historyRepository.phases

    private val _selectedPhaseId = MutableStateFlow<String?>("phase3")
    val selectedPhaseId: StateFlow<String?> = _selectedPhaseId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredArticles = MutableStateFlow<List<HistoricalArticle>>(historyRepository.articles)
    val filteredArticles: StateFlow<List<HistoricalArticle>> = _filteredArticles.asStateFlow()

    private val _selectedArticle = MutableStateFlow<HistoricalArticle?>(null)
    val selectedArticle: StateFlow<HistoricalArticle?> = _selectedArticle.asStateFlow()

    init {
        updateFilteredArticles()
    }

    fun selectPhase(phaseId: String?) {
        _selectedPhaseId.value = phaseId
        updateFilteredArticles()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        updateFilteredArticles()
    }

    fun selectArticle(article: HistoricalArticle?) {
        _selectedArticle.value = article
    }

    private fun updateFilteredArticles() {
        val phaseFilter = _selectedPhaseId.value
        val query = _searchQuery.value.trim().lowercase()

        val list = historyRepository.articles.filter { article ->
            val matchesPhase = phaseFilter == null || article.phaseId == phaseFilter
            val matchesQuery = query.isEmpty() ||
                    article.title.lowercase().contains(query) ||
                    article.overview.lowercase().contains(query) ||
                    article.detailedContent.lowercase().contains(query) ||
                    article.location.lowercase().contains(query)
            matchesPhase && matchesQuery
        }
        _filteredArticles.value = list
    }
}
