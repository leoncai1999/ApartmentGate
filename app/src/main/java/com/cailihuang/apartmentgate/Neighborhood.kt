package com.cailihuang.apartmentgate

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Neighborhood (
        @field:SerializedName("average_rent")
        var average_rent: Int = 0,
        @field:SerializedName("favorites")
        var favorites: Int = 0,
        @field:SerializedName("image_url")
        var image_url: String = "",
        @field:SerializedName("name")
        var name: String = ""
): Parcelable