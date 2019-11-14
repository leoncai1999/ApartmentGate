package com.cailihuang.apartmentgate.api

import com.google.firebase.database.PropertyName

data class ApartmentListing (
//    @SerializedName("name")
//    val name: String,
//    @SerializedName("url")
//    val : String,
//    @SerializedName("address")
//    val address: String,
//    @SerializedName("bds")
//    val bds: String,
//    @SerializedName("rent")
//    val rent: String



    @PropertyName("name")
    var name: String = "",
    @PropertyName("url")
    var url: String = "",
    @PropertyName("address")
    var address: String = "",
    @PropertyName("bds")
    var bds: String = "",
    @PropertyName("rent")
    var rent: String = ""


//    @set:PropertyName("name")
//    @get:PropertyName("name")
//    var name: String = "",
//    //@PropertyName("email") val email: String = "",
//    @set:PropertyName("address")
//    @get:PropertyName("address")
//    var address: String = ""
//    //@PropertyName("provider") val provider: String = ""




)
//{
//    constructor() : this("", "", "", "", "")
//}