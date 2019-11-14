package com.cailihuang.apartmentgate

import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.cailihuang.apartmentgate.api.ApartmentListing
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ListFragment: Fragment() {
    private lateinit var viewModel: ListViewModel
    private lateinit var listAdapter: ListingAdapter

    companion object {
        fun newInstance(): ListFragment {
            return ListFragment()
        }
    }

    private fun initAdapter(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerView)
        //listAdapter = ListingAdapter(viewModel)
        listAdapter = ListingAdapter()
        rv.adapter = listAdapter
        rv.layoutManager = LinearLayoutManager(context)

        viewModel.observeListings().observe(this, Observer {
            listAdapter.submitList(it)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProviders.of(this)[ListViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val root = inflater.inflate(R.layout.fragment_list, container, false)

        initAdapter(root)
        viewModel.refresh()

        val listings = arrayListOf<ApartmentListing>()
        val ref = FirebaseDatabase.getInstance().getReference("apartment")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (productSnapshot in dataSnapshot.children) {
                    val listing = productSnapshot.getValue(ApartmentListing::class.java)
                    listings.add(listing!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        })


        listAdapter.submitList(listings)

        return root
    }
}