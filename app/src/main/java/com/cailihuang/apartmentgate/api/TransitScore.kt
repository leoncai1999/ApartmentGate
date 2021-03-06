package com.cailihuang.apartmentgate.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransitScore (
        @field:SerializedName("score")
        var score: Int = 0,
        @field:SerializedName("description")
        var description: String = "",
        @field:SerializedName("summary")
        var summary: String = ""
): Parcelable