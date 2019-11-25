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

interface HowLoudApi {

    @GET("/address?")
    fun getHowLoudScore(@Query("address") address: String, @Query("key")
    key: String): Call<HowLoudQuery>

    class HowLoudQuery(
            val status: String?,
            val request: HowLoudRequest,
            val result: List<HowLoudScore>)

    data class HowLoudRequest(val latitude: Double, val key: String, val longitude: Double)

    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder()
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        var httpurl = HttpUrl.Builder()
                .scheme("http")
                .host("elb1.howloud.com")
                .build()
        fun create(): HowLoudApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): HowLoudApi {

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
                    .create(HowLoudApi::class.java)
        }
    }

}