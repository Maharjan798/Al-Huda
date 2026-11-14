package com.example.data.repository

import com.example.data.api.AlAdhanApiService
import com.example.data.db.PrayerTimeDao
import com.example.data.models.CachedPrayerTimesEntity
import com.example.data.models.DailyPrayerSchedule
import com.example.data.models.PrayerItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class PrayerRepository(
    private val apiService: AlAdhanApiService,
    private val prayerTimeDao: PrayerTimeDao
) {

    val calculationMethods = listOf(
        Pair(2, "ISNA - Islamic Society of North America"),
        Pair(3, "MWL - Muslim World League"),
        Pair(4, "Umm Al-Qura - Makkah"),
        Pair(5, "Egyptian General Authority of Survey"),
        Pair(1, "University of Islamic Sciences, Karachi"),
        Pair(8, "Dubai Islamic Affairs & Charitable Activities")
    )

    suspend fun getDailyPrayerSchedule(
        latitude: Double = 21.4225, // Default Makkah
        longitude: Double = 39.8262,
        cityName: String = "Makkah, Saudi Arabia",
        method: Int = 2,
        school: Int = 0
    ): DailyPrayerSchedule {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // Try local Room DB first
        val cached = prayerTimeDao.getPrayerTimesForDate(dateKey)
        if (cached != null) {
            return DailyPrayerSchedule(
                fajr = cached.fajr,
                sunrise = cached.sunrise,
                dhuhr = cached.dhuhr,
                asr = cached.asr,
                maghrib = cached.maghrib,
                isha = cached.isha,
                dateReadable = cached.dateReadable,
                hijriDateFormatted = cached.hijriDate,
                gregorianDateFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()),
                locationName = cached.locationName
            )
        }

        return try {
            val response = apiService.getTimingsByCoordinates(
                date = dateKey,
                latitude = latitude,
                longitude = longitude,
                method = method,
                school = school
            )

            if (response.isSuccessful && response.body()?.data != null) {
                val data = response.body()!!.data!!
                val t = data.timings
                val hijri = data.date.hijri
                val hijriFormatted = if (hijri != null) {
                    "${hijri.day} ${hijri.month?.en ?: ""} ${hijri.year} AH"
                } else "1448 AH"

                val cleanFajr = cleanTime(t.fajr)
                val cleanSunrise = cleanTime(t.sunrise)
                val cleanDhuhr = cleanTime(t.dhuhr)
                val cleanAsr = cleanTime(t.asr)
                val cleanMaghrib = cleanTime(t.maghrib)
                val cleanIsha = cleanTime(t.isha)

                val entity = CachedPrayerTimesEntity(
                    dateKey = dateKey,
                    fajr = cleanFajr,
                    sunrise = cleanSunrise,
                    dhuhr = cleanDhuhr,
                    asr = cleanAsr,
                    maghrib = cleanMaghrib,
                    isha = cleanIsha,
                    dateReadable = data.date.readable,
                    hijriDate = hijriFormatted,
                    locationName = cityName
                )
                prayerTimeDao.insertPrayerTimes(entity)

                DailyPrayerSchedule(
                    fajr = cleanFajr,
                    sunrise = cleanSunrise,
                    dhuhr = cleanDhuhr,
                    asr = cleanAsr,
                    maghrib = cleanMaghrib,
                    isha = cleanIsha,
                    dateReadable = data.date.readable,
                    hijriDateFormatted = hijriFormatted,
                    gregorianDateFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()),
                    locationName = cityName
                )
            } else {
                getFallbackSchedule(cityName)
            }
        } catch (e: Exception) {
            getFallbackSchedule(cityName)
        }
    }

    suspend fun getQiblaAngle(latitude: Double, longitude: Double): Double {
        return try {
            val response = apiService.getQiblaDirection(latitude, longitude)
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!.direction
            } else {
                calculateQiblaOffline(latitude, longitude)
            }
        } catch (e: Exception) {
            calculateQiblaOffline(latitude, longitude)
        }
    }

    // Great Circle formula to calculate Qibla heading towards Kaaba (21.4225 N, 39.8262 E)
    private fun calculateQiblaOffline(lat: Double, lng: Double): Double {
        val makkahLat = Math.toRadians(21.4225)
        val makkahLng = Math.toRadians(39.8262)
        val userLat = Math.toRadians(lat)
        val userLng = Math.toRadians(lng)

        val dLng = makkahLng - userLng
        val y = sin(dLng)
        val x = cos(userLat) * tan(makkahLat) - sin(userLat) * cos(dLng)
        var qibla = Math.toDegrees(atan2(y, x))
        if (qibla < 0) {
            qibla += 360.0
        }
        return qibla
    }

    private fun cleanTime(raw: String): String {
        return raw.split(" ").firstOrNull() ?: raw
    }

    private fun getFallbackSchedule(cityName: String): DailyPrayerSchedule {
        return DailyPrayerSchedule(
            fajr = "05:12",
            sunrise = "06:34",
            dhuhr = "12:28",
            asr = "15:45",
            maghrib = "18:22",
            isha = "19:42",
            dateReadable = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
            hijriDateFormatted = "24 Safar 1448 AH",
            gregorianDateFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date()),
            locationName = cityName
        )
    }

    fun parsePrayerItems(schedule: DailyPrayerSchedule): List<PrayerItem> {
        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val rawList = listOf(
            Triple("Fajr", "الفجر", schedule.fajr),
            Triple("Sunrise", "الشروق", schedule.sunrise),
            Triple("Dhuhr", "الظهر", schedule.dhuhr),
            Triple("Asr", "العصر", schedule.asr),
            Triple("Maghrib", "المغرب", schedule.maghrib),
            Triple("Isha", "العشاء", schedule.isha)
        )

        val prayerMinutes = rawList.map { (name, ar, timeStr) ->
            val parts = timeStr.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val totalMins = h * 60 + m
            Triple(name, ar, totalMins)
        }

        var nextIndex = -1
        for (i in prayerMinutes.indices) {
            if (prayerMinutes[i].third > currentMinutes) {
                nextIndex = i
                break
            }
        }
        if (nextIndex == -1) nextIndex = 0 // After Isha -> Fajr next day

        return rawList.mapIndexed { index, (en, ar, timeStr) ->
            val isPassed = if (nextIndex == 0 && currentMinutes > prayerMinutes.last().third) {
                true
            } else {
                index < nextIndex
            }
            PrayerItem(
                name = en,
                nameArabic = ar,
                time = formatTime12h(timeStr),
                isNext = index == nextIndex,
                isPassed = isPassed
            )
        }
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
