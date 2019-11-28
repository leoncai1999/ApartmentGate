package com.cailihuang.apartmentgate.api

import com.cailihuang.apartmentgate.APIKeys

class CommuteTimeRepository(private val api: CommuteTimeApi) {

    suspend fun getCommuteTime(origin: String, destination: String): CommuteTimeInfo {

        // TODO add transit option
        // TODO add work start time, work end time
        // these functions must have user login enabled, which Im not reactivating until later
        // to save time on the emulator launch

        //println(" DISTANCE MATRIX API STATUS --- " + api.fetchCommute(origin, destination, APIKeys.googleMapsAPIKey).status)

        //return api.fetchCommute(origin, destination, APIKeys.googleMapsAPIKey).rows[0].elements[0].duration.value

        //return 1
        //return api.getDirections(origin, destination, APIKeys.googleMapsAPIKey)

        return api.fetchCommute(origin, destination, APIKeys.googleMapsAPIKey).rows[0].elements[0].duration
    }

}