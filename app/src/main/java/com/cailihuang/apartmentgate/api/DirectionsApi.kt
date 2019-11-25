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

interface DirectionsApi {

    @GET("/maps/api/directions/json?&sensor=false&mode=driving&key=***REMOVED***")
    fun getDirections(@Query("origin") origin: String, @Query("destination")
    destination: String, @Query("key") key: String): Call<DirectionsQuery>

    class DirectionsQuery (
            val geocoded_waypoints : List<Geocoded_waypoints>,
            val routes : List<Routes>,
            val status : String
    )

    data class Geocoded_waypoints (
            val geocoder_status : String,
            val place_id : String,
            val types : List<String>
    )

    data class Routes (
            val bounds : Bounds,
            val copyrights : String,
            val legs : List<Legs>,
            val overview_polyline : Overview_polyline,
            val summary : String,
            val warnings : List<String>,
            val waypoint_order : List<String>
    )

    data class Bounds (
            val northeast : Northeast,
            val southwest : Southwest
    )

    data class Northeast (
            val lat : Double,
            val lng : Double
    )

    data class Southwest (
            val lat : Double,
            val lng : Double
    )

    data class Legs (
            val distance : Distance,
            val duration : Duration,
            val end_address : String,
            val end_location : End_location,
            val start_address : String,
            val start_location : Start_location,
            val steps : List<Steps>,
            val traffic_speed_entry : List<String>,
            val via_waypoint : List<String>
    )

    data class Distance (
            val text : String,
            val value : Int
    )

    data class Duration (
            val text : String,
            val value : Int
    )

    data class End_location (
            val lat : Double,
            val lng : Double
    )

    data class Start_location (
            val lat : Double,
            val lng : Double
    )

    data class Steps (
            val distance : Distance,
            val duration : Duration,
            val end_location : End_location,
            val html_instructions : String,
            val polyline : Polyline,
            val start_location : Start_location,
            val travel_mode : String
    )

    data class Polyline (
            val points : String
    )

    data class Overview_polyline (
            val points : String
    )

    companion object {
        private fun buildGsonConverterFactory(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder()
            return GsonConverterFactory.create(gsonBuilder.create())
        }
        var httpurl = HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .build()
        fun create(): DirectionsApi = create(httpurl)
        private fun create(httpUrl: HttpUrl): DirectionsApi {

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
                    .create(DirectionsApi::class.java)
        }
    }

}