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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_map.*
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.cailihuang.apartmentgate.api.ApartmentListing
import com.cailihuang.apartmentgate.api.CommuteTimeApi
import com.cailihuang.apartmentgate.api.CommuteTimeInfo
import com.cailihuang.apartmentgate.api.CommuteTimeRepository
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Semaphore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance(): MapFragment {
            return MapFragment()
        }
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var geocoder: Geocoder
    private lateinit var map: GoogleMap
    private var listings = mutableListOf<ApartmentListing>()

    // Right now, all of the commute times must be retrieved before you launch the list
    private val gotCommuteTimesSema = Semaphore(1)

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
            gotCommuteTimesSema.acquire()

            (activity as MainActivity).setFragment(ListFragment.newInstance())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        gotCommuteTimesSema.acquire()

        // start map at center of San Francisco
        val startLocation = LatLng(37.775453, -122.439660)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12.0f))

        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val ref = FirebaseDatabase.getInstance().getReference("listings").child("TZAVBG6NoTmSCv1tFdhe").child("apartment")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0

                val commuteTimeApi = CommuteTimeApi.create()
                val commuteTimeRepository = CommuteTimeRepository(commuteTimeApi)
                //val job =
                val uiScope = CoroutineScope(Dispatchers.Main + Job())

                var commuteTime = CommuteTimeInfo()
                val commuteListingSema = Semaphore(1)

                for (productSnapshot in dataSnapshot.children) {
                    if (count < 5) { // temporary solution
                        val apartment = productSnapshot.getValue(ApartmentListing::class.java)
                        listings.add(apartment!!)
                        val markerInfoWindow = MarkerInfoWindowAdapter(activity!!)
                        map.setInfoWindowAdapter(markerInfoWindow)

//                        val apartmentAddress = geocoder.getFromLocationName(apartment.address, 1)
//                        val workAddress = geocoder.getFromLocationName(viewModel.getWorkAddress().value, 1)

//                        val origin = apartmentAddress[0].latitude.toString() + "," + apartmentAddress[0].longitude.toString()
//                        val destination = workAddress[0].latitude.toString() + ", " + workAddress[0].longitude.toString()

                        commuteListingSema.acquire()

                        uiScope.launch(
                            context = uiScope.coroutineContext
                                    + Dispatchers.IO) {
                            // Update LiveData from IO dispatcher, use postValue

                            //commuteTime = commuteTimeRepository.getCommuteTime(origin, destination)

                            commuteTime = commuteTimeRepository.getCommuteTime("37.779020, -122.479290", "37.792422, -122.406252")
                            commuteListingSema.release()
                        }

                        commuteListingSema.acquire()
                        commuteListingSema.release()

                        Log.d("COMMUTE TIME", "count is $count --- commute time ${commuteTime.value}")

                        viewModel.commuteTimes.put(apartment.address, commuteTime)

                        count++
                    }
                }

                //val workAddress = geocoder.getFromLocationName(viewModel.getWorkAddress().value, 1)
                //map.addMarker(MarkerOptions().position(LatLng(workAddress[0].latitude, workAddress[0].longitude)))

                gotCommuteTimesSema.release()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        })

        map.setOnInfoWindowClickListener {
            // the on click listener is applied to the whole marker dialog because there is no
            // easy way to only have an on click listener for the get details button. We can look
            // into a workaround or 3rd party library later to address this

            // TODO: Query data instead of using helper function below for efficiency purposes
            (activity as MainActivity).setFragment(OneListingFragment.newInstance(getListingFromTitle(it.title)))
        }
    }

    // keeping around in case grpc is still a dud
    private fun googleMapifyAddress() {

    }

    // possibly used to refactor code
    private fun getCommuteTime() {

        //viewModel.refreshCommuteTime()

    }

    private fun getListingFromTitle(title: String): ApartmentListing {
        for (listing in listings) {
            if (listing.name == title) {
                return listing
            }
        }
        return listings[0]
    }

}