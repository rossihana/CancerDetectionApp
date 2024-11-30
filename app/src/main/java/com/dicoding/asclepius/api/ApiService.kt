package com.dicoding.asclepius.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("top-headlines")
    suspend fun getCancerNews(
        @Query("q") query: String = "cancer",
        @Query("category") category: String = "health",
        @Query("language") language: String = "en",
        @Query("apiKey") apiKey: String = "c8138415582f4c6b9b8c02a775ed47c7"
    ): Response<NewsResponse>
}