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
        if (db == null) {
            Log.d("FirebaseFirestore", "FirebaseFirestore is null!")
        }
        val user = FirebaseAuth.getInstance().currentUser!!
        userRef = db.collection("Users").document(user.uid.toString())
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


    private fun fetchListings() = viewModelScope.launch(
        context = viewModelScope.coroutineContext
                + Dispatchers.IO) {
        // Update LiveData from IO dispatcher, use postValue
        apartmentListings.postValue(ApartRepository.getListings())
    }

    fun observeListings(): LiveData<List<ApartmentListing>> {
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
            }
        }
    }

    fun observeCommuteTime(): LiveData<String> {
        return currentCommuteTime
    }

    fun observeOverviewPolyline(): LiveData<String> {
        return currentOverviewPolyline
    }

    // to be used for filtering, sorting
    fun refresh() {
        fetchListings()
    }

    fun populateFavorites() {
        val user = FirebaseAuth.getInstance().currentUser!!
        val userRef = db.collection("Users").document(user?.uid.toString())
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
            if (fav.address == listing.address) {
                return true
            }
        }

        return false
    }

    fun getFav(): LiveData<List<ApartmentListing>> {
        return favListings
    }

}