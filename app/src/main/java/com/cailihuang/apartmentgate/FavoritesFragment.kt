package com.cailihuang.apartmentgate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cailihuang.apartmentgate.api.ApartmentListing
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.user_profile_information.*

class FavoritesFragment: Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var listAdapter: ListingAdapter

    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }

    private fun initAdapter(root: View) {
        viewModel.initFirestore()
        val rv = root.findViewById<RecyclerView>(R.id.recyclerViewFav)
        listAdapter = ListingAdapter(viewModel)
        rv.adapter = listAdapter
        rv.layoutManager = LinearLayoutManager(context)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_favorites, container, false)
        viewModel = activity?.run {
            ViewModelProviders.of(this)[MainViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

        initAdapter(root)
        viewModel.populateFavorites()

        viewModel.getFav().observe(this, Observer {
            listAdapter.submitList(it)
        })

        return root
    }

}