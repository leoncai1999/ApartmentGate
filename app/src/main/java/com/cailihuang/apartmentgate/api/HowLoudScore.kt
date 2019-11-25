package com.cailihuang.apartmentgate.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HowLoudScore (
        @field:SerializedName("airports")
        var airports: Int = 0,
        @field:SerializedName("traffictext")
        var traffictext: String = "",
        @field:SerializedName("localtext")
        var localtext: String = "",
        @field:SerializedName("airportstext")
        var airportstext: String = "",
        @field:SerializedName("score")
        var score: Int = 0,
        @field:SerializedName("traffic")
        var traffic: Int = 0,
        @field:SerializedName("scoretext")
        var scoretext: String = "",
        @field:SerializedName("local")
        var local: Int = 0
): Parcelable