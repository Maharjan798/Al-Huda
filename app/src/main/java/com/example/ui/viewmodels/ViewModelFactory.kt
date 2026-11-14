package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.api.NetworkClient
import com.example.data.db.AppDatabase
import com.example.data.repository.AzkarRepository
import com.example.data.repository.HadithRepository
import com.example.data.repository.HistoryRepository
import com.example.data.repository.PrayerRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.SettingsRepository

class AppViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    private val db by lazy { AppDatabase.getInstance(context) }
    private val settingsRepository by lazy { SettingsRepository(context) }
    val locationService by lazy { com.example.location.LocationService(context) }

    val quranRepository by lazy {
        QuranRepository(NetworkClient.quranApiService, db.quranBookmarkDao())
    }

    val prayerRepository by lazy {
        PrayerRepository(NetworkClient.adhanApiService, db.prayerTimeDao())
    }

    val historyRepository by lazy { HistoryRepository() }
    val azkarRepository by lazy { AzkarRepository() }
    val hadithRepository by lazy { HadithRepository(NetworkClient.hadithApiService) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(QuranViewModel::class.java) -> {
                QuranViewModel(quranRepository, settingsRepository) as T
            }
            modelClass.isAssignableFrom(PrayerViewModel::class.java) -> {
                PrayerViewModel(prayerRepository, settingsRepository, locationService, context.applicationContext) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(historyRepository) as T
            }
            modelClass.isAssignableFrom(AzkarViewModel::class.java) -> {
                AzkarViewModel(azkarRepository) as T
            }
            modelClass.isAssignableFrom(HadithViewModel::class.java) -> {
                HadithViewModel(hadithRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
