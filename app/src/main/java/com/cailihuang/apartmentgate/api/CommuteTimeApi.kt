package com.cailihuang.apartmentgate.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface CommuteTimeApi {

    @GET("/maps/api/distancematrix/json?units=imperial")
    suspend fun fetchCommute(
        @Query("origins") homeAddress: String,
        @Query("destinations") wordAddress: String,
        @Query("key") APIkey: String): CommuteResponse

    data class CommuteResponse(
        val rows: List<CommuteRows>//,
        //val status: String
        // status used for debugging API response
    )

    class CommuteRows(
        val elements: List<CommuteElementsResponse>
    )

    data class CommuteElementsResponse(val duration: CommuteTimeInfo)

    companion object {
        var httpurl = HttpUrl.Builder()
            .scheme("https")
            .host("maps.googleapis.com")
            .build()
        // Public create function that ties together building the base
        // URL and the private create function that initializes Retrofit
        fun create(): CommuteTimeApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): CommuteTimeApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    this.level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()
            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CommuteTimeApi::class.java)
        }
    }

}