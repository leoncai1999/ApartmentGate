package com.cailihuang.apartmentgate

import androidx.lifecycle.*
import com.cailihuang.apartmentgate.api.ApartmentApi
import com.cailihuang.apartmentgate.api.ApartmentListing
import com.cailihuang.apartmentgate.api.ApartmentListingRepository
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.AlgorithmParameterGenerator


class ListViewModel : ViewModel() {
    // XXX Write me

    //private val apartApi = ApartmentApi.create()
    private val ApartRepository = ApartmentListingRepository()
    private val apartmentListings = MutableLiveData<List<ApartmentListing>>().apply {
        value = mutableListOf()
    }
    private val workAddress = MutableLiveData<String>().apply {
        value = "160 Spear St, San Francisco, CA"
    }

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

    fun refresh() {
        fetchListings()
    }
}