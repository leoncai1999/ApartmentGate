package com.cailihuang.apartmentgate

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.api.ApartmentListing
import kotlinx.android.synthetic.main.fragment_list.*
import android.content.res.Resources
import android.widget.*

class ListFragment: Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var listAdapter: ListingAdapter

    companion object {
        fun newInstance(): ListFragment {
            return ListFragment()
        }
    }

    private fun initAdapter(root: View) {
        val rv = root.findViewById<RecyclerView>(R.id.recyclerViewList)
        listAdapter = ListingAdapter(viewModel)
        rv.adapter = listAdapter
        rv.layoutManager = LinearLayoutManager(context)
    }

    private fun initializeLayoutElems(root: View) {
        ArrayAdapter.createFromResource(
            context!!,
            R.array.sort_by_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            root.findViewById<Spinner>(R.id.sortSpinner).adapter = adapter
        }

        root.findViewById<Spinner>(R.id.sortSpinner).onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val res: Resources = resources
                val sortByArray = res.getStringArray(R.array.sort_by_array)
                viewModel.sortBy = sortByArray[position]
                viewModel.populateListings()
            }
        }

        root.findViewById<Button>(R.id.filtersButton).setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.main_frame, FiltersFragment.newInstance())
                .addToBackStack(null)
                .commit()
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
        initializeLayoutElems(root)

        viewModel.getListings().observe(this, Observer {
            listAdapter.submitList(it)

            //recyclerViewList.smoothScrollToPosition(0)
        })

        // Used to convert Cloud Firestore string fields to Int so that they're sortable
        //convertFirestoreStringToInts()

        // Realtime database code
//        val listings = mutableListOf<ApartmentListing>()
//        ref = FirebaseDatabase.getInstance().getReference("listings").child("TZAVBG6NoTmSCv1tFdhe").child("apartment")
//
//        ref.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                initializeLayoutElems()
//
//                for (productSnapshot in dataSnapshot.children) {
//                    val listing = productSnapshot.getValue(ApartmentListing::class.java)
//                    listings.add(listing!!)
//                }
//
//                listAdapter.submitList(listings)
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                throw databaseError.toException()
//            }
//        })

        viewModel.populateListings()



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapButton.setOnClickListener {
            (activity as MainActivity).setFragment(MapFragment.newInstance())
        }

        upButton.setOnClickListener {
            recyclerViewList.smoothScrollToPosition(0)
        }
    }

    private fun convertFirestoreStringToInts() {
        val listingRef = viewModel.db.collection("listing")
        listingRef
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d("LISTING", "${document.id} => ${document.data}")
                    val aListing = document.toObject(ApartmentListing::class.java)
                    val docRef = viewModel.db.collection("listing").document(document.id)

//                    if (!(aListing.baths == "" || aListing.beds == "" ||
//                                aListing.size == "" || aListing.deposit == "" || aListing.rent == "")) {
//                        var beds = 0
//                        if (!(aListing.beds == "studio" || aListing.beds == "Studio")) {
//                            beds = makeIntFromString(aListing.beds)
//                        }
//                        val baths = makeIntFromString(aListing.baths)
//                        val deposit = makeIntFromString(aListing.deposit)
//                        val rent = makeIntFromString(aListing.rent)
//                        val size = makeIntFromString(aListing.size)
//
//                        docRef
//                            .update("baths", baths)
//                            .addOnSuccessListener { Log.d("LISTING UPDATE", "DocumentSnapshot successfully updated!") }
//                            .addOnFailureListener { e -> Log.w("LISTING UPDATE", "Error updating document", e) }
//                        docRef
//                            .update("beds", beds)
//                            .addOnSuccessListener { Log.d("LISTING UPDATE", "DocumentSnapshot successfully updated!") }
//                            .addOnFailureListener { e -> Log.w("LISTING UPDATE", "Error updating document", e) }
//                        docRef
//                            .update("deposit", deposit)
//                            .addOnSuccessListener { Log.d("LISTING UPDATE", "DocumentSnapshot successfully updated!") }
//                            .addOnFailureListener { e -> Log.w("LISTING UPDATE", "Error updating document", e) }
//                        docRef
//                            .update("rent", rent)
//                            .addOnSuccessListener { Log.d("LISTING UPDATE", "DocumentSnapshot successfully updated!") }
//                            .addOnFailureListener { e -> Log.w("LISTING UPDATE", "Error updating document", e) }
//                        docRef
//                            .update("size", size)
//                            .addOnSuccessListener { Log.d("LISTING UPDATE", "DocumentSnapshot successfully updated!") }
//                            .addOnFailureListener { e -> Log.w("LISTING UPDATE", "Error updating document", e) }
//                    } else {
//                        docRef.delete()
//                            .addOnSuccessListener { Log.d("LISTING DELETE", "DocumentSnapshot successfully deleted!") }
//                            .addOnFailureListener { e -> Log.w("LISTING DELETE", "Error deleting document", e) }
//                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("LISTING", "Error getting documents: ", exception)
            }
    }

    private fun makeIntFromString(s: String): Int {
        var sNew = ""
        for (i in (0 .. (s.length-1))){
            if (isIntChar(s[i])) {
                sNew += s[i]
            }
        }
        var i = sNew.toInt()
        return i
    }

    private fun isIntChar(c: Char): Boolean {
        if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4' ||
                c == '5' || c == '6' || c == '7' || c == '8' || c == '9') {
            return true
        }
        return false
    }

}