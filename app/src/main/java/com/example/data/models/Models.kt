package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- AlQuran API Models ---

@JsonClass(generateAdapter = true)
data class AlQuranBaseResponse<T>(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: T?
)

@JsonClass(generateAdapter = true)
data class SurahDto(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "revelationType") val revelationType: String
)

@JsonClass(generateAdapter = true)
data class SurahDetailDto(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "revelationType") val revelationType: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int,
    @Json(name = "ayahs") val ayahs: List<AyahDto>?
)

@JsonClass(generateAdapter = true)
data class AyahDto(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "juz") val juz: Int,
    @Json(name = "manzil") val manzil: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "ruku") val ruku: Int,
    @Json(name = "hizbQuarter") val hizbQuarter: Int,
    @Json(name = "audio") val audio: String? = null,
    @Json(name = "audioSecondary") val audioSecondary: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class EditionDto(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "language") val language: String,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "format") val format: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "direction") val direction: String? = null
)

@JsonClass(generateAdapter = true)
data class PageSurahSummaryDto(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "englishNameTranslation") val englishNameTranslation: String,
    @Json(name = "numberOfAyahs") val numberOfAyahs: Int = 0,
    @Json(name = "revelationType") val revelationType: String = ""
)

@JsonClass(generateAdapter = true)
data class PageAyahDto(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "surah") val surah: PageSurahSummaryDto? = null,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "juz") val juz: Int,
    @Json(name = "manzil") val manzil: Int = 1,
    @Json(name = "page") val page: Int,
    @Json(name = "ruku") val ruku: Int = 1,
    @Json(name = "hizbQuarter") val hizbQuarter: Int = 1,
    @Json(name = "audio") val audio: String? = null
)

@JsonClass(generateAdapter = true)
data class QuranPageDto(
    @Json(name = "number") val number: Int,
    @Json(name = "ayahs") val ayahs: List<PageAyahDto>?,
    @Json(name = "edition") val edition: EditionDto? = null
)

@JsonClass(generateAdapter = true)
data class CompleteQuranDto(
    @Json(name = "surahs") val surahs: List<SurahDetailDto>?,
    @Json(name = "edition") val edition: EditionDto? = null
)

@JsonClass(generateAdapter = true)
data class SearchMatchDto(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "surah") val surah: PageSurahSummaryDto? = null,
    @Json(name = "edition") val edition: EditionDto? = null
)

@JsonClass(generateAdapter = true)
data class QuranSearchDataDto(
    @Json(name = "count") val count: Int = 0,
    @Json(name = "matches") val matches: List<SearchMatchDto>? = null
)

// Combined UI Ayah model
data class AyahUiModel(
    val numberInSurah: Int,
    val globalAyahNumber: Int,
    val arabicText: String,
    val translationText: String,
    val tafsirText: String? = null,
    val audioUrl: String? = null,
    val surahNumber: Int,
    val surahName: String,
    val isBookmarked: Boolean = false
)

// UI models for E-Book (Page-by-page 1 to 604)
data class EbookAyahUiModel(
    val globalNumber: Int,
    val numberInSurah: Int,
    val surahNumber: Int,
    val surahNameAr: String,
    val surahNameEn: String,
    val arabicText: String,
    val translationText: String,
    val audioUrl: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val isNewSurahStart: Boolean = false,
    val isBookmarked: Boolean = false
)

data class QuranEbookPageUiModel(
    val pageNumber: Int,
    val juzNumber: Int,
    val primarySurahNumber: Int,
    val primarySurahNameAr: String,
    val primarySurahNameEn: String,
    val ayahs: List<EbookAyahUiModel>
)

// Reciter options
data class ReciterInfo(
    val id: String,
    val name: String,
    val subtext: String,
    val identifier: String // e.g. "ar.alafasy" or "ar.abdulbasitmurattal"
)

// --- AlAdhan API Models ---

@JsonClass(generateAdapter = true)
data class AlAdhanTimingsResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: AlAdhanDataDto?
)

@JsonClass(generateAdapter = true)
data class AlAdhanCalendarResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<AlAdhanDataDto>?
)

@JsonClass(generateAdapter = true)
data class AlAdhanDataDto(
    @Json(name = "timings") val timings: TimingsDto,
    @Json(name = "date") val date: DateContainerDto,
    @Json(name = "meta") val meta: MetaDto?
)

@JsonClass(generateAdapter = true)
data class TimingsDto(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String,
    @Json(name = "Midnight") val midnight: String
)

@JsonClass(generateAdapter = true)
data class DateContainerDto(
    @Json(name = "readable") val readable: String,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "hijri") val hijri: HijriDateDto?,
    @Json(name = "gregorian") val gregorian: GregorianDateDto?
)

@JsonClass(generateAdapter = true)
data class HijriDateDto(
    @Json(name = "date") val date: String,
    @Json(name = "format") val format: String?,
    @Json(name = "day") val day: String,
    @Json(name = "weekday") val weekday: WeekdayDto?,
    @Json(name = "month") val month: MonthDto?,
    @Json(name = "year") val year: String,
    @Json(name = "designation") val designation: DesignationDto?
)

@JsonClass(generateAdapter = true)
data class GregorianDateDto(
    @Json(name = "date") val date: String,
    @Json(name = "day") val day: String,
    @Json(name = "weekday") val weekday: WeekdayDto?,
    @Json(name = "month") val month: MonthDto?,
    @Json(name = "year") val year: String
)

@JsonClass(generateAdapter = true)
data class WeekdayDto(
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class MonthDto(
    @Json(name = "number") val number: Int,
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class DesignationDto(
    @Json(name = "abbreviated") val abbreviated: String,
    @Json(name = "expanded") val expanded: String
)

@JsonClass(generateAdapter = true)
data class MetaDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "timezone") val timezone: String,
    @Json(name = "method") val method: MethodDto?
)

@JsonClass(generateAdapter = true)
data class MethodDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

// UI Prayer Model
data class DailyPrayerSchedule(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val dateReadable: String,
    val hijriDateFormatted: String,
    val gregorianDateFormatted: String,
    val locationName: String
)

data class PrayerItem(
    val name: String,
    val nameArabic: String,
    val time: String,
    val isNext: Boolean,
    val isPassed: Boolean
)

// --- Room Entities ---

@Entity(tableName = "quran_bookmarks")
data class QuranBookmarkEntity(
    @PrimaryKey val id: String, // "surahNum_ayahNum"
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val arabicText: String,
    val translationText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "last_read_position")
data class LastReadPositionEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_prayer_times")
data class CachedPrayerTimesEntity(
    @PrimaryKey val dateKey: String, // e.g. "2026-08-11"
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val dateReadable: String,
    val hijriDate: String,
    val locationName: String
)

// --- Islamic History Models ---

data class HistoricalPhase(
    val id: String,
    val phaseNumber: Int,
    val title: String,
    val titleArabic: String,
    val eraPeriod: String, // e.g. "570–610 CE"
    val summary: String,
    val keyEventsCount: Int,
    val accentColorHex: String = "#0A3A2A"
)

data class HistoricalArticle(
    val id: String,
    val phaseId: String,
    val title: String,
    val titleArabic: String,
    val dateOrYear: String,
    val location: String,
    val overview: String,
    val detailedContent: String,
    val keyTakeaways: List<String>,
    val mapRegionUrl: String? = null
)

// --- Azkar Models ---

data class AzkarItem(
    val id: Int,
    val category: String, // "Morning", "Evening", "After Prayer", "Sleep"
    val arabicText: String,
    val translation: String,
    val reference: String,
    val targetCount: Int,
    var currentCount: Int = 0
)

data class TranslationInfo(
    val id: String,
    val languageName: String,
    val translatorName: String,
    val flagEmoji: String
)
