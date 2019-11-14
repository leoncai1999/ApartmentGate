package com.cailihuang.apartmentgate

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.SupportMapFragment
import java.util.*

class OneListingFragment : Fragment() {

    companion object {
        fun newInstance(): OneListingFragment {
            return OneListingFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_one_listing, container, false)
        return rootView
    }

}