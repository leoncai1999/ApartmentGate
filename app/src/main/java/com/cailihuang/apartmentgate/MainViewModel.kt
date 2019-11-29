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
import com.google.firebase.firestore.FieldValue


class MainViewModel : ViewModel() {

    lateinit var db: FirebaseFirestore

    fun initFirestore() {
        db = FirebaseFirestore.getInstance()
        if (db == null) {
            Log.d("FirebaseFirestore", "FirebaseFirestore is null!")
        }
    }

    private var favPosts = MutableLiveData<List<ApartmentListing>>().apply {
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
    private var currentCommuteFare = MutableLiveData<String>()
    private var currentDirections = MutableLiveData<List<DirectionsApi.Steps>>()

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

                val fare = response.body()!!.routes[0].fare
                if (fare != null) {
                    currentCommuteFare.postValue(fare.text)
                } else {
                    currentCommuteFare.postValue("$0.00")
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

    fun observeCommuteFare(): LiveData<String> {
        return currentCommuteFare
    }

    fun observeDirections(): LiveData<List<DirectionsApi.Steps>> {
        return currentDirections
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
                    favPosts.postValue(profile?.favorites)
                } else {
                    Log.d("CLOUD FIRESTORE", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CLOUD FIRESTORE", "get failed with ", exception)
            }
    }

    fun addFav(favListing: ApartmentListing) {
        val favoritesRef = db.collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        favoritesRef.update("favorites", FieldValue.arrayUnion(favListing))
        populateFavorites()
    }

    fun removeFav(favListing: ApartmentListing) {
        val favoritesRef = db.collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid)
        favoritesRef.update("favorites", FieldValue.arrayRemove(favListing))

        println("REMOVE REMOVE REMOVE REMOVE REMOVE REMOVE REMOVE REMOVE")
        populateFavorites()
    }

    fun isFav(listing: ApartmentListing): Boolean {
        var isFav = false
        val isFavSema = Semaphore(1)
        val user = FirebaseAuth.getInstance().currentUser!!

        isFavSema.acquire()

        // PROBLEM AREA!!!

        val userRef = db.collection("Users").document(user?.uid.toString())
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("CLOUD FIRESTORE", "DocumentSnapshot data: ${document.data}")
                    val profile = document.toObject(UserProfile::class.java)
                    if (profile != null) {
                        for (fav in profile.favorites) {
                            if (fav.address == listing.address) {

                                println("ISFAV ISFAV ISFAV ISFAV ISFAV ISFAV")
                                isFav = true
                            }
                        }
                    }
                    isFavSema.release()
                } else {
                    Log.d("CLOUD FIRESTORE", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CLOUD FIRESTORE", "get failed with ", exception)
            }

        isFavSema.acquire()
        return isFav
    }

    fun getFav(): LiveData<List<ApartmentListing>> {
        return favPosts
    }

}