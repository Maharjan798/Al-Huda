package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.api.NetworkClient
import com.example.data.db.AppDatabase
import com.example.data.repository.PrayerRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class PrayerScheduleRefreshWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(context)
            val repository = PrayerRepository(NetworkClient.adhanApiService, db.prayerTimeDao())
            val settingsRepository = SettingsRepository(context)

            val cityName = settingsRepository.cityName.first()
            val method = settingsRepository.calcMethod.first()
            val school = settingsRepository.juristicSchool.first()

            val schedule = repository.getDailyPrayerSchedule(
                latitude = 21.4225,
                longitude = 39.8262,
                cityName = cityName,
                method = method,
                school = school
            )

            PrayerNotificationScheduler.schedulePrayerNotifications(context, schedule)
            com.example.widget.PrayerWidgetProvider.updateAllWidgets(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
