package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateSingleWidget(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PrayerWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.example.widget.ACTION_UPDATE_WIDGET"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, PrayerWidgetProvider::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }

        private suspend fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val db = AppDatabase.getInstance(context)
            val cached = db.prayerTimeDao().getPrayerTimesForDate(dateKey)

            val fajr = cached?.fajr ?: "05:12"
            val sunrise = cached?.sunrise ?: "06:34"
            val dhuhr = cached?.dhuhr ?: "12:28"
            val asr = cached?.asr ?: "15:45"
            val maghrib = cached?.maghrib ?: "18:22"
            val isha = cached?.isha ?: "19:42"
            val locationName = cached?.locationName ?: "Makkah, Saudi Arabia"
            val hijriDate = cached?.hijriDate ?: "24 Safar 1448 AH"

            val rawPrayers = listOf(
                Triple("Fajr", "الفجر", fajr),
                Triple("Sunrise", "الشروق", sunrise),
                Triple("Dhuhr", "الظهر", dhuhr),
                Triple("Asr", "العصر", asr),
                Triple("Maghrib", "المغرب", maghrib),
                Triple("Isha", "العشاء", isha)
            )

            val now = Calendar.getInstance()
            val currentMins = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

            var nextPrayer = rawPrayers.first()
            var delayMins = 0

            for ((name, ar, timeStr) in rawPrayers) {
                val parts = timeStr.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                val prayerMins = h * 60 + m

                if (prayerMins > currentMins) {
                    nextPrayer = Triple(name, ar, timeStr)
                    delayMins = prayerMins - currentMins
                    break
                }
            }

            // If all prayers passed today, next prayer is Fajr tomorrow
            if (delayMins == 0 && currentMins >= parseMinutes(rawPrayers.last().third)) {
                val fajrMins = parseMinutes(fajr)
                delayMins = (24 * 60 - currentMins) + fajrMins
                nextPrayer = rawPrayers.first()
            }

            val hours = delayMins / 60
            val mins = delayMins % 60
            val countdownStr = if (hours > 0) {
                String.format(Locale.US, "in %02dh %02dm", hours, mins)
            } else {
                String.format(Locale.US, "in %02dm", mins)
            }

            val formatted12h = format12h(nextPrayer.third)
            val prayerDisplayName = "${nextPrayer.first} (${nextPrayer.second})"

            val views = RemoteViews(context.packageName, R.layout.widget_prayer).apply {
                setTextViewText(R.id.widget_next_prayer_name, prayerDisplayName)
                setTextViewText(R.id.widget_next_prayer_time, formatted12h)
                setTextViewText(R.id.widget_countdown_text, countdownStr)
                setTextViewText(R.id.widget_location_text, locationName)
                setTextViewText(R.id.widget_date_text, hijriDate)

                // Launch main app when tapping widget
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun parseMinutes(time24: String): Int {
            val parts = time24.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return h * 60 + m
        }

        private fun format12h(time24: String): String {
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
}
