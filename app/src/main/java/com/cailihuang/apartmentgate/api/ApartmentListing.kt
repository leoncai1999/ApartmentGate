package com.cailihuang.apartmentgate.api

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApartmentListing (
        @field:SerializedName("about")
        var about: String = "",
        @field:SerializedName("address1")
        var address1: String = "",
        @field:SerializedName("address2")
        var address2: String = "",
        @field:SerializedName("baths")
        var baths: Int = 0,
        @field:SerializedName("beds")
        var beds: Int = 0,
        @field:SerializedName("deposit")
        var deposit: Int = 0,
        @field:SerializedName("neighborhood")
        var neighborhood: String = "",
        @field:SerializedName("neighborhood_url")
        var neighborhood_url: String = "",
        @field:SerializedName("rent")
        var rent: Int = 0,
        @field:SerializedName("size")
        var size: Int = 0,
        @field:SerializedName("AGScore")
        var AGScore: Int = 0,
        @field:SerializedName("soundScore")
        var soundScore: Int = 0,
        @field:SerializedName("walkScore")
        var walkScore: Int = 0
): Parcelable