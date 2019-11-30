package com.cailihuang.apartmentgate

import androidx.lifecycle.*
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cailihuang.apartmentgate.api.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.AlgorithmParameterGenerator
import java.util.concurrent.Semaphore
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue


class MainViewModel : ViewModel() {

    lateinit var db: FirebaseFirestore
    lateinit var userRef: DocumentReference

    fun initFirestore() {
        db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser!!
        userRef = db.collection("Users").document(user.uid)
    }

    private var favListings = MutableLiveData<List<ApartmentListing>>().apply {
        value = mutableListOf()
    }

    //private val apartApi = ApartmentApi.create()
    private val ApartRepository = ApartmentListingRepository()
    private val apartmentListings = MutableLiveData<List<ApartmentListing>>().apply {
        value = mutableListOf()
    }

    val commuteTimes = mutableMapOf<String, CommuteTimeInfo>()

    private val workAddress = MutableLiveData<String>().apply {
        value = "160 Spear St, San Francisco, CA"
    }

    private val walkScoreApi = WalkScoreApi.create()
    private val walkScoreRepository = WalkScoreRepository(walkScoreApi)
    private var currentWalkScore = MutableLiveData<WalkScore>()

    private val howLoudApi = HowLoudApi.create()
    private val howLoudRepository = HowLoudRepository(howLoudApi)
    private var currentHowLoudScore = MutableLiveData<HowLoudScore>()

    private val directionsApi = DirectionsApi.create()
    private val directionsRepository = DirectionsRepository(directionsApi)
    private var currentCommuteTime = MutableLiveData<String>()
    private var currentOverviewPolyline = MutableLiveData<String>()
    private var currentCommuteFare = MutableLiveData<String>()
    private var currentDirections = MutableLiveData<List<DirectionsApi.Steps>>()
    private var currentDistance = MutableLiveData<String>()
    private var currentDurationInTraffic = MutableLiveData<String>()

    fun populateListings() {
        val listingRef = db.collection("listing")
        val listings = mutableListOf<ApartmentListing>()
        listingRef
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val aListing = document.toObject(ApartmentListing::class.java)
                    listings.add(aListing)
                }
                apartmentListings.postValue(listings)
            }
    }

    fun getListings(): LiveData<List<ApartmentListing>> {
        return apartmentListings
    }

    fun setWorkAddress(address: String) {
        workAddress.value = address
    }

    fun getWorkAddress() : LiveData<String> {
        return workAddress
    }

    fun fetchWalkScore(address: String, lat: String, lon: String, key: String) {
        viewModelScope.launch(
                context = viewModelScope.coroutineContext
                        + Dispatchers.IO) {
            val callResponse = walkScoreRepository.getWalkScore(address, lat, lon, key)
            val response = callResponse.execute()
            if (response.isSuccessful) {
                currentWalkScore.postValue(response.body())
            }
        }
    }

    fun observeWalkScore(): LiveData<WalkScore> {
        return currentWalkScore
    }

    fun fetchHowLoudScore(address: String, key: String) {
        viewModelScope.launch(
                context = viewModelScope.coroutineContext
                        + Dispatchers.IO) {
            val callResponse = howLoudRepository.getHowLoudScore(address, key)
            val response = callResponse.execute()
            if (response.isSuccessful) {
                currentHowLoudScore.postValue(response.body()!!.result[0])
            }
        }
    }

    fun observeHowLoudScore(): LiveData<HowLoudScore> {
        return currentHowLoudScore
    }

    fun fetchDirections(origin: String, destination: String, mode: String, key: String) {
        viewModelScope.launch(
                context = viewModelScope.coroutineContext
                        + Dispatchers.IO) {
            val callResponse = directionsRepository.getDirections(origin, destination, mode, key)
            val response = callResponse.execute()
            if (response.isSuccessful) {
                currentCommuteTime.postValue(response.body()!!.routes[0].legs[0].duration.text)
                currentOverviewPolyline.postValue(response.body()!!.routes[0].overview_polyline.points)

                val fare = response.body()!!.routes[0].fare
                if (fare != null) {
                    currentCommuteFare.postValue(fare.text)
                } else {
                    currentCommuteFare.postValue("$0.00")
                }

                currentDistance.postValue(response.body()!!.routes[0].legs[0].distance.text)

                val durationInTraffic = response.body()!!.routes[0].legs[0].steps[0].duration_in_traffic
                if (durationInTraffic != null) {
                    currentDurationInTraffic.postValue(durationInTraffic.text)
                }

                var directions = ArrayList<DirectionsApi.Steps>()
                val steps = response.body()!!.routes[0].legs[0].steps
                for (i in 0 until steps.size) {
                    directions.add(steps[i])
                }
                currentDirections.postValue(directions)
            }
        }
    }

    fun observeCommuteTime(): LiveData<String> {
        return currentCommuteTime
    }

    fun observeOverviewPolyline(): LiveData<String> {
        return currentOverviewPolyline
    }

    fun observeCommuteFare(): LiveData<String> {
        return currentCommuteFare
    }

    fun observeDirections(): LiveData<List<DirectionsApi.Steps>> {
        return currentDirections
    }

    fun observeCurrentDistance(): LiveData<String> {
        return currentDistance
    }

    fun observeDurationInTraffic(): LiveData<String> {
        return currentDurationInTraffic
    }

    fun populateFavorites() {
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("CLOUD FIRESTORE", "DocumentSnapshot data: ${document.data}")
                    val profile = document.toObject(UserProfile::class.java)
                    favListings.postValue(profile?.favorites)
                } else {
                    Log.d("CLOUD FIRESTORE", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CLOUD FIRESTORE", "get failed with ", exception)
            }
    }

    fun addFav(favListing: ApartmentListing) {
        val localList = favListings.value?.toMutableList()
        localList?.let {
            it.add(favListing)
            favListings.value = it
        }
        val favoritesRef = db.collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        favoritesRef.update("favorites", FieldValue.arrayUnion(favListing))
    }

    fun removeFav(favListing: ApartmentListing) {
        val localList = favListings.value?.toMutableList()
        localList?.let {
            it.remove(favListing)
            favListings.value = it
        }
        val favoritesRef = db.collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        favoritesRef.update("favorites", FieldValue.arrayRemove(favListing))
    }

    fun isFav(listing: ApartmentListing): Boolean {
        for (fav in favListings.value.orEmpty()) {
            if (fav.address1 == listing.address1) {
                return true
            }
        }
        return false
    }

    fun getFav(): LiveData<List<ApartmentListing>> {
        return favListings
    }

}