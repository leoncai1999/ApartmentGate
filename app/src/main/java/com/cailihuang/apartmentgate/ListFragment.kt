package com.cailihuang.apartmentgate

import android.media.Image
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import kotlinx.android.synthetic.main.fragment_list.*
import android.widget.AdapterView


class ListFragment: Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var listAdapter: ListingAdapter

    companion object {
        fun newInstance(): ListFragment {
            return ListFragment()
        }
    }

    private fun initAdapter(root: View) {
        viewModel.initFirestore()
        val rv = root.findViewById<RecyclerView>(R.id.recyclerViewList)
        listAdapter = ListingAdapter(viewModel)
        rv.adapter = listAdapter
        rv.layoutManager = LinearLayoutManager(context)
    }

    private fun initializeLayoutElems() {
        ArrayAdapter.createFromResource(
            context!!,
            R.array.sort_by_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            sortSpinner.adapter = adapter
        }

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> println("sort by da da da")
                    1 -> println("etc")
                }

            }

        }

        filtersButton.setOnClickListener {
            // launch new filters fragment probably
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        val root = inflater.inflate(R.layout.fragment_list, container, false)

        initAdapter(root)
        viewModel.populateFavorites()



        val listings = mutableListOf<ApartmentListing>()
        val ref = FirebaseDatabase.getInstance().getReference("listings").child("TZAVBG6NoTmSCv1tFdhe").child("apartment")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                initializeLayoutElems()

                for (productSnapshot in dataSnapshot.children) {
                    val listing = productSnapshot.getValue(ApartmentListing::class.java)
                    listings.add(listing!!)
                }

                listAdapter.submitList(listings)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
        })

        return root
    }

}