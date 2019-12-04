package com.cailihuang.apartmentgate

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.content.Context
import android.app.Activity
import androidx.core.content.ContextCompat
import com.cailihuang.apartmentgate.api.ApartmentListing
import kotlinx.android.synthetic.main.marker_listing.view.*

class MarkerInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker?): View {

        var infoView = (context as Activity).layoutInflater.inflate(R.layout.marker_listing, null)
        infoView.minimumWidth = 550
        infoView.minimumHeight = 425
        var infoWindow: ApartmentListing? = p0?.tag as ApartmentListing?

        infoView.markerApartmentName.text = infoWindow?.address1
        infoView.markerApartmentAddress.text = infoWindow?.address1!!.substringBefore(" Unit")
                .plus(", ").plus(infoWindow?.address2)
        infoView.markerRent.text = infoWindow?.rent.toString() + "/month"

        val beds = infoWindow?.beds.toString()
        if (beds == "0") {
            infoView.markerBeds.text = "Studio"
        } else {
            infoView.markerBeds.text = beds + " Beds"
        }

        val agScore = infoWindow?.AGScore!!
        if (agScore >= 85) {
            infoView.markerAGScore.setTextColor(ContextCompat.getColor(context, R.color.chromeGreen))
        } else if (agScore >= 70) {
            infoView.markerAGScore.setTextColor(ContextCompat.getColor(context, R.color.chromeYellow))
        } else {
            infoView.markerAGScore.setTextColor(ContextCompat.getColor(context, R.color.chromeRed))
        }
        infoView.markerAGScore.text = agScore.toString()

        return infoView
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}