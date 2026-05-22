package com.example.myapp

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("next") val next: String?,
    @SerializedName("previous") val previous: String?,
    @SerializedName("results") val results: List<NewsArticle>
)

data class NewsArticle(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("news_site") val newsSite: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("published_at") val publishedAt: String
)
