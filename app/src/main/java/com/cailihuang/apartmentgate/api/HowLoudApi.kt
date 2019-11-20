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

    // TODO: Change the WSApiKey to a parameter and establish practice for hiding api key
    // Note: to get this to work, you must put in the api key below
    @GET("/address?key=")
    fun getHowLoudScore(@Query("address") address: String): Call<HowLoudQuery>

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