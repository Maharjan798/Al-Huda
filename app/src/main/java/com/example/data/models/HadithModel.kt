package com.example.data.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RandomHadithResponse(
    @Json(name = "status") val status: Int? = null,
    @Json(name = "data") val data: HadithDataDto? = null
)

@JsonClass(generateAdapter = true)
data class HadithDataDto(
    @Json(name = "header") val header: String? = null,
    @Json(name = "hadith_english") val hadithEnglish: String? = null,
    @Json(name = "hadith_arabic") val hadithArabic: String? = null,
    @Json(name = "header_arabic") val headerArabic: String? = null,
    @Json(name = "refno") val refno: String? = null,
    @Json(name = "bookName") val bookName: String? = null
)

@JsonClass(generateAdapter = true)
data class AlternateHadithResponse(
    @Json(name = "book") val book: String? = null,
    @Json(name = "bookName") val bookName: String? = null,
    @Json(name = "chapterName") val chapterName: String? = null,
    @Json(name = "hadithNumber") val hadithNumber: String? = null,
    @Json(name = "arabic") val arabic: String? = null,
    @Json(name = "english") val english: String? = null,
    @Json(name = "narrator") val narrator: String? = null
)

data class HadithUiModel(
    val id: String,
    val arabicText: String,
    val englishText: String,
    val narrator: String? = null,
    val bookName: String,
    val refNo: String,
    val chapterName: String? = null,
    val grade: String = "Sahih"
)
