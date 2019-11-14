package com.cailihuang.apartmentgate

import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import android.content.Context
import android.app.Activity
import com.cailihuang.apartmentgate.api.ApartmentListing
import kotlinx.android.synthetic.main.marker_listing.view.*

class MarkerInfoWindowAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker?): View {

        var infoView = (context as Activity).layoutInflater.inflate(R.layout.marker_listing, null)
        infoView.minimumWidth = 750
        infoView.minimumHeight = 500
        var infoWindow: ApartmentListing? = p0?.tag as ApartmentListing?

        infoView.markerApartmentName.text = infoWindow?.name
        infoView.markerApartmentAddress.text = infoWindow?.address
        infoView.markerRent.text = infoWindow?.rent
        infoView.markerBeds.text = infoWindow?.bds

        return infoView
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}