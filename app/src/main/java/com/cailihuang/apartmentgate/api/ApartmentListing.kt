package com.cailihuang.apartmentgate.api

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApartmentListing (
        @field:SerializedName("name")
        var name: String = "",
        @field:SerializedName("url")
        var url: String = "",
        @field:SerializedName("address")
        var address: String = "",
        @field:SerializedName("bds")
        var bds: String = "",
        @field:SerializedName("rent")
        var rent: String = ""
): Parcelable