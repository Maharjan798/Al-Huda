package com.example.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.models.DailyPrayerSchedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object PrayerNotificationScheduler {

    private const val PERIODIC_REFRESH_WORK_NAME = "prayer_schedule_periodic_refresh"

    fun schedulePrayerNotifications(context: Context, schedule: DailyPrayerSchedule) {
        val workManager = WorkManager.getInstance(context)

        val prayers = listOf(
            "Fajr" to schedule.fajr,
            "Dhuhr" to schedule.dhuhr,
            "Asr" to schedule.asr,
            "Maghrib" to schedule.maghrib,
            "Isha" to schedule.isha
        )

        val now = Calendar.getInstance()

        prayers.forEach { (prayerName, rawTime) ->
            val timeParts = rawTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return@forEach
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: return@forEach

            val prayerCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If prayer time today has already passed, schedule for tomorrow
            if (prayerCalendar.before(now)) {
                prayerCalendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val delayMillis = prayerCalendar.timeInMillis - System.currentTimeMillis()
            if (delayMillis <= 0) return@forEach

            val formattedTime = formatTime12h(rawTime)

            val inputData = Data.Builder()
                .putString(PrayerNotificationWorker.KEY_PRAYER_NAME, prayerName)
                .putString(PrayerNotificationWorker.KEY_PRAYER_TIME, formattedTime)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("adhan_alert")
                .build()

            workManager.enqueueUniqueWork(
                "prayer_alert_$prayerName",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        // Schedule background periodic refresh worker to keep prayer times updated
        schedulePeriodicRefreshWork(context)
    }

    fun schedulePeriodicRefreshWork(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val periodicRequest = PeriodicWorkRequestBuilder<PrayerScheduleRefreshWorker>(
            12, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        ).addTag("prayer_periodic_refresh").build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_REFRESH_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    fun cancelAllPrayerNotifications(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("adhan_alert")
    }

    private fun formatTime12h(time24: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
            val sdf12 = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf24.parse(time24)
            if (date != null) sdf12.format(date) else time24
        } catch (e: Exception) {
            time24
        }
    }
}
