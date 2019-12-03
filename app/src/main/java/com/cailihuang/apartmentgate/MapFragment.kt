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
import com.cailihuang.apartmentgate.api.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.android.synthetic.main.user_profile_information.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Semaphore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.net.URLEncoder
import java.text.SimpleDateFormat


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

        viewModel.initFirestore()


        // Initially upload soundScore and walkScore scores to Cloud Firestore database
//        val listingRef = viewModel.db.collection("listing")
//        listingRef
//            .get()
//            .addOnSuccessListener { result ->
//                println("YOLO")
//                for (document in result) {
//                        Log.d("LISTING", "${document.id} => ${document.data}")
//                        val aListing = document.toObject(ApartmentListing::class.java)
//                        val docRef = viewModel.db.collection("listing").document(document.id)
//                        val fullAddress = aListing.address1.substringBefore(" Unit") + ", " + aListing.address2
//                        val uiScope = CoroutineScope(Dispatchers.Main + Job())
//                        val coords = geocoder.getFromLocationName(fullAddress, 1)
//                        val addScoresSema = Semaphore(1)
//
//                        addScoresSema.acquire()
//
//                        val walkScoreApi = WalkScoreApi.create()
//                        val walkScoreRepository = WalkScoreRepository(walkScoreApi)
//                        uiScope.launch(
//                            context = uiScope.coroutineContext
//                                    + Dispatchers.IO) {
//                            val callResponse = walkScoreRepository.getWalkScore(URLEncoder.encode(fullAddress, "UTF-8"), coords[0].latitude.toString(), coords[0].longitude.toString(), APIKeys.walkscoreAPIKey)
//                            val response = callResponse.execute()
//                            docRef.update("walkScore", response.body()!!.walkscore)
//                            addScoresSema.release()
//                        }
//
//                        addScoresSema.acquire()
//
//                        val howLoudApi = HowLoudApi.create()
//                        val howLoudRepository = HowLoudRepository(howLoudApi)
//                        uiScope.launch(
//                            context = uiScope.coroutineContext
//                                    + Dispatchers.IO) {
//                            val callResponse = howLoudRepository.getHowLoudScore(URLEncoder.encode(fullAddress, "UTF-8"), APIKeys.soundscoreAPIKey)
//                            val response = callResponse.execute()
//                            if (response.isSuccessful) {
//                                docRef.update("soundScore", response.body()!!.result[0].score)
//                                addScoresSema.release()
//                            }
//                        }
//
//                        addScoresSema.acquire()
//                        addScoresSema.release()
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d("LISTING", "Error getting documents: ", exception)
//            }


        viewModel.populateListings()



        viewModel.getListings().observe(this, Observer { apartments ->
            for (i in 0 until apartments.size) {



                val fullAddress = apartments[i].address1.substringBefore(" Unit") + ", " + apartments[i].address2



                viewModel.db.collection("listing").document()

            }
        })








        // REALTIME DATABSE


        val ref = FirebaseDatabase.getInstance().getReference("listings").child("TZAVBG6NoTmSCv1tFdhe").child("apartment")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var count = 0

                 val uiScope = CoroutineScope(Dispatchers.Main + Job())

                var commuteTime = CommuteTimeInfo()
                val commuteListingSema = Semaphore(1)

                for (productSnapshot in dataSnapshot.children) {
                    if (count < 0) { // temporary solution
                        val apartment = productSnapshot.getValue(ApartmentListingOld::class.java)
//                        listings.add(apartment!!)
//                        val markerInfoWindow = MarkerInfoWindowAdapter(activity!!)
//                        map.setInfoWindowAdapter(markerInfoWindow)

//                        val apartmentAddress = geocoder.getFromLocationName(apartment.address, 1)
//                        val workAddress = geocoder.getFromLocationName(viewModel.getWorkAddress().value, 1)

                        val address = geocoder.getFromLocationName(apartment!!.address1, 1)
                        val marker = map.addMarker(MarkerOptions().position(LatLng(address[0].latitude, address[0].longitude))
                            .title(apartment.address1).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                        marker.tag = apartment
                        marker.showInfoWindow()

//                        val origin = apartmentAddress[0].latitude.toString() + "," + apartmentAddress[0].longitude.toString()
//                        val destination = workAddress[0].latitude.toString() + ", " + workAddress[0].longitude.toString()

//                        commuteListingSema.acquire()
//
//                        uiScope.launch(
//                            context = uiScope.coroutineContext
//                                    + Dispatchers.IO) {
//                            // Update LiveData from IO dispatcher, use postValue
//
//                            //commuteTime = commuteTimeRepository.getCommuteTime(origin, destination)
//
//                            commuteTime = commuteTimeRepository.getCommuteTime("37.779020, -122.479290", "37.792422, -122.406252")
//                            commuteListingSema.release()
//                        }
//
//                        commuteListingSema.acquire()
//                        commuteListingSema.release()

                        val mode = viewModel.currentUserProfile.transportation
                        var workStartMinString = viewModel.currentUserProfile.workStartMin.toString()
                        if (workStartMinString == "0") {
                            workStartMinString += "0"
                        }
                        // add 8 hours to conver to UTC
                        var workStartHourString = (viewModel.currentUserProfile.workStartHour + 8).toString()

                        println(" WHAT IS THE STRING ??? " + workStartHourString)
                        if (workStartHourString.length == 1) {
                            workStartHourString = "0" + workStartHourString
                        }
                        val arrivalTimeString = "Dec 02 2019 " + workStartHourString + ":" + workStartMinString + ":00.000 UTC"
                        val df = SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz")
                        val date = df.parse(arrivalTimeString)
                        val epoch = date.time / 1000

//                        viewModel.fetchDirections("37.779020,-122.479290", "37.792422,-122.406252", mode, epoch.toString(), APIKeys.googleMapsAPIKey)
//                        viewModel.observeCommuteTime().observe(this, Observer {
//                            commuteTime = it
//                        })


                        Log.d("COMMUTE TIME", "count is $count --- commute time ${commuteTime.value}")

                        //viewModel.commuteTimes.put(apartment.address1, commuteTime)

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
            if (listing.address1 == title) {
                return listing
            }
        }
        return listings[0]
    }

    private fun calculateApartmentScore(listing: ApartmentListing) {

    }

}