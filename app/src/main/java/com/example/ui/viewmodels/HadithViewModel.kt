package com.example.ui.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.HadithUiModel
import com.example.data.repository.HadithRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class HadithUiState {
    object Loading : HadithUiState()
    data class Success(val hadith: HadithUiModel, val isRefreshing: Boolean = false) : HadithUiState()
    data class Error(val message: String) : HadithUiState()
}

class HadithViewModel(
    val hadithRepository: HadithRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HadithUiState>(HadithUiState.Loading)
    val uiState: StateFlow<HadithUiState> = _uiState.asStateFlow()

    init {
        loadHadithOfTheDay()
    }

    fun loadHadithOfTheDay() {
        viewModelScope.launch {
            _uiState.value = HadithUiState.Loading
            try {
                val hadith = hadithRepository.getHadithOfTheDay(forceRandom = false)
                _uiState.value = HadithUiState.Success(hadith)
            } catch (e: Exception) {
                _uiState.value = HadithUiState.Error(e.localizedMessage ?: "Failed to load Hadith of the Day")
            }
        }
    }

    fun refreshNextHadith() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HadithUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            } else {
                _uiState.value = HadithUiState.Loading
            }

            try {
                val newHadith = hadithRepository.getHadithOfTheDay(forceRandom = true)
                _uiState.value = HadithUiState.Success(newHadith, isRefreshing = false)
            } catch (e: Exception) {
                val fallback = hadithRepository.getHadithOfTheDay(forceRandom = false)
                _uiState.value = HadithUiState.Success(fallback, isRefreshing = false)
            }
        }
    }

    fun copyHadithToClipboard(context: Context, hadith: HadithUiModel) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textToCopy = """
            " ${hadith.englishText} "
            
            ${hadith.arabicText}
            
            — ${hadith.narrator ?: "Hadith"}, ${hadith.bookName} (${hadith.refNo})
            Shared via Al-Huda Islamic App
        """.trimIndent()

        val clip = ClipData.newPlainText("Hadith of the Day", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Hadith copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    fun shareHadith(context: Context, hadith: HadithUiModel) {
        val shareText = """
            ✨ Hadith of the Day ✨
            
            ${hadith.arabicText}
            
            " ${hadith.englishText} "
            
            📚 ${hadith.bookName} (${hadith.refNo})
            ${hadith.narrator ?: ""}
            
            Shared via Al-Huda
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Hadith of the Day")
        context.startActivity(shareIntent)
    }
}
