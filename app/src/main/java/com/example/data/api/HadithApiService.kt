package com.example.data.api

import com.example.data.models.RandomHadithResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HadithApiService {

    @GET("{book}")
    suspend fun getRandomHadith(
        @Path("book") book: String = "bukhari"
    ): RandomHadithResponse
}
