package com.cailihuang.apartmentgate.api

import com.google.gson.GsonBuilder

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


interface ApartmentApi {

    data class ListingResponse(val apartment: List<ApartmentListing>)

}