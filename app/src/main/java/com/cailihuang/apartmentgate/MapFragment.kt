package com.cailihuang.apartmentgate

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_map.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.util.*

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    private lateinit var viewModel: ListViewModel
    private lateinit var geocoder: Geocoder
    private lateinit var map: GoogleMap

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFrag) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        geocoder = Geocoder(activity, Locale.getDefault())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listButton.setOnClickListener {
            val manager = activity?.supportFragmentManager
            val transaction = manager?.beginTransaction()
            transaction?.replace(R.id.main_frame, ListFragment.newInstance())
            transaction?.addToBackStack(null)
            transaction?.commit()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // start map at center of San Francisco
        val startLocation = LatLng(37.775453, -122.439660)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12.0f))

        // TODO: Determine how many pins to plot at one time, and  resolve runtime issue
        // "Cannot add the same observer with different lifecycles"

//        viewModel = activity?.run {
//            ViewModelProviders.of(this)[ListViewModel::class.java]
//        } ?: throw Exception("Invalid Activity")
//
//        viewModel.refresh()

//        viewModel.observeListings().observe(this, Observer {
//            for (apartment in it) {
//                val address = geocoder.getFromLocationName(apartment.direccion, 1)
//                map.addMarker(MarkerOptions().position(LatLng(address[0].latitude, address[0].longitude))
//                        .title(apartment.nombre))
//            }

//            for (i in 0..15) {
//                val apartment = it[i]
//                val address = geocoder.getFromLocationName(apartment.direccion, 1)
//                map.addMarker(MarkerOptions().position(LatLng(address[0].latitude, address[0].longitude))
//                        .title(apartment.nombre))
//            }
//        })
    }

}