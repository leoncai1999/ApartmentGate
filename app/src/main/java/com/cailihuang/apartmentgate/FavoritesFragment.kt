package com.cailihuang.apartmentgate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.fragment.app.Fragment
import com.cailihuang.apartmentgate.api.ApartmentListing
import com.google.firebase.database.*

import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.user_profile_information.*

class FavoritesFragment: Fragment() {

    private lateinit var userRef: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorites, container, false)

        val user = FirebaseAuth.getInstance().currentUser!!
        // TODO check for null user (not logged in)

        val mDatabase = FirebaseDatabase.getInstance()
        val mDatabaseReference = mDatabase.reference.child("Users")
        val currentUserDb = mDatabaseReference.child(user.uid.toString())
        val listings = mutableListOf<ApartmentListing>()

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.uid).child("favorites")
        userRef.addValueEventListener(object : ValueEventListener {

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

        return rootView
    }

    fun isFav() {








    }

    fun addFav() {



    }

    fun removeFav() {

    }
}