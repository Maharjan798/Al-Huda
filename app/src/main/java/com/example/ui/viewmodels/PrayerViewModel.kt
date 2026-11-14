package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.DailyPrayerSchedule
import com.example.data.models.PrayerItem
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SettingsRepository
import com.example.location.LocationService
import com.example.worker.PrayerNotificationScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class PrayerUiState {
    object Loading : PrayerUiState()
    data class Success(
        val schedule: DailyPrayerSchedule,
        val prayerItems: List<PrayerItem>,
        val nextPrayerName: String,
        val countdownTimeFormatted: String
    ) : PrayerUiState()
    data class Error(val message: String) : PrayerUiState()
}

class PrayerViewModel(
    private val prayerRepository: PrayerRepository,
    private val settingsRepository: SettingsRepository,
    private val locationService: LocationService? = null,
    private val appContext: Context? = null
) : ViewModel() {

    private val _prayerUiState = MutableStateFlow<PrayerUiState>(PrayerUiState.Loading)
    val prayerUiState: StateFlow<PrayerUiState> = _prayerUiState.asStateFlow()

    val currentCalcMethod: StateFlow<Int> = settingsRepository.calcMethod.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2
    )

    val currentJuristicSchool: StateFlow<Int> = settingsRepository.juristicSchool.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val currentCityName: StateFlow<String> = settingsRepository.cityName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Makkah, Saudi Arabia"
    )

    val isCalendarSyncEnabled: StateFlow<Boolean> = settingsRepository.calendarSyncEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private var currentLatitude = 21.4225
    private var currentLongitude = 39.8262

    init {
        if (locationService?.hasLocationPermission() == true) {
            refreshWithGPSLocation()
        } else {
            loadPrayerTimes()
        }
        startCountdownTimer()
    }

    fun refreshWithGPSLocation(onComplete: ((Boolean) -> Unit)? = null) {
        val service = locationService ?: run {
            onComplete?.invoke(false)
            return
        }
        viewModelScope.launch {
            _prayerUiState.value = PrayerUiState.Loading
            val result = service.getCurrentUserLocation()
            if (result.isSuccess) {
                currentLatitude = result.latitude
                currentLongitude = result.longitude
                settingsRepository.setCityName(result.cityName)
            }
            loadPrayerTimes()
            onComplete?.invoke(result.isSuccess)
        }
    }

    fun loadPrayerTimes() {
        viewModelScope.launch {
            _prayerUiState.value = PrayerUiState.Loading
            val method = currentCalcMethod.value
            val school = currentJuristicSchool.value
            val city = currentCityName.value

            val schedule = prayerRepository.getDailyPrayerSchedule(
                latitude = currentLatitude,
                longitude = currentLongitude,
                cityName = city,
                method = method,
                school = school
            )

            val items = prayerRepository.parsePrayerItems(schedule)
            val nextItem = items.find { it.isNext } ?: items.first()
            val countdown = calculateCountdownString(nextItem.time)

            _prayerUiState.value = PrayerUiState.Success(
                schedule = schedule,
                prayerItems = items,
                nextPrayerName = nextItem.name,
                countdownTimeFormatted = countdown
            )

            appContext?.let { ctx ->
                PrayerNotificationScheduler.schedulePrayerNotifications(ctx, schedule)
                com.example.widget.PrayerWidgetProvider.updateAllWidgets(ctx)
                if (isCalendarSyncEnabled.value) {
                    com.example.calendar.CalendarSyncManager.syncPrayerTimesToCalendar(ctx, schedule)
                }
            }
        }
    }

    fun toggleCalendarSync(enabled: Boolean, context: Context, onResult: ((com.example.calendar.CalendarSyncResult) -> Unit)? = null) {
        viewModelScope.launch {
            settingsRepository.setCalendarSyncEnabled(enabled)
            if (enabled) {
                val state = _prayerUiState.value
                if (state is PrayerUiState.Success) {
                    val result = com.example.calendar.CalendarSyncManager.syncPrayerTimesToCalendar(context, state.schedule)
                    onResult?.invoke(result)
                } else {
                    onResult?.invoke(com.example.calendar.CalendarSyncResult.Error("Prayer schedule not loaded yet"))
                }
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double, cityName: String) {
        currentLatitude = lat
        currentLongitude = lng
        viewModelScope.launch {
            settingsRepository.setCityName(cityName)
            loadPrayerTimes()
        }
    }

    fun updateCalcMethod(method: Int) {
        viewModelScope.launch {
            settingsRepository.setCalcMethod(method)
            loadPrayerTimes()
        }
    }

    fun updateJuristicSchool(school: Int) {
        viewModelScope.launch {
            settingsRepository.setJuristicSchool(school)
            loadPrayerTimes()
        }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val currentState = _prayerUiState.value
                if (currentState is PrayerUiState.Success) {
                    val nextItem = currentState.prayerItems.find { it.isNext } ?: currentState.prayerItems.first()
                    val newCountdown = calculateCountdownString(nextItem.time)
                    _prayerUiState.value = currentState.copy(countdownTimeFormatted = newCountdown)
                }
            }
        }
    }

    private fun calculateCountdownString(targetTime12h: String): String {
        return try {
            val now = Calendar.getInstance()
            val targetCal = Calendar.getInstance()

            val parts = targetTime12h.split(" ")
            val timeParts = parts[0].split(":")
            var hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            val amPm = parts.getOrNull(1)?.uppercase() ?: "AM"

            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0

            targetCal.set(Calendar.HOUR_OF_DAY, hour)
            targetCal.set(Calendar.MINUTE, minute)
            targetCal.set(Calendar.SECOND, 0)

            if (targetCal.before(now)) {
                targetCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            val diffMs = targetCal.timeInMillis - now.timeInMillis
            val diffSecs = diffMs / 1000
            val hours = diffSecs / 3600
            val mins = (diffSecs % 3600) / 60
            val secs = diffSecs % 60

            String.format("%02d:%02d:%02d", hours, mins, secs)
        } catch (e: Exception) {
            "00:00:00"
        }
    }
}
