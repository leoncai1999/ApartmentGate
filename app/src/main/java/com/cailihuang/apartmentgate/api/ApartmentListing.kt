package com.cailihuang.apartmentgate.api

import com.google.gson.annotations.SerializedName

data class ApartmentListing (
    @SerializedName("name")
    val nombre: String,
    @SerializedName("url")
    val link: String,
    @SerializedName("address")
    val direccion: String,
    @SerializedName("bds")
    val beds: String,
    @SerializedName("rent")
    val price: String
)