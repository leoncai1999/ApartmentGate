package com.cailihuang.apartmentgate.api

import com.cailihuang.apartmentgate.APIKeys

class CommuteTimeRepository(private val api: CommuteTimeApi) {

    suspend fun getCommuteTime(origin: String, destination: String): Int {
        return api.fetchCommute(origin, destination, APIKeys.googleMapsAPIKey).rows[0].elements[0].duration.value
    }

}