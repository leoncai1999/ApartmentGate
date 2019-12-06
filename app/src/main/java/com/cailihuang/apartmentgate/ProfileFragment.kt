package com.cailihuang.apartmentgate

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.fragment_one_listing.view.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.user_profile_information.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.util.*

class ProfileFragment: Fragment() {

    // they would need to interact with FirebaseAuth
    private lateinit var viewModel: MainViewModel

    private lateinit var preferredTransport: String
    private lateinit var demographic: String
    private lateinit var walkability: String
    private lateinit var geocoder: Geocoder

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

        rootView.findViewById<TimePicker>(R.id.startTime).setIs24HourView(true)
        rootView.findViewById<TimePicker>(R.id.endTime).setIs24HourView(true)

        geocoder = Geocoder(activity, Locale.getDefault())

        val user = FirebaseAuth.getInstance().currentUser
        val userRef = viewModel.db.collection("Users").document(user?.uid.toString())
        userRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("CLOUD FIRESTORE", "DocumentSnapshot data: ${document.data}")
                    val profile = document.toObject(UserProfile::class.java)

                    val commuteTimeSpinnerRef = rootView.findViewById<Spinner>(R.id.commuteTimeSpinner)
                    val transportationTimeSpinnerRef = rootView.findViewById<Spinner>(R.id.transportationSpinner)
                    val demographicSpinnerRef = rootView.findViewById<Spinner>(R.id.demographicSpinner)
                    val walkabilitySpinnerRef = rootView.findViewById<Spinner>(R.id.walkabilitySpinner)


                    val commuteSpinnerAdapter = ArrayAdapter.createFromResource(
                        context!!,
                        R.array.commute_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        commuteTimeSpinnerRef.adapter = adapter
                    }

                    val transportationSpinnerAdapter = ArrayAdapter.createFromResource(
                        context!!,
                        R.array.transportation_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        transportationTimeSpinnerRef.adapter = adapter
                    }

                    val demographicSpinnerAdapter = ArrayAdapter.createFromResource(
                        context!!,
                        R.array.demographic_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        demographicSpinnerRef.adapter = adapter
                    }

                    val walkabilitySpinnerAdapter = ArrayAdapter.createFromResource(
                        context!!,
                        R.array.walkability_array,
                        android.R.layout.simple_spinner_item
                    ).also { adapter ->
                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        // Apply the adapter to the spinner
                        walkabilitySpinnerRef.adapter = adapter
                    }

                    //val newEmailRef = rootView.findViewById<EditText>(R.id.newEmailET)
                    val newPwRef = rootView.findViewById<EditText>(R.id.newPasswordET)
                    val newConfPwRef = rootView.findViewById<EditText>(R.id.confirmPasswordET)

                    rootView.findViewById<Button>(R.id.changeProfileButton).setOnClickListener {

                        // passwords match
                        // password more than eight characters
                        if (!newPwRef.text.isEmpty() || !newConfPwRef.text.isEmpty()) {
                            if (newPwRef.text.toString() != newConfPwRef.text.toString()) {
                                Toast.makeText(context, "Your passwords must match.", Toast.LENGTH_LONG).show()
                            } else if (newPwRef.text.toString().length < 8) {
                                Toast.makeText(context, "Password must be at least eight characters long.", Toast.LENGTH_LONG).show()
                            } else {
                                println("UPDATING PASSWORD!!!")
                                user!!.updatePassword(newPwRef.text.toString())
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Password updated.", Toast.LENGTH_LONG).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Password failed to update.", Toast.LENGTH_LONG).show()
                                    }
                            }
                        }

                        if (rootView.findViewById<EditText>(R.id.addressET).text.isNotEmpty()) {
                            val address = geocoder.getFromLocationName(addressET.text.toString(), 1)
                            if (address == null || address.size == 0) {
                                Toast.makeText(context, "Must enter a valid address.", Toast.LENGTH_LONG).show()
                            } else {
                                profile?.workAddress =
                                    rootView.findViewById<EditText>(R.id.addressET).text.toString()
                            }
                        }

                        profile?.workStartHour = rootView.findViewById<TimePicker>(R.id.startTime).hour
                        profile?.workStartMin = rootView.findViewById<TimePicker>(R.id.startTime).minute
                        profile?.workEndHour = rootView.findViewById<TimePicker>(R.id.endTime).hour
                        profile?.workEndMin = rootView.findViewById<TimePicker>(R.id.endTime).minute
                        profile?.maxCommuteTime = commuteTimeSpinnerRef.selectedItem.toString()
                        profile?.transportation = transportationTimeSpinnerRef.selectedItem.toString().toLowerCase()
                        profile?.demographic = demographicSpinnerRef.selectedItem.toString().toLowerCase()
                        profile?.walkability = walkabilitySpinnerRef.selectedItem.toString().replace(" ", "").replace("\'", "").toLowerCase()
                        if (rootView.findViewById<EditText>(R.id.budgetET).text.isNotEmpty()) {
                            profile?.budget = Integer.parseInt(rootView.findViewById<EditText>(R.id.budgetET).text.toString())
                        }
                        if (rootView.findViewById<EditText>(R.id.sizeET).text.isNotEmpty()) {
                            profile?.size = Integer.parseInt(rootView.findViewById<EditText>(R.id.sizeET).text.toString())
                        }

                        userRef.set(profile!!)
                        println("PROFILE SET!!!")
                        val lastUserIdRef = viewModel.db.collection("LastUser").document("lastuser")
                        lastUserIdRef.update("userid", "")
                        (activity as MainActivity).setFragment(MapFragment.newInstance())
                    }

                    rootView.findViewById<Button>(R.id.signOutButton).setOnClickListener {
                        FirebaseAuth.getInstance().signOut()
                        val welcomeIntent = Intent(context, WelcomeActivity::class.java)
                        startActivity(welcomeIntent)
                    }

                    //rootView.findViewById<EditText>(R.id.newEmailET).hint = profile?.email
                    rootView.findViewById<EditText>(R.id.addressET).hint = profile?.workAddress
                    rootView.findViewById<TimePicker>(R.id.startTime).hour = profile?.workStartHour!!
                    rootView.findViewById<TimePicker>(R.id.startTime).minute = profile.workStartMin
                    rootView.findViewById<TimePicker>(R.id.endTime).hour = profile.workEndHour
                    rootView.findViewById<TimePicker>(R.id.endTime).minute = profile.workEndMin
                    rootView.findViewById<EditText>(R.id.budgetET).hint = profile.budget.toString()
                    rootView.findViewById<EditText>(R.id.sizeET).hint = profile.size.toString()

                    commuteTimeSpinnerRef.setSelection(commuteSpinnerAdapter.getPosition(profile.maxCommuteTime))
                    val transSpinString = profile.transportation.substring(0, 1).toUpperCase() + profile.transportation.substring(1, profile.transportation.length)
                    transportationTimeSpinnerRef.setSelection(transportationSpinnerAdapter.getPosition(transSpinString))
                    val demSpinString = profile.demographic.substring(0, 1).toUpperCase() + profile.demographic.substring(1, profile.demographic.length)
                    demographicSpinnerRef.setSelection(demographicSpinnerAdapter.getPosition(demSpinString))
                    var walkSpinString = ""
                    when (profile.walkability) {
                        "Don\'t care" -> walkSpinString = "dontcare"
                        "Care" -> walkSpinString = "care"
                        "Care a lot" -> walkSpinString = "carealot"
                    }
                    walkabilitySpinnerRef.setSelection(walkabilitySpinnerAdapter.getPosition(walkSpinString))

                    preferredTransport = profile.transportation
                    walkability = profile.walkability
                    demographic = profile.demographic

                    /*

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

                     */
                } else {
                    Log.d("CLOUD FIRESTORE", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("CLOUD FIRESTORE", "get failed with ", exception)
            }

        return rootView
    }

}