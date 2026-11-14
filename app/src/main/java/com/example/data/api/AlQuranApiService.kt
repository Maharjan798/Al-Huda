package com.example.data.api

import com.example.data.models.AlQuranBaseResponse
import com.example.data.models.CompleteQuranDto
import com.example.data.models.EditionDto
import com.example.data.models.PageAyahDto
import com.example.data.models.QuranPageDto
import com.example.data.models.QuranSearchDataDto
import com.example.data.models.SurahDetailDto
import com.example.data.models.SurahDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AlQuranApiService {

    // Fetch list of all 114 Surahs
    @GET("surah")
    suspend fun getSurahList(): Response<AlQuranBaseResponse<List<SurahDto>>>

    // Fetch single Surah metadata
    @GET("surah/{surahNumber}")
    suspend fun getSurah(
        @Path("surahNumber") surahNumber: Int
    ): Response<AlQuranBaseResponse<SurahDto>>

    // Fetch Surah with single specific edition (e.g. "quran-uthmani", "hi.hindi", "en.sahih")
    @GET("surah/{surahNumber}/{edition}")
    suspend fun getSurahByEdition(
        @Path("surahNumber") surahNumber: Int,
        @Path("edition") edition: String
    ): Response<AlQuranBaseResponse<SurahDetailDto>>

    // Fetch Surah with multiple editions (e.g. "quran-uthmani,hi.hindi")
    @GET("surah/{surahNumber}/editions/{editions}")
    suspend fun getSurahEditions(
        @Path("surahNumber") surahNumber: Int,
        @Path("editions") editions: String = "quran-uthmani,en.sahih"
    ): Response<AlQuranBaseResponse<List<SurahDetailDto>>>

    // Fetch Surah Audio for a specific Reciter (e.g. "ar.alafasy")
    @GET("surah/{surahNumber}/{reciterId}")
    suspend fun getSurahAudio(
        @Path("surahNumber") surahNumber: Int,
        @Path("reciterId") reciterId: String = "ar.alafasy"
    ): Response<AlQuranBaseResponse<SurahDetailDto>>

    // Fetch specific Page (1 to 604) for complete Quran E-Book in requested edition
    @GET("page/{pageNumber}/{edition}")
    suspend fun getPage(
        @Path("pageNumber") pageNumber: Int,
        @Path("edition") edition: String = "quran-uthmani"
    ): Response<AlQuranBaseResponse<QuranPageDto>>

    // Fetch specific Juz (1 to 30) in requested edition
    @GET("juz/{juzNumber}/{edition}")
    suspend fun getJuz(
        @Path("juzNumber") juzNumber: Int,
        @Path("edition") edition: String = "quran-uthmani"
    ): Response<AlQuranBaseResponse<QuranPageDto>>

    // Fetch complete Quran edition in one call (e.g. "quran-uthmani" or "hi.hindi")
    @GET("quran/{edition}")
    suspend fun getCompleteQuran(
        @Path("edition") edition: String = "quran-uthmani"
    ): Response<AlQuranBaseResponse<CompleteQuranDto>>

    // Fetch random Ayah
    @GET("ayah/random/{edition}")
    suspend fun getRandomAyah(
        @Path("edition") edition: String = "quran-uthmani"
    ): Response<AlQuranBaseResponse<PageAyahDto>>

    // Full text search in Quran
    @GET("search/{word}/{surah}/{language}")
    suspend fun searchQuran(
        @Path("word") word: String,
        @Path("surah") surah: String = "all",
        @Path("language") language: String = "en.sahih"
    ): Response<AlQuranBaseResponse<QuranSearchDataDto>>

    // List of available editions
    @GET("edition")
    suspend fun getEditions(
        @Query("type") type: String? = null,
        @Query("format") format: String? = null,
        @Query("language") language: String? = null
    ): Response<AlQuranBaseResponse<List<EditionDto>>>
}
