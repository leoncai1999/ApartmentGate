package com.cailihuang.apartmentgate

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
import kotlinx.android.synthetic.main.fragment_one_listing.*
import java.net.URLEncoder
import java.util.*

class OneListingFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    companion object {
        fun newInstance(listing: ApartmentListing): OneListingFragment {
            val oneListingFragment = OneListingFragment()
            val args = Bundle()
            args.putParcelable("listing", listing)
            oneListingFragment.arguments = args
            return oneListingFragment
        }
    }

    private lateinit var geocoder: Geocoder

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        geocoder = Geocoder(activity, Locale.getDefault())

        val rootView = inflater.inflate(R.layout.fragment_one_listing, container, false)
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
                coords[0].latitude.toString(), coords[0].longitude.toString())
        viewModel.observeWalkScore().observe(this, Observer {
            // TODO: Must comply with branding requirements by linking to walkscore website
            apartmentWalkScoreTV.text = "Walk Score®: " + it.walkscore
            apartmentTransitScoreTV.text = "Transit Score®: " + it.transit.score
            apartmentBikeScoreTV.text = "Bike Score®: " + it.bike.score
        })

        viewModel.fetchHowLoudScore(URLEncoder.encode(listing!!.address, "UTF-8"))
        viewModel.observeHowLoudScore().observe(this, Observer {
            apartmentSoundScoreTV.text = "Sound Score®: " + it.score
        })

        return rootView
    }

}