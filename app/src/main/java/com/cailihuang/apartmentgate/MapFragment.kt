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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.gson.annotations.SerializedName
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
    private lateinit var workCoordsString: String

    private var listings = mutableListOf<ApartmentListing>()

    private var fireInitSema = Semaphore(1)
    private var recalcSema = Semaphore(1)

    // Right now, all of the commute times must be retrieved before you launch the list

    data class UserID (
        @field:SerializedName("userid")
        var userid: String = "",
        @field:SerializedName("address")
        var address: String = "",
        @field:SerializedName("transportation")
        var transportation: String = "",
        @field:SerializedName("workStartTime")
        var workStartTime: String = "")

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_map, container, false)

        fireInitSema.acquire()

        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        //println("DOES IT GET HERE??? AHHHHHHHHH")

        viewModel.initFirestore()

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFrag) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        geocoder = Geocoder(activity, Locale.getDefault())




        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listButton.setOnClickListener {
            (activity as MainActivity).setFragment(ListFragment.newInstance())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //fireInitSema.acquire()
        map = googleMap

        // start map at center of San Francisco
        val startLocation = LatLng(37.775453, -122.439660)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLocation, 12.0f))


        // Initially upload soundScore and walkScore scores to Cloud Firestore database
//        val listingRef = viewModel.db.collection("listing")
//        listingRef
//            .get()
//            .addOnSuccessListener { result ->
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

        // Set the ApartmentGate score for each listing

        //recalcSema.acquire()

        val lastUserIdRef = viewModel.db.collection("LastUser").document("lastuser")
        lastUserIdRef.get().addOnSuccessListener { document ->

            val lastUser = document.toObject(UserID::class.java)
            val currentUser = FirebaseAuth.getInstance().currentUser
            var currentProfile: UserProfile
            val userRef = viewModel.db.collection("Users").document(currentUser!!.uid)
            userRef.get()
                .addOnSuccessListener { document ->

                    if (document != null) {
                        Log.d("CLOUD FIRESTORE", "DocumentSnapshot data: ${document.data}")
                        currentProfile = document.toObject(UserProfile::class.java)!!
                        val workCoords = geocoder.getFromLocationName(currentProfile.workAddress, 1)
                        workCoordsString = workCoords[0].latitude.toString() + "," + workCoords[0].longitude.toString()

                        if (currentUser.uid != lastUser!!.userid) {
                            val listingRef = viewModel.db.collection("listing")
                            val currentStartTime = currentProfile.workStartHour.toString().plus(currentProfile.workStartMin.toString())
                            listingRef
                                .get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        // Log.d("LISTING", "${document.id} => ${document.data}")
                                        val aListing = document.toObject(ApartmentListing::class.java)
                                        val recalcCommute = (currentProfile.workAddress != lastUser.address) ||
                                                (currentProfile.transportation != lastUser.transportation) ||
                                                (currentStartTime != lastUser.workStartTime)
                                        println("address boolean --- " + (currentProfile.workAddress != lastUser.address))
                                        println("transport boolean --- " + (currentProfile.transportation != lastUser.transportation))
                                        println("workStart boolean --- " + (currentStartTime != lastUser.workStartTime))

                                        val docRef = viewModel.db.collection("listing").document(document.id)
                                        docRef.update("agscore", calculateApartmentScore(aListing, recalcCommute))

                                        val fullAddress = aListing.address1.substringBefore(" Unit") + ", " + aListing.address2
                                        val apartmentAddress = geocoder.getFromLocationName(fullAddress, 1)
                                        docRef.update("latitude", apartmentAddress[0].latitude)
                                        docRef.update("longitude", apartmentAddress[0].longitude)
                                        docRef.update("commuteTime", aListing.commuteTime)
                                    }
                                    lastUserIdRef.update("userid", currentUser.uid)
                                    lastUserIdRef.update("address", currentProfile.workAddress)
                                    lastUserIdRef.update("transportation", currentProfile.transportation)
                                    lastUserIdRef.update("workStartTime", currentStartTime)
                                    //recalcSema.release()
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("LISTING", "Error getting documents: ", exception)
                                }
                        } else {
                            //recalcSema.release()
                        }
                    } else {
                        Log.d("CLOUD FIRESTORE", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("CLOUD FIRESTORE", "get failed with ", exception)
                }
        }

        viewModel.populateListings()

//        recalcSema.acquire()
//        recalcSema.release()

        viewModel.getListings().observe(this, Observer { apartments ->
            for (i in 0 until apartments.size) {

                val apartment = apartments[i]
                val fullAddress = apartments[i].address1.substringBefore(" Unit") + ", " + apartments[i].address2

                val markerInfoWindow = MarkerInfoWindowAdapter(activity!!)
                map.setInfoWindowAdapter(markerInfoWindow)
                val marker = map.addMarker(MarkerOptions().position(LatLng(apartment.latitude, apartment.longitude))
                        .title(apartment.address1).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)))
                marker.tag = apartment
                marker.showInfoWindow()

            }
//            val workAddress = geocoder.getFromLocationName(viewModel.currentUserProfile.workAddress, 1)
//            map.addMarker(MarkerOptions().position(LatLng(workAddress[0].latitude, workAddress[0].longitude)))
        })

        // REALTIME DATABSE

        //fireInitSema.release()


        map.setOnInfoWindowClickListener {
            // the on click listener is applied to the whole marker dialog because there is no
            // easy way to only have an on click listener for the get details button. We can look
            // into a workaround or 3rd party library later to address this
            var apartmentListing: ApartmentListing
            viewModel.db.collection("listing")
                .whereEqualTo("address1", it.title)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        apartmentListing = document.toObject(ApartmentListing::class.java)
                        (activity as MainActivity).setFragment(OneListingFragment.newInstance(apartmentListing))
                    }
                }
        }
    }

    // keeping around in case grpc is still a dud
    private fun googleMapifyAddress() {

    }

    // possibly used to refactor code
    private fun getCommuteTime() {

        //viewModel.refreshCommuteTime()

    }


    fun calculateApartmentScore(listing: ApartmentListing, recalcCommute: Boolean): Int {
        val userProfile = viewModel.currentUserProfile!!

        // Component 1: Commute
        var commuteScore = 100
        var maxCommute = userProfile.maxCommuteTime.substringBefore(" ").toInt()
        // maxCommute is 1 hour, convert to 60 min
        if (maxCommute == 1) {
            maxCommute = 60
        }
        val directionsApi = DirectionsApi.create()
        val directionsRepository = DirectionsRepository(directionsApi)
        var commuteTimeValue = listing.commuteTime

        if (recalcCommute) {
            /* For efficiency purposes, the directions API is only called if the commute details
            have changed from last time */
            val getCommuteSema = Semaphore(1)
            getCommuteSema.acquire()
            val apartmentCoordsString = listing.latitude.toString() + "," + listing.longitude.toString()
            val uiScope = CoroutineScope(Dispatchers.Main + Job())
            uiScope.launch(
                context = uiScope.coroutineContext
                        + Dispatchers.IO) {
                val callResponse = directionsRepository.getDirections(apartmentCoordsString, workCoordsString, userProfile.transportation, dateToEpoch().toString(), APIKeys.googleMapsAPIKey)
                val response = callResponse.execute()
                commuteTimeValue = response.body()!!.routes[0].legs[0].duration.value
                getCommuteSema.release()
            }

            getCommuteSema.acquire()
            getCommuteSema.release()
            commuteTimeValue /= 60
            listing.commuteTime = commuteTimeValue
        }

        if (commuteTimeValue > maxCommute) {
            commuteScore -= (commuteTimeValue - maxCommute) * 3
            if (commuteScore < 0) {
                commuteScore = 0
            }
        }

        // Component 2: Atmosphere
        var atmosphereScore = 100
        var preferredAtmosphere = userProfile.demographic
        val soundScore = listing.soundScore
        if (preferredAtmosphere == "retirees" && soundScore < 85) {
            atmosphereScore -= ((85 - soundScore) * 2.5).toInt()
        } else if (preferredAtmosphere == "families") {
            if (soundScore < 65) {
                atmosphereScore -= ((65 - soundScore) * 2.5).toInt()
            } else if (soundScore > 85) {
                atmosphereScore -= (soundScore - 85)
            }
        } else if (preferredAtmosphere == "professionals" && soundScore > 65) {
            atmosphereScore -= (soundScore - 65)
        }

        // Component 3: Walkability
        val walkValue = listing.walkScore
        var walkScore = listing.walkScore
        val walkabilityImportance = userProfile.walkability
        if (walkabilityImportance == "carealot") {
            walkScore -= (100 - walkValue)
            if (walkScore < 0) {
                walkScore = 0
            }
        } else if (walkabilityImportance == "dontcare") {
            walkScore += ((100 - walkValue)*.25).toInt()
            if (walkScore > 100) {
                walkScore = 100
            }
        }

        // Component 4: Affordability
        var affordabilityScore = 100
        val idealRent = userProfile.budget
        if (idealRent < listing.rent) {
            var pointCost = idealRent / 100
            if (pointCost <= 0) {
                pointCost = 1
            }
            affordabilityScore -= (listing.rent - idealRent) / pointCost
        }

        if (affordabilityScore < 0) {
            affordabilityScore = 0
        }

        // Component 5: Size
        var sizeScore = 100
        val idealSize = userProfile.size
        if (idealSize > listing.size) {
            sizeScore -= (idealSize - listing.size)/2
            if (sizeScore < 0) {
                sizeScore = 0
            }
        }

//        println("commute Score - " + commuteScore)
//        println("atmos Score - " + atmosphereScore)
//        println("walk Score - " + walkScore)
//        println("afford Score - " + affordabilityScore)
//        println("size Score - " + sizeScore)

        return ((commuteScore + atmosphereScore + walkScore + affordabilityScore + sizeScore) / 5)
    }



    private fun dateToEpoch(): Long {
        var workStartMinString = viewModel.currentUserProfile!!.workStartMin.toString()
        if (workStartMinString == "0") {
            workStartMinString += "0"
        }
        // add 8 hours to conver to UTC
        var workStartHourString = (viewModel.currentUserProfile!!.workStartHour + 8).toString()

        //println(" WHAT IS THE STRING ??? " + workStartHourString)
        if (workStartHourString.length == 1) {
            workStartHourString = "0" + workStartHourString
        }
        val arrivalTimeString = "Dec 02 2019 " + workStartHourString + ":" + workStartMinString + ":00.000 UTC"
        val df = SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz")
        val date = df.parse(arrivalTimeString)
        val epoch = date.time / 1000
        return epoch
    }



}