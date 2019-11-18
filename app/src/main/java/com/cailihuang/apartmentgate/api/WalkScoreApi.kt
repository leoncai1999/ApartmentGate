package com.cailihuang.apartmentgate.api

import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WalkScoreApi {

    // TODO: Change the WSApiKey to a parameter and establish practice for hiding api key
    // Note: to get this to work, you must put in the api key below
    @GET("/score?format=json&transit=1&bike=1&wsapikey=")
    fun getWalkScore(@Query("address") address: String, @Query("lat") lat:
    String, @Query("lon") lon: String): Call<WalkScore>

    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder()
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        var httpurl = HttpUrl.Builder()
                .scheme("http")
                .host("api.walkscore.com")
                .build()
        fun create(): WalkScoreApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): WalkScoreApi {

            val client = OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().apply {
                        this.level = HttpLoggingInterceptor.Level.BASIC
                    })
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(buildGsonConverterFactory())
                    .build()
                    .create(WalkScoreApi::class.java)
        }
    }
}