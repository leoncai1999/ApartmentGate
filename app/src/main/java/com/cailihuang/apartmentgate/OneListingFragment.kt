package com.cailihuang.apartmentgate

import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.api.ApartmentListing
import com.cailihuang.apartmentgate.api.DirectionsApi
import com.cailihuang.apartmentgate.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import kotlinx.android.synthetic.main.fragment_one_listing.*
import java.text.SimpleDateFormat

class OneListingFragment : Fragment(), OnMapReadyCallback {

    private lateinit var viewModel: MainViewModel
    private lateinit var geocoder: Geocoder
    private lateinit var map: GoogleMap
    private lateinit var rootView: View
    private lateinit var directionsAdapter: DirectionsListAdapter

    companion object {
        fun newInstance(listing: ApartmentListing): OneListingFragment {
            val oneListingFragment = OneListingFragment()
            val args = Bundle()
            args.putParcelable("listing", listing)
            oneListingFragment.arguments = args
            return oneListingFragment
        }
    }

    private fun initAdapter(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.directionsRV)
        directionsAdapter = DirectionsListAdapter(viewModel)
        rv.adapter = directionsAdapter
        rv.layoutManager = LinearLayoutManager(context)
        rv.addItemDecoration(DividerItemDecoration(rv.getContext(), DividerItemDecoration.VERTICAL))
        rv.isNestedScrollingEnabled = false
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
        val fullAddress = listing!!.address1.substringBefore(" Unit") + ", " + listing.address2

        val backButton = rootView.findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
            if (viewModel.returnToMap) {
                (activity as MainActivity).setFragment(MapFragment.newInstance())
            } else {
                (activity as MainActivity).supportFragmentManager.popBackStack()
            }
        }

        val favoriteButton = rootView.findViewById<ImageView>(R.id.actionFavorite)

        favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp)

        if (viewModel.isFav(listing)) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_black_24dp)
        }

        val apartmentNameTV = rootView.findViewById<TextView>(R.id.apartmentName)
        apartmentNameTV.text = listing!!.address1
        val apartmentAddressTV = rootView.findViewById<TextView>(R.id.apartmentAddress)
        apartmentAddressTV.text = fullAddress
        val apartmentGateScoreTV = rootView.findViewById<TextView>(R.id.apartmentGateScore)
        val agScore = listing.AGScore
        if (agScore >= 85) {
            apartmentGateScoreTV.setTextColor(ContextCompat.getColor(context!!, R.color.chromeGreen))
        } else if (agScore >= 70) {
            apartmentGateScoreTV.setTextColor(ContextCompat.getColor(context!!, R.color.chromeYellow))
        } else {
            apartmentGateScoreTV.setTextColor(ContextCompat.getColor(context!!, R.color.chromeRed))
        }
        apartmentGateScoreTV.text = agScore.toString()
        val apartmentRentTV = rootView.findViewById<TextView>(R.id.apartmentRent)
        apartmentRentTV.text = "Rent: $" + listing.rent + "/month"
        val apartmentBedroomsTV = rootView.findViewById<TextView>(R.id.apartmentBedrooms)
        if (listing.beds == 0) {
            apartmentBedroomsTV.text = "Bedrooms: Studio"
        } else {
            apartmentBedroomsTV.text = "Bedrooms: " + listing.beds
        }
        val apartmentBathroomsTV = rootView.findViewById<TextView>(R.id.apartmentBathrooms)
        apartmentBathroomsTV.text = "Bathooms: " + listing.baths
        val apartmentSizeTV = rootView.findViewById<TextView>(R.id.apartmentSize)
        apartmentSizeTV.text = "Size: " + listing.size + " Sq Ft"
        val apartmentNeighborhoodTV = rootView.findViewById<TextView>(R.id.apartmentNeighborhood)
        apartmentNeighborhoodTV.text = "Neighorhood: " + listing.neighborhood
        val apartmentDescriptionTV = rootView.findViewById<TextView>(R.id.apartmentDescription)
        apartmentDescriptionTV.text = listing.about

        val apartmentAddress = geocoder.getFromLocationName(fullAddress, 1)
        val apartmentImage = rootView.findViewById<ImageView>(R.id.apartmentImage)
        val imageURL = "https://maps.googleapis.com/maps/api/streetview?size=600x300&location="
                .plus(apartmentAddress[0].latitude.toString()).plus(",%20")
                .plus(apartmentAddress[0].longitude.toString())
                .plus("&fov=100&heading=70&pitch=0&key=")
                .plus(APIKeys.googleMapsAPIKey)
        Glide.glideFetch(imageURL, apartmentImage)

        val apartmentWalkScoreTV = rootView.findViewById<TextView>(R.id.apartmentWalkScore)
        //val apartmentTransitScoreTV = rootView.findViewById<TextView>(R.id.apartmentTransitScore)
        val apartmentBikeScoreTV = rootView.findViewById<TextView>(R.id.apartmentBikeScore)
        val apartmentSoundScoreTV = rootView.findViewById<TextView>(R.id.apartmentSoundScore)

        val coords = geocoder.getFromLocationName(fullAddress, 1)

        viewModel.fetchWalkScore(URLEncoder.encode(fullAddress, "UTF-8"),
                coords[0].latitude.toString(), coords[0].longitude.toString(), APIKeys.walkscoreAPIKey)
        viewModel.observeWalkScore().observe(this, Observer {
            val wsHelpLink = it.help_link
            val walkscoreText = SpannableString("Walk Score®: " + it.walkscore)
            walkscoreText.setSpan(UnderlineSpan(), 0, 10, 0)
            walkscoreText.setSpan(UnderlineSpan(), 13, walkscoreText.length, 0)
            apartmentWalkScoreTV.text = walkscoreText
            apartmentWalkScoreTV.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wsHelpLink))
                startActivity(browserIntent)
            }

//            val transitscoreText = SpannableString("Transit Score®: " + it.transit.score)
//            transitscoreText.setSpan(UnderlineSpan(), 0, 13, 0)
//            transitscoreText.setSpan(UnderlineSpan(), 16, transitscoreText.length, 0)
//            apartmentTransitScoreTV.text = transitscoreText
//            apartmentTransitScoreTV.setOnClickListener {
//                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wsHelpLink))
//                startActivity(browserIntent)
//            }

            val bikescoreText = SpannableString("Bike Score®: " + it.bike.score)
            bikescoreText.setSpan(UnderlineSpan(), 0, 10, 0)
            bikescoreText.setSpan(UnderlineSpan(), 13, bikescoreText.length, 0)
            apartmentBikeScoreTV.text = bikescoreText
            apartmentBikeScoreTV.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wsHelpLink))
                startActivity(browserIntent)
            }
        })

        viewModel.fetchHowLoudScore(URLEncoder.encode(fullAddress, "UTF-8"), APIKeys.soundscoreAPIKey)
        viewModel.observeHowLoudScore().observe(this, Observer {
            apartmentSoundScoreTV.text = "Sound Score®: " + it.score
        })

        favoriteButton.setOnClickListener {
            if (viewModel.isFav(listing)) {
                favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp)
                viewModel.removeFav(listing)
                viewModel.updateTrendingNeighborhoods(listing, true)
            } else {
                favoriteButton.setImageResource(R.drawable.ic_favorite_black_24dp)
                viewModel.addFav(listing)
                viewModel.updateTrendingNeighborhoods(listing, false)
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.apartmentMapFrag) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val currentUserProf = viewModel.currentUserProfile

        val listing = arguments?.getParcelable<ApartmentListing>("listing")
        val fullAddress = listing!!.address1.substringBefore(" Unit") + ", " + listing.address2
        val apartmentAddress = geocoder.getFromLocationName(fullAddress, 1)
        val workAddress = geocoder.getFromLocationName(currentUserProf.workAddress, 1)
        val apartmentCoords = LatLng(apartmentAddress[0].latitude, apartmentAddress[0].longitude)
        val workCoords = LatLng(workAddress[0].latitude, workAddress[0].longitude)

        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val origin = apartmentAddress[0].latitude.toString() + "," + apartmentAddress[0].longitude.toString()
        val destination = workAddress[0].latitude.toString() + ", " + workAddress[0].longitude.toString()

        val mode = currentUserProf.transportation
        var workStartMinString = currentUserProf.workStartMin.toString()
        if (workStartMinString == "0") {
            workStartMinString += "0"
        }
        // add 8 hours to conver to UTC
        var workStartHourString = (currentUserProf.workStartHour + 8).toString()

        println(" WHAT IS THE STRING ??? " + workStartHourString)
        if (workStartHourString.length == 1) {
            workStartHourString = "0" + workStartHourString
        }
        val arrivalTimeString = "Dec 02 2019 " + workStartHourString + ":" + workStartMinString + ":00.000 UTC"
        val df = SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz")
        val date = df.parse(arrivalTimeString)
        val epoch = date.time / 1000

        viewModel.fetchDirections(origin, destination, mode, epoch.toString(),APIKeys.googleMapsAPIKey)

        val apartmentCommuteTimeTV = rootView.findViewById<TextView>(R.id.apartmentCommuteTime)
        val apartmentCommuteFareTV = rootView.findViewById<TextView>(R.id.apartmentCommuteFare)

        viewModel.observeCommuteTime().observe(this, Observer {
            apartmentCommuteTimeTV.text = "Commute Time: " + it
        })

        viewModel.observeCommuteFare().observe(this, Observer {
            apartmentCommuteFareTV.text = "Commute Fare: " + it
        })

        val commuteFare = rootView.findViewById<TextView>(R.id.apartmentCommuteFare)
        val commuteDistance = rootView.findViewById<TextView>(R.id.commuteDistance)
        val durationInTraffic = rootView.findViewById<TextView>(R.id.durationInTraffic)
        val directionsRV = rootView.findViewById<RecyclerView>(R.id.directionsRV)

        if (mode == "driving") {
            viewModel.observeDurationInTraffic().observe(this, Observer {
                durationInTraffic.text = "Duration in Traffic: " + it
            })
        }

        lateinit var lineOptions: PolylineOptions
        var chromeBlue = Color.parseColor("#4285F4")

        if (mode == "transit") {
            viewModel.observeDirections().observe(this, Observer {
                map.clear()
                commuteDistance.visibility = View.GONE
                durationInTraffic.visibility = View.GONE

                for (i in 0 until it.size) {
                    if (it[i].travel_mode == "TRANSIT") {
                        val lineColor = it[i].transit_details.line.color
                        if (lineColor != null) {
                            // Light rail and Subway lines have special route colors
                            lineOptions = PolylineOptions().color(Color.parseColor(lineColor)).width(10f)
                        } else {
                            lineOptions = PolylineOptions().color(chromeBlue).width(10f)
                        }
                    } else if  (it[i].travel_mode == "WALKING") {
                        val pattern = Arrays.asList(Dot(), Gap(15f))
                        lineOptions = PolylineOptions().color(chromeBlue).pattern(pattern).width(10f)
                    } else {
                        lineOptions = PolylineOptions().color(chromeBlue).width(10f)
                    }

                    drawPoly(it[i].polyline.points, lineOptions)
                }

                plotMarkers(apartmentCoords, workCoords)

                initAdapter(rootView)
                directionsAdapter.submitList(it)
            })
        } else {
            viewModel.observeCurrentDistance().observe(this, Observer {
                commuteDistance.text = "Distance: " + it
            })
            viewModel.observeOverviewPolyline().observe(this, Observer {
                map.clear()
                commuteFare.visibility = View.GONE
                directionsRV.visibility = View.GONE

                if (mode != "driving") {
                    durationInTraffic.visibility = View.GONE
                }

                if (mode == "walking") {
                    val pattern = Arrays.asList(Dot(), Gap(20f), Dash(30f), Gap(20f))
                    lineOptions = PolylineOptions().color(Color.BLUE).pattern(pattern).width(10f)
                } else {
                    lineOptions = PolylineOptions().color(Color.BLUE).width(10f)
                }
                drawPoly(it, lineOptions)
                plotMarkers(apartmentCoords, workCoords)
            })
        }
    }

    fun plotMarkers(apartmentCoords: LatLng, workCoords: LatLng) {
        map.addMarker(MarkerOptions().position(apartmentCoords)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
        map.addMarker(MarkerOptions().position(workCoords))
        val zoomBounds = LatLngBounds.Builder().include(apartmentCoords).include(workCoords).build()
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(zoomBounds, 100))
    }

    private fun drawPoly(polyline: String, lineOptions: PolylineOptions) {
        var points = ArrayList<LatLng>()
        var polypoints = decodePoly(polyline)

        for (i in 0 until polypoints.size) {
            val lat = polypoints[i].latitude
            val lon = polypoints[i].longitude
            points.add(LatLng(lat, lon))
        }

        lineOptions.addAll(points)
        map.addPolyline(lineOptions)
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