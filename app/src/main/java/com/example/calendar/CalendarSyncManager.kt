package com.example.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.example.data.models.DailyPrayerSchedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

sealed class CalendarSyncResult {
    data class Success(val count: Int, val calendarName: String) : CalendarSyncResult()
    object PermissionDenied : CalendarSyncResult()
    data class Error(val message: String) : CalendarSyncResult()
}

object CalendarSyncManager {

    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    fun hasCalendarPermission(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun syncPrayerTimesToCalendar(
        context: Context,
        schedule: DailyPrayerSchedule
    ): CalendarSyncResult {
        if (!hasCalendarPermission(context)) {
            return CalendarSyncResult.PermissionDenied
        }

        return try {
            val resolver = context.contentResolver

            // 1. Find target calendar ID
            val (calendarId, calName) = getPrimaryCalendarId(context)
                ?: return CalendarSyncResult.Error("No active system calendar found on device.")

            // 2. Clear previous Al-Huda prayer events for today
            clearExistingPrayerEvents(context, calendarId)

            // 3. Prepare prayer list to sync
            val prayersToSync = listOf(
                Triple("Fajr", "الفجر", schedule.fajr),
                Triple("Dhuhr", "الظهر", schedule.dhuhr),
                Triple("Asr", "العصر", schedule.asr),
                Triple("Maghrib", "المغرب", schedule.maghrib),
                Triple("Isha", "العشاء", schedule.isha)
            )

            val todayDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            var insertedCount = 0

            for ((enName, arName, time24) in prayersToSync) {
                val startMillis = parseTimeToMillis(todayDateKey, time24) ?: continue
                val endMillis = startMillis + (20 * 60 * 1000) // 20 minutes duration

                val eventValues = ContentValues().apply {
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.TITLE, "$enName Prayer ($arName)")
                    put(CalendarContract.Events.DESCRIPTION, "Daily prayer time synced from Al-Huda Islamic Companion App.")
                    put(CalendarContract.Events.EVENT_LOCATION, schedule.locationName)
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                    put(CalendarContract.Events.HAS_ALARM, 1)
                    put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                }

                val eventUri: Uri? = resolver.insert(CalendarContract.Events.CONTENT_URI, eventValues)
                if (eventUri != null) {
                    insertedCount++
                    val eventId = ContentUris.parseId(eventUri)

                    // Add 10-minute reminder alert
                    val reminderValues = ContentValues().apply {
                        put(CalendarContract.Reminders.EVENT_ID, eventId)
                        put(CalendarContract.Reminders.MINUTES, 10)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    }
                    resolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                }
            }

            CalendarSyncResult.Success(insertedCount, calName)
        } catch (e: Exception) {
            CalendarSyncResult.Error(e.localizedMessage ?: "Failed to sync calendar")
        }
    }

    private fun getPrimaryCalendarId(context: Context): Pair<Long, String>? {
        val resolver = context.contentResolver
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.VISIBLE
        )

        val cursor = resolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.IS_PRIMARY} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val nameIndex = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex) ?: "System Calendar"
                return Pair(id, name)
            }
        }

        // Fallback to first available calendar ID
        val fallbackCursor = resolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME),
            null,
            null,
            null
        )
        fallbackCursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(0)
                val name = it.getString(1) ?: "System Calendar"
                return Pair(id, name)
            }
        }

        return null
    }

    private fun clearExistingPrayerEvents(context: Context, calendarId: Long) {
        try {
            val resolver = context.contentResolver
            val selection = "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DESCRIPTION} LIKE ?"
            val selectionArgs = arrayOf(calendarId.toString(), "%Al-Huda%")
            resolver.delete(CalendarContract.Events.CONTENT_URI, selection, selectionArgs)
        } catch (e: Exception) {
            // Ignore cleanup failure
        }
    }

    private fun parseTimeToMillis(dateKeyYmd: String, time24: String): Long? {
        return try {
            val fullStr = "$dateKeyYmd $time24"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            sdf.parse(fullStr)?.time
        } catch (e: Exception) {
            null
        }
    }
}
