package com.cailihuang.apartmentgate.api

import com.google.gson.Gson
import com.cailihuang.apartmentgate.MainActivity

class ApartmentListingRepository() {
    val gson = Gson()

    private fun unpackListings(response: ApartmentApi.ListingResponse): List<ApartmentListing>? {
        var posts = mutableListOf<ApartmentListing>()
        for (post in response.apartment) {
            posts.add(post)
        }
        return posts
    }

    fun getListings(): List<ApartmentListing>? {
        val response = gson.fromJson<ApartmentApi.ListingResponse>(
            MainActivity.jsonListings,
            ApartmentApi.ListingResponse::class.java)
        return unpackListings(response)
    }
}
