package com.cailihuang.apartmentgate.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommuteTimeInfo (
    @field:SerializedName("value")
    var value: Int = 0,
    @field:SerializedName("text")
    var text: String = ""
): Parcelable