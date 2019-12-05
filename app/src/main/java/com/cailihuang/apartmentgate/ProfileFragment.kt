package com.cailihuang.apartmentgate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.user_profile_information.*

class ProfileFragment: Fragment() {

    // TODO  implement changing login info (email, password)
    // they would need to interact with FirebaseAuth
    private lateinit var viewModel: MainViewModel

    private lateinit var preferredTransport: String
    private lateinit var demographic: String
    private lateinit var walkability: String

    companion object {
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_profile, container, false)

        viewModel = MainViewModel()
        viewModel.initFirestore()

        val user = FirebaseAuth.getInstance().currentUser
        val userRef = viewModel.db.collection("Users").document(user?.uid.toString())
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("CLOUD FIRESTORE", "DocumentSnapshot data: ${document.data}")
                    val profile = document.toObject(UserProfile::class.java)

                    initializeLayoutElems()

                    val commuteSpinnerAdapter = ArrayAdapter.createFromResource(
                        context!!,
                        R.array.commute_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        commuteTimeSpinner.adapter = adapter
                    }

                    changeProfileButton.setOnClickListener {
                        if (newEmailET.text.isNotEmpty()) {
                            profile?.email = newEmailET.text.toString()
                        }
                        if (addressET.text.isNotEmpty()) {
                            profile?.workAddress = addressET.text.toString()
                        }

                        profile?.workStartHour = startTime.hour
                        profile?.workStartMin = startTime.minute
                        profile?.workEndHour = endTime.hour
                        profile?.workEndMin = endTime.minute
                        profile?.maxCommuteTime = commuteTimeSpinner.selectedItem.toString()
                        profile?.transportation = preferredTransport
                        profile?.demographic = demographic
                        profile?.walkability = walkability
                        if (budgetET.text.isNotEmpty()) {
                            profile?.budget = Integer.parseInt(budgetET.text.toString())
                        }
                        if (sizeET.text.isNotEmpty()) {
                            profile?.size = Integer.parseInt(sizeET.text.toString())
                        }

                        userRef.set(profile!!)
                        val lastUserIdRef = viewModel.db.collection("LastUser").document("lastuser")
                        lastUserIdRef.update("userid", "")
                        (activity as MainActivity).setFragment(MapFragment.newInstance())
                    }

                    signOutButton.setOnClickListener {
                        FirebaseAuth.getInstance().signOut()
                        val welcomeIntent = Intent(context, WelcomeActivity::class.java)
                        startActivity(welcomeIntent)
                    }

                    newEmailET.hint = profile?.email
                    addressET.hint = profile?.workAddress
                    startTime.hour = profile?.workStartHour!!
                    startTime.minute = profile.workStartMin
                    endTime.hour = profile.workEndHour
                    endTime.minute = profile.workEndMin
                    budgetET.hint = profile.budget.toString()
                    sizeET.hint = profile.size.toString()

                    commuteTimeSpinner.setSelection(commuteSpinnerAdapter.getPosition(profile.maxCommuteTime))

                    preferredTransport = profile.transportation
                    walkability = profile.walkability
                    demographic = profile.demographic

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
                } else {
                    Log.d("CLOUD FIRESTORE", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CLOUD FIRESTORE", "get failed with ", exception)
            }

        return rootView
    }

    private fun initializeLayoutElems() {

        startTime.setIs24HourView(true)
        endTime.setIs24HourView(true)

        // TODO reset color when another option selected
        carButton.setOnClickListener {
            preferredTransport = "driving"
            //changeButtonsColors(carButton, transitButton, walkButton, bikeButton)
            ViewCompat.setBackgroundTintList(carButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        transitButton.setOnClickListener {
            preferredTransport = "transit"
            //changeButtonsColors(transitButton, carButton, walkButton, bikeButton)
            ViewCompat.setBackgroundTintList(transitButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        walkButton.setOnClickListener {
            preferredTransport = "walking"
            //changeButtonsColors(walkButton, carButton, transitButton, bikeButton)
            ViewCompat.setBackgroundTintList(walkButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        bikeButton.setOnClickListener {
            preferredTransport = "bicycling"
            //changeButtonsColors(bikeButton, walkButton, carButton, transitButton)
            ViewCompat.setBackgroundTintList(bikeButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }

        familiesButton.setOnClickListener {
            demographic = "families"
            //changeButtonsColors(familiesButton, professionalsButton, retireesButton)
            ViewCompat.setBackgroundTintList(familiesButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        professionalsButton.setOnClickListener {
            demographic = "professionals"
            //changeButtonsColors(professionalsButton, familiesButton, retireesButton)
            ViewCompat.setBackgroundTintList(professionalsButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        retireesButton.setOnClickListener {
            demographic = "retirees"
            //changeButtonsColors(retireesButton, familiesButton, professionalsButton)
            ViewCompat.setBackgroundTintList(retireesButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }

        dontCareButton.setOnClickListener {
            walkability = "dontcare"
            //changeButtonsColors(dontCareButton, careButton, careAlotButton)
            ViewCompat.setBackgroundTintList(dontCareButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        careButton.setOnClickListener {
            walkability = "care"
            //changeButtonsColors(careButton, dontCareButton, careAlotButton)
            ViewCompat.setBackgroundTintList(careButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
        careAlotButton.setOnClickListener {
            walkability = "carealot"
            //changeButtonsColors(careAlotButton, dontCareButton, careButton)
            ViewCompat.setBackgroundTintList(careAlotButton, ContextCompat.getColorStateList(context!!, android.R.color.holo_blue_dark))
        }
    }

}