package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.data.models.AzkarItem
import com.example.data.repository.AzkarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AzkarViewModel(
    val azkarRepository: AzkarRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Morning")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _azkarList = MutableStateFlow<List<AzkarItem>>(emptyList())
    val azkarList: StateFlow<List<AzkarItem>> = _azkarList.asStateFlow()

    // Tasbeeh Counter state
    private val _tasbeehCount = MutableStateFlow(0)
    val tasbeehCount: StateFlow<Int> = _tasbeehCount.asStateFlow()

    private val _tasbeehTarget = MutableStateFlow(33)
    val tasbeehTarget: StateFlow<Int> = _tasbeehTarget.asStateFlow()

    val categories = listOf("Morning", "Evening", "After Prayer", "Sleep")

    init {
        loadCategory(_selectedCategory.value)
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        loadCategory(category)
    }

    private fun loadCategory(category: String) {
        val items = azkarRepository.getAzkarByCategory(category).map { it.copy() }
        _azkarList.value = items
    }

    fun incrementAzkarCount(itemId: Int) {
        val current = _azkarList.value
        val updated = current.map { item ->
            if (item.id == itemId) {
                val newCount = item.currentCount + 1
                item.copy(currentCount = if (newCount > item.targetCount) item.targetCount else newCount)
            } else item
        }
        _azkarList.value = updated
    }

    fun resetAzkarCount(itemId: Int) {
        val current = _azkarList.value
        val updated = current.map { item ->
            if (item.id == itemId) item.copy(currentCount = 0) else item
        }
        _azkarList.value = updated
    }

    fun incrementTasbeeh() {
        val next = _tasbeehCount.value + 1
        _tasbeehCount.value = next
    }

    fun resetTasbeeh() {
        _tasbeehCount.value = 0
    }

    fun setTasbeehTarget(target: Int) {
        _tasbeehTarget.value = target
        _tasbeehCount.value = 0
    }
}
