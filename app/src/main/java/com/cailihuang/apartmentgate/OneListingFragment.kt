package com.cailihuang.apartmentgate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cailihuang.apartmentgate.api.ApartmentListing
import kotlinx.android.synthetic.main.fragment_one_listing.*

class OneListingFragment : Fragment() {

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

        return rootView
    }

}