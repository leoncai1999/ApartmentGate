package com.cailihuang.apartmentgate

import androidx.lifecycle.*
import android.content.Context
import android.content.Intent
import android.location.Geocoder
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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainViewModel : ViewModel() {

    lateinit var db: FirebaseFirestore
    lateinit var userRef: DocumentReference
    lateinit var currentUserProfile: UserProfile

    fun initFirestore() {
        println("uno")
        db = FirebaseFirestore.getInstance()
        println("dos")
        val user = FirebaseAuth.getInstance().currentUser!!
        println("tres")
        println("user id is: " + user.uid)
        userRef = db.collection("Users").document(user.uid)
        println("4")
        userRef.get()
            .addOnSuccessListener { document ->
                println("5")
                if (document != null) {
                    Log.d("CLOUD FIRESTORE", "DocumentSnapshot data: ${document.data}")
                    currentUserProfile = document.toObject(UserProfile::class.java)!!
                } else {
                    Log.d("CLOUD FIRESTORE", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CLOUD FIRESTORE", "get failed with ", exception)
            }
    }

    private var favListings = MutableLiveData<List<ApartmentListing>>().apply {
        value = mutableListOf()
    }

    //private val apartApi = ApartmentApi.create()
    private val ApartRepository = ApartmentListingRepository()
    private val apartmentListings = MutableLiveData<List<ApartmentListing>>().apply {
        value = mutableListOf()
    }

    var sortBy = ""
    var rentLimit = 0
    var minSize = 0
    var minBeds = 0
    var commuteTimeLimit = ""

    private var neighborhoods = MutableLiveData<List<Neighborhood>>().apply {
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

    private lateinit var geocoder: Geocoder

    fun populateListings() {
        val listingRefFiltered = getFilterListingRef()
        val listingRef = getSortListingRef(listingRefFiltered)

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

    private fun getSortListingRef(listingRefFiltered: Query): Query {
        when (sortBy) {
            "Rent low to high" -> return listingRefFiltered.orderBy("rent")
            "Rent high to low" -> return listingRefFiltered.orderBy("rent", Query.Direction.DESCENDING)
            "Size low to high" -> return listingRefFiltered.orderBy("size")
            "Size high to low" -> return listingRefFiltered.orderBy("size", Query.Direction.DESCENDING)
            "Commute time" -> println("IDK about this one yet")
        }

        return listingRefFiltered
    }

    private fun getFilterListingRef(): Query {
        if (rentLimit != 0) {
            return db.collection("listing").whereLessThanOrEqualTo("rent", rentLimit).orderBy("rent")
        }
        if (minSize != 0) {
            return db.collection("listing").whereGreaterThanOrEqualTo("size", minSize).orderBy("size")
        }
        if (minBeds != 0) {
            return db.collection("listing").whereGreaterThanOrEqualTo("beds", minBeds).orderBy("beds")
        }
        when (commuteTimeLimit) {
            "10 min" -> println("Haven't figured this one out yet")
        }

        return db.collection("listing")
    }

    fun resetFilters() {
        rentLimit = 0
        minSize = 0
        minBeds = 0
        commuteTimeLimit = ""
    }

    fun getListings(): LiveData<List<ApartmentListing>> {
        return apartmentListings
    }

    fun populateNeighborhoods() {
        var neighbhoodRef = db.collection("Neighborhoods").orderBy("favorites", Query.Direction.DESCENDING)
        val cityNeighborhoods = mutableListOf<Neighborhood>()
        neighbhoodRef.get().addOnSuccessListener { result ->
            for (document in result) {
                val neighborhood = document.toObject(Neighborhood::class.java)
                println(neighborhood.toString())
                cityNeighborhoods.add(neighborhood)
            }
            println("list to post is: " + cityNeighborhoods.toString())
            neighborhoods.postValue(cityNeighborhoods)
        }
    }

    fun getNeighborhoods(): LiveData<List<Neighborhood>> {
        return neighborhoods
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

    fun getWalkScore(address: String, lat: String, lon: String): Int {
        val callResponse = walkScoreRepository.getWalkScore(address, lat, lon, APIKeys.walkscoreAPIKey)
        val response = callResponse.execute()
        return response.body()!!.walkscore

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

    fun getSoundScore(address: String): Int {
        val callResponse = howLoudRepository.getHowLoudScore(address, APIKeys.soundscoreAPIKey)
        val response = callResponse.execute()
        return response.body()!!.result[0].score
    }

    fun observeHowLoudScore(): LiveData<HowLoudScore> {
        return currentHowLoudScore
    }

    fun fetchDirections(origin: String, destination: String, mode: String, arrivalTime: String, key: String) {
        viewModelScope.launch(
                context = viewModelScope.coroutineContext
                        + Dispatchers.IO) {
            val callResponse = directionsRepository.getDirections(origin, destination, mode, arrivalTime, key)
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

    fun updateTrendingNeighborhoods(listing: ApartmentListing, isRemove: Boolean) {
        // forward slash character must be escaped for database
        val listingNeighborhood = listing.neighborhood.replace("/", " & ")
        val neighborhoodRef = db.collection("Neighborhoods").document(listingNeighborhood)
        neighborhoodRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                var favoriteCount = document.toObject(Neighborhood::class.java)!!.favorites
                var newFavoriteCount = 0
                /* A neighborhood's averageRent is the aggregate of all favorited listings matching
                that neighborhood among all users */
                var averageRent = document.toObject(Neighborhood::class.java)!!.average_rent
                var newAverageRent = 0
                if (isRemove) {
                    newFavoriteCount = favoriteCount - 1
                    if (newFavoriteCount == 0) {
                        neighborhoodRef.delete()
                    } else {
                        newAverageRent = (averageRent*favoriteCount - listing.rent)/newFavoriteCount
                        neighborhoodRef.update("favorites", newFavoriteCount)
                        neighborhoodRef.update("average_rent", newAverageRent)
                    }
                } else {
                    newFavoriteCount = favoriteCount + 1
                    newAverageRent = (averageRent*favoriteCount + listing.rent)/newFavoriteCount
                    neighborhoodRef.update("favorites", newFavoriteCount)
                    neighborhoodRef.update("average_rent", newAverageRent)
                }
            } else {
                /* Scenario where the the neighborhood hasn't previously being seen in a
                favorited listing before */
                val neighborhoodData = hashMapOf(
                        "average_rent" to listing.rent,
                        "favorites" to 1,
                        // TODO: Find a way to get unique images for each neighborhood
                        "image_url" to "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5c/San_Francisco_%285222422754%29_%282%29.jpg/640px-San_Francisco_%285222422754%29_%282%29.jpg?1575182093536",
                        "name" to listing.neighborhood
                )
                neighborhoodRef.set(neighborhoodData)
            }
        }
    }


    fun calculateApartmentScore(listing: ApartmentListing,apartmentCoords: String, workCoords: String): Int {
        val userProfile = currentUserProfile

        // Component 1: Commute
        var commuteScore = 100
        var maxCommute = userProfile.maxCommuteTime.substringBefore(" ").toInt()
        // maxCommute is 1 hour, convert to 60 min
        if (maxCommute == 1) {
            maxCommute = 60
        }
        val directionsApi = DirectionsApi.create()
        val directionsRepository = DirectionsRepository(directionsApi)
        var commuteTimeValue = 0
        val getCommuteSema = Semaphore(1)
        getCommuteSema.acquire()


        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO) {
            val callResponse = directionsRepository.getDirections(apartmentCoords, workCoords, userProfile.transportation, dateToEpoch().toString(), APIKeys.googleMapsAPIKey)
            val response = callResponse.execute()
            commuteTimeValue = response.body()!!.routes[0].legs[0].duration.value
            getCommuteSema.release()
        }

        getCommuteSema.acquire()
        getCommuteSema.release()
        commuteTimeValue /= 60
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
        val walkScore = listing.walkScore

        // Component 4: Affordability
        var affordabilityScore = 100
        val idealRent = userProfile.budget
        if (idealRent < listing.rent) {
            val pointCost = idealRent / 100
            affordabilityScore -= (listing.rent - idealRent) / pointCost
        }

        // Component 5: Size
        var sizeScore = 100
        val idealSize = userProfile.size
        if (idealSize < listing.size) {
            sizeScore -= (idealSize - listing.size)/2
            if (sizeScore < 0) {
                sizeScore = 0
            }
        }

        return (commuteScore + atmosphereScore + walkScore + affordabilityScore + sizeScore)/5
    }

    private fun dateToEpoch(): Long {
        var workStartMinString = currentUserProfile.workStartMin.toString()
        if (workStartMinString == "0") {
            workStartMinString += "0"
        }
        // add 8 hours to conver to UTC
        var workStartHourString = (currentUserProfile.workStartHour + 8).toString()

        println(" WHAT IS THE STRING ??? " + workStartHourString)
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