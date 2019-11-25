package com.cailihuang.apartmentgate

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cailihuang.apartmentgate.api.ApartmentListing
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList

class OneListingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: MainViewModel
    private lateinit var geocoder: Geocoder
    private lateinit var map: GoogleMap
    private lateinit var rootView: View

    companion object {
        fun newInstance(listing: ApartmentListing): OneListingFragment {
            val oneListingFragment = OneListingFragment()
            val args = Bundle()
            args.putParcelable("listing", listing)
            oneListingFragment.arguments = args
            return oneListingFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        geocoder = Geocoder(activity, Locale.getDefault())

        rootView = inflater.inflate(R.layout.fragment_one_listing, container, false)
        val listing = arguments?.getParcelable<ApartmentListing>("listing")

        val apartmentNameTV = rootView.findViewById<TextView>(R.id.apartmentName)
        apartmentNameTV.text = listing!!.name
        val apartmentAddressTV = rootView.findViewById<TextView>(R.id.apartmentAddress)
        apartmentAddressTV.text = listing!!.address
        val apartmentRentTV = rootView.findViewById<TextView>(R.id.apartmentRent)
        apartmentRentTV.text = "Rent: " + listing!!.rent + "/month"
        val apartmentBedroomsTV = rootView.findViewById<TextView>(R.id.apartmentBedrooms)
        apartmentBedroomsTV.text = "Bedrooms: " + listing!!.bds

        val apartmentWalkScoreTV = rootView.findViewById<TextView>(R.id.apartmentWalkScore)
        val apartmentTransitScoreTV = rootView.findViewById<TextView>(R.id.apartmentTransitScore)
        val apartmentBikeScoreTV = rootView.findViewById<TextView>(R.id.apartmentBikeScore)
        val apartmentSoundScoreTV = rootView.findViewById<TextView>(R.id.apartmentSoundScore)

        val coords = geocoder.getFromLocationName(listing!!.address, 1)

        viewModel.fetchWalkScore(URLEncoder.encode(listing!!.address, "UTF-8"),
                coords[0].latitude.toString(), coords[0].longitude.toString(), APIKeys.walkscoreAPIKey)
        viewModel.observeWalkScore().observe(this, Observer {
            // TODO: Must comply with branding requirements by linking to walkscore website
            apartmentWalkScoreTV.text = "Walk Score®: " + it.walkscore
            apartmentTransitScoreTV.text = "Transit Score®: " + it.transit.score
            apartmentBikeScoreTV.text = "Bike Score®: " + it.bike.score
        })

        viewModel.fetchHowLoudScore(URLEncoder.encode(listing!!.address, "UTF-8"), APIKeys.sondscoreAPIKey)
        viewModel.observeHowLoudScore().observe(this, Observer {
            apartmentSoundScoreTV.text = "Sound Score®: " + it.score
        })

        val mapFragment = childFragmentManager.findFragmentById(R.id.apartmentMapFrag) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val listing = arguments?.getParcelable<ApartmentListing>("listing")
        val apartmentAddress = geocoder.getFromLocationName(listing!!.address, 1)
        val workAddress = geocoder.getFromLocationName(viewModel.getWorkAddress().value, 1)
        val apartmentCoords = LatLng(apartmentAddress[0].latitude, apartmentAddress[0].longitude)
        val workCoords = LatLng(workAddress[0].latitude, workAddress[0].longitude)

        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val origin = apartmentAddress[0].latitude.toString() + "," + apartmentAddress[0].longitude.toString()
        val destination = workAddress[0].latitude.toString() + ", " + workAddress[0].longitude.toString()
        viewModel.fetchDirections(origin, destination, APIKeys.googleMapsAPIKey)

        val apartmentCommuteTimeTV = rootView.findViewById<TextView>(R.id.apartmentCommuteTime)

        viewModel.observeCommuteTime().observe(this, Observer {
            apartmentCommuteTimeTV.text = "Commute Time: " + it
        })

        viewModel.observeOverviewPolyline().observe(this, Observer {
            map.clear()
            var points = ArrayList<LatLng>()
            val lineOptions = PolylineOptions().color(Color.BLUE).width(10f)
            var polypoints = decodePoly(it)

            for (i in 0 until polypoints.size) {
                val lat = polypoints[i].latitude
                val lon = polypoints[i].longitude
                points.add(LatLng(lat, lon))
            }

            lineOptions.addAll(points)
            map.addPolyline(lineOptions)
            map.addMarker(MarkerOptions().position(apartmentCoords)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
            map.addMarker(MarkerOptions().position(workCoords))

            val zoomBounds = LatLngBounds.Builder().include(apartmentCoords).include(workCoords).build()
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(zoomBounds, 100))
        })

    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

}