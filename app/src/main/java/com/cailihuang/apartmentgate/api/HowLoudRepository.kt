package com.cailihuang.apartmentgate.api

import retrofit2.Call

class HowLoudRepository(private val howLoudApi: HowLoudApi) {

    fun getHowLoudScore(address: String, key: String): Call<HowLoudApi.HowLoudQuery> {
        return howLoudApi.getHowLoudScore(address, key)
    }

}