package com.cailihuang.apartmentgate

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer


import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_account.*

import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.user_profile_information.*

class ProfileFragment: Fragment() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    class SingleProfile {

        val budget = 0
        val demographic = ""
        val email = ""
        val maxCommuteTime = ""
        val size = 0
        val transportation = ""
        val walkability = ""
        val workAddress = ""
        val workEndHour = 0
        val workEndMin = 0
        val workStartHour = 0
        val workStartMin = 0


    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)
//        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFrag) as? SupportMapFragment
//        mapFragment?.getMapAsync(this)



        val user = FirebaseAuth.getInstance().currentUser
        // TODO check for null user

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        val currentUserDb = mDatabaseReference.child(user?.uid.toString())

        val ref = FirebaseDatabase.getInstance().getReference("Users").child(user?.uid.toString())
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val profile = dataSnapshot.getValue(SingleProfile::class.java)

                if (profile == null) {
                    println("PROFILE IS NULLO !!!")
                }
                println("Whats the email ??? " + profile?.email)


                // TODO make changing password separate activity???
                newEmailET.hint = profile?.email
                addressET.hint = profile?.workAddress
                startTime.hour = profile?.workStartHour!!
                startTime.minute = profile.workStartMin
                endTime.hour = profile.workEndHour
                endTime.minute = profile.workEndMin
                budgetET.hint = profile.budget.toString()
                sizeET.hint = profile.size.toString()


                when (profile.maxCommuteTime) {
                    "10 min" -> commuteTimeSpinner.setSelection(0)
                    "15 min" -> commuteTimeSpinner.setSelection(1)
                    "30 min" -> commuteTimeSpinner.setSelection(2)
                    "45 min" -> commuteTimeSpinner.setSelection(3)
                    "1 hour" -> commuteTimeSpinner.setSelection(4)
                }

                when (profile.transportation) {
                    "car" -> ViewCompat.setBackgroundTintList(carButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "transit" -> ViewCompat.setBackgroundTintList(transitButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "walk" -> ViewCompat.setBackgroundTintList(walkButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "bike" -> ViewCompat.setBackgroundTintList(bikeButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                }

                when (profile.walkability) {
                    "dontcare" -> ViewCompat.setBackgroundTintList(dontCareButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "care" -> ViewCompat.setBackgroundTintList(careButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "carealot" -> ViewCompat.setBackgroundTintList(careAlotButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                }

                when (profile.demographic) {
                    "families" -> ViewCompat.setBackgroundTintList(familiesButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "professionals" -> ViewCompat.setBackgroundTintList(professionalsButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                    "retirees" -> ViewCompat.setBackgroundTintList(retireesButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

        })


        //userEmailET.text = "hi"







        return rootView
    }



}