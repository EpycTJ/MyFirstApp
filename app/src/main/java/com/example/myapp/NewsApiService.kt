package com.example.myapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v4/articles/")
    suspend fun getLatestNews(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): NewsResponse

    companion object {
        private const val BASE_URL = "https://api.spaceflightnewsapi.net/"

        fun create(): NewsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NewsApiService::class.java)
        }
    }
}
