package com.cailihuang.apartmentgate.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WalkScore (
        @field:SerializedName("status")
        var status: Int = 0,
        @field:SerializedName("walkscore")
        var walkscore: Int = 0,
        @field:SerializedName("description")
        var description: String = "",
        @field:SerializedName("updated")
        var updated: String = "",
        @field:SerializedName("logo_url")
        var logo_url: String = "",
        @field:SerializedName("more_info_icon")
        var more_info_icon: String = "",
        @field:SerializedName("more_info_link")
        var more_info_link: String = "",
        @field:SerializedName("ws_link")
        var ws_link: String = "",
        @field:SerializedName("help_link")
        var help_link: String = "",
        @field:SerializedName("snapped_lat")
        var snapped_lat: String = "",
        @field:SerializedName("snapped_lon")
        var snapped_lon: String = "",
        @field:SerializedName("transit")
        var transit: TransitScore = TransitScore(),
        @field:SerializedName("bike")
        var bike: BikeScore = BikeScore()
): Parcelable