package com.cailihuang.apartmentgate.api

import retrofit2.Call

class WalkScoreRepository(private val walkScoreApi: WalkScoreApi) {

    fun getWalkScore(address: String, lat: String, lon: String, key: String) : Call<WalkScore> {
        return walkScoreApi.getWalkScore(address, lat, lon, key)
    }

}