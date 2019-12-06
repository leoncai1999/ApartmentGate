package com.cailihuang.apartmentgate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_create_account.*
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ArrayAdapter
import android.app.Activity
import android.graphics.Color
import android.widget.AdapterView
import android.graphics.Typeface
import android.location.Geocoder
import androidx.core.view.ViewCompat
import androidx.core.content.ContextCompat
import android.widget.Button
import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.user_profile_information.*
import kotlinx.coroutines.sync.Semaphore
import java.util.*

// TODO potentially add nickname

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MainViewModel

    private lateinit var preferredTransport: String
    private lateinit var demographic: String
    private lateinit var walkability: String
    private lateinit var geocoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        initializeFirebase()
        initializeLayoutElems()

        createProfileButton.setOnClickListener {
            if (fieldsAllFilled()) {
                createNewAccount()
            }}

        geocoder = Geocoder(this, Locale.getDefault())
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        viewModel = MainViewModel()
        viewModel.db = FirebaseFirestore.getInstance()
    }

    private fun initializeLayoutElems() {

        startTime.setIs24HourView(true)
        endTime.setIs24HourView(true)

        /*

        carButton.setOnClickListener {
            preferredTransport = "driving"

            println("DRIVING BUTTON PRESSED")
            ViewCompat.setBackgroundTintList(transitButton, ContextCompat.getColorStateList(this, android.R.color.background_light))
            ViewCompat.setBackgroundTintList(walkButton, ContextCompat.getColorStateList(this, android.R.color.background_light))
            ViewCompat.setBackgroundTintList(bikeButton, ContextCompat.getColorStateList(this, android.R.color.background_light))
            //changeButtonsColors(transitButton, walkButton, bikeButton)
            ViewCompat.setBackgroundTintList(carButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        transitButton.setOnClickListener {
            preferredTransport = "transit"

            println("TRANSIT BUTTON PRESSED")
            carButton.setBackground(createProfileButton.background)
            walkButton.setBackground(createProfileButton.background)
            bikeButton.setBackground(createProfileButton.background)
            //changeButtonsColors(carButton, walkButton, bikeButton)
            ViewCompat.setBackgroundTintList(transitButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        walkButton.setOnClickListener {
            preferredTransport = "walking"

            println("WALK BUTTON PRESSED")
            carButton.setBackground(createProfileButton.background)
            transitButton.setBackground(createProfileButton.background)
            bikeButton.setBackground(createProfileButton.background)
            //changeButtonsColors(carButton, transitButton, bikeButton)
            ViewCompat.setBackgroundTintList(walkButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        bikeButton.setOnClickListener {
            preferredTransport = "bicycling"

            println("BIKE BUTTON PRESSED")
            carButton.setBackground(createProfileButton.background)
            walkButton.setBackground(createProfileButton.background)
            walkButton.setBackground(createProfileButton.background)
            //changeButtonsColors(walkButton, carButton, transitButton)
            ViewCompat.setBackgroundTintList(bikeButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }

        familiesButton.setOnClickListener {
            demographic = "families"
            changeButtonsColors(professionalsButton, retireesButton)
            ViewCompat.setBackgroundTintList(familiesButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        professionalsButton.setOnClickListener {
            demographic = "professionals"
            changeButtonsColors(familiesButton, retireesButton)
            ViewCompat.setBackgroundTintList(professionalsButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        retireesButton.setOnClickListener {
            demographic = "retirees"
            changeButtonsColors(familiesButton, professionalsButton)
            ViewCompat.setBackgroundTintList(retireesButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }

        dontCareButton.setOnClickListener {
            walkability = "dontcare"
            changeButtonsColors(careButton, careAlotButton)
            ViewCompat.setBackgroundTintList(dontCareButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        careButton.setOnClickListener {
            walkability = "care"
            changeButtonsColors(dontCareButton, careAlotButton)
            ViewCompat.setBackgroundTintList(careButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        careAlotButton.setOnClickListener {
            walkability = "carealot"
            changeButtonsColors(dontCareButton, careButton)
            ViewCompat.setBackgroundTintList(careAlotButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }

        */

        ArrayAdapter.createFromResource(
            this,
            R.array.commute_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            commuteTimeSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.transportation_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            transportationSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.demographic_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            demographicSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.walkability_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            walkabilitySpinner.adapter = adapter
        }
    }

    private fun changeButtonsColors(otherButton1: Button, otherButton2: Button, otherButton3: Button? = null) {
        otherButton1.setBackground(createProfileButton.background)
        otherButton2.setBackground(createProfileButton.background)
        otherButton3?.setBackground(createProfileButton.background)
    }

    private fun createNewAccount() {

        val email = newEmailET.text.toString()
        val password = newPasswordET.text.toString()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = auth.getCurrentUser()
                Toast.makeText(this, "New user email: " + user?.email + "\nPlease be patient as we calculate your scores.", Toast.LENGTH_LONG).show()

                var newUser = UserProfile()

                newUser.email = newEmailET.text.toString()
                newUser.workAddress= addressET.text.toString()
                newUser.workStartHour = startTime.hour
                newUser.workStartMin = startTime.minute
                newUser.workEndHour = endTime.hour
                newUser.workEndMin = endTime.minute
                newUser.transportation = transportationSpinner.selectedItem.toString().toLowerCase()
                newUser.maxCommuteTime = commuteTimeSpinner.selectedItem.toString()
                newUser.demographic = demographicSpinner.selectedItem.toString().toLowerCase()
                newUser.walkability = walkabilitySpinner.selectedItem.toString().replace(" ", "").replace("\'", "").toLowerCase()
                newUser.budget = Integer.parseInt(budgetET.text.toString())
                newUser.size = Integer.parseInt(sizeET.text.toString())

                viewModel.db.collection("Users")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).set(newUser)
                    .addOnSuccessListener { documentReference ->
                        Log.d("CLOUD FIRESTORE", "DocumentSnapshot written with ID: ${FirebaseAuth.getInstance().currentUser!!.uid}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("CLOUD FIRESTORE", "Error adding document", e)
                    }
            } else {
                Log.d("FAILED EMAIL: " , " $email")
                Log.d("FAILED PASSWORD: " , " $password")
                Toast.makeText(this, "Account creation failed.\nTry another email or password.", Toast.LENGTH_LONG).show()
            }

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
    }

    private fun fieldsAllFilled(): Boolean {

        if (newEmailET.text.isEmpty() ||
            addressET.text.isEmpty() ||
            budgetET.text.isEmpty() ||
            sizeET.text.isEmpty()) {
            Toast.makeText(this, "You left one or more fields blank.", Toast.LENGTH_LONG).show()
            return false
        }

        // passwords match
        // password more than eight characters
        if (newPasswordET.text.isEmpty() || confirmPasswordET.text.isEmpty()) {
            Toast.makeText(this, "You must fill in a password.", Toast.LENGTH_LONG).show()
            return false
        } else if (newPasswordET.text.toString() != confirmPasswordET.text.toString()) {
            Toast.makeText(this, "Your passwords must match.", Toast.LENGTH_LONG).show()
            return false
        } else if (newPasswordET.text.toString().length < 8) {
            Toast.makeText(this, "Password must be at least eight characters long.", Toast.LENGTH_LONG).show()
            return false
        }

        val address = geocoder.getFromLocationName(addressET.text.toString(), 1)
        if (address == null || address.size == 0) {
            Toast.makeText(this, "Must enter a valid address.", Toast.LENGTH_LONG).show()
            return false
        }


        return true
    }

}