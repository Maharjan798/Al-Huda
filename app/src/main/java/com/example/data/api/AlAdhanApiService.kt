package com.example.data.api

import com.example.data.models.AlAdhanCalendarResponse
import com.example.data.models.AlAdhanTimingsResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class QiblaResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: QiblaDataDto?
)

@JsonClass(generateAdapter = true)
data class QiblaDataDto(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "direction") val direction: Double
)

interface AlAdhanApiService {

    // Timings by coordinates
    @GET("timings/{date}")
    suspend fun getTimingsByCoordinates(
        @Path("date") date: String = "today",
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2, // ISNA=2, MWL=3, Egypt=5, Makkah=4, Karachi=1, Dubai=8
        @Query("school") school: Int = 0 // 0=Shafi, 1=Hanafi
    ): Response<AlAdhanTimingsResponse>

    // Timings by city & country
    @GET("timingsByCity/{date}")
    suspend fun getTimingsByCity(
        @Path("date") date: String = "today",
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0
    ): Response<AlAdhanTimingsResponse>

    // Monthly calendar by city
    @GET("calendarByCity/{year}/{month}")
    suspend fun getCalendarByCity(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0
    ): Response<AlAdhanCalendarResponse>

    // Monthly calendar by coordinates
    @GET("calendar/{year}/{month}")
    suspend fun getCalendarByCoordinates(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2,
        @Query("school") school: Int = 0
    ): Response<AlAdhanCalendarResponse>

    // Qibla direction angle from Makkah
    @GET("qibla/{latitude}/{longitude}")
    suspend fun getQiblaDirection(
        @Path("latitude") latitude: Double,
        @Path("longitude") longitude: Double
    ): Response<QiblaResponse>
}
