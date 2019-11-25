package com.cailihuang.apartmentgate.api

import retrofit2.Call

class DirectionsRepository(private val directionsApi: DirectionsApi) {

    fun getDirections(origin: String, destination: String, key: String): Call<DirectionsApi.DirectionsQuery> {
        return directionsApi.getDirections(origin, destination, key)
    }

}