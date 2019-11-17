package com.cailihuang.apartmentgate

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_create_account.*
import kotlinx.android.synthetic.main.activity_welcome.*
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.ArrayAdapter
import android.app.Activity
import android.graphics.Color
import android.widget.AdapterView
import android.graphics.Typeface
import androidx.core.view.ViewCompat
import androidx.core.content.ContextCompat
import android.widget.Button


class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase


    private lateinit var preferredTransport: String
    private lateinit var demographic: String
    private lateinit var walkability: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        initializeFirebase()

        initializeLayoutElems()

        createProfileButton.setOnClickListener { createNewAccount() }
    }


    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
    }

    private fun initializeLayoutElems() {

        startTime.setIs24HourView(true)
        endTime.setIs24HourView(true)

        // TODO reset color when another option selected

        carButton.setOnClickListener {
            preferredTransport = "car"
            //changeButtonsColors(carButton, transitButton, walkButton, bikeButton)
            ViewCompat.setBackgroundTintList(carButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        transitButton.setOnClickListener {
            preferredTransport = "transit"
            //changeButtonsColors(transitButton, carButton, walkButton, bikeButton)
            ViewCompat.setBackgroundTintList(transitButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        walkButton.setOnClickListener {
            preferredTransport = "walk"
            //changeButtonsColors(walkButton, carButton, transitButton, bikeButton)
            ViewCompat.setBackgroundTintList(walkButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        bikeButton.setOnClickListener {
            preferredTransport = "bike"
            //changeButtonsColors(bikeButton, walkButton, carButton, transitButton)
            ViewCompat.setBackgroundTintList(bikeButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }

        familiesButton.setOnClickListener {
            demographic = "families"
            //changeButtonsColors(familiesButton, professionalsButton, retireesButton)
            ViewCompat.setBackgroundTintList(familiesButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        professionalsButton.setOnClickListener {
            demographic = "professionals"
            //changeButtonsColors(professionalsButton, familiesButton, retireesButton)
            ViewCompat.setBackgroundTintList(professionalsButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        retireesButton.setOnClickListener {
            demographic = "retirees"
            //changeButtonsColors(retireesButton, familiesButton, professionalsButton)
            ViewCompat.setBackgroundTintList(retireesButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }

        dontCareButton.setOnClickListener {
            walkability = "dontcare"
            //changeButtonsColors(dontCareButton, careButton, careAlotButton)
            ViewCompat.setBackgroundTintList(dontCareButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        careButton.setOnClickListener {
            walkability = "care"
            //changeButtonsColors(careButton, dontCareButton, careAlotButton)
            ViewCompat.setBackgroundTintList(careButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }
        careAlotButton.setOnClickListener {
            walkability = "carealot"
            //changeButtonsColors(careAlotButton, dontCareButton, careButton)
            ViewCompat.setBackgroundTintList(careAlotButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
        }

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
    }

//    private fun changeButtonsColors(selectedButton: Button, otherButton1: Button, otherButton2: Button, otherButton3: Button? = null) {
//        otherButton1.setBackground(createProfileButton.background)
//        otherButton2.setBackground(createProfileButton.background)
//        otherButton3?.setBackground(createProfileButton.background)
//        ViewCompat.setBackgroundTintList(selectedButton, ContextCompat.getColorStateList(this, android.R.color.holo_blue_dark))
//    }

    private fun createNewAccount() {
        // TODO check if fields are empty
        // TODO confirm password

        val email = newEmailET.text.toString()
        val password = newPasswordET.text.toString()
       // val firs

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                val user = auth.getCurrentUser()
                Toast.makeText(this, "New user email: " + user?.email, Toast.LENGTH_LONG).show()

                // update user profile information
                val currentUserDb = mDatabaseReference.child(user?.uid.toString())

                currentUserDb.child("email").setValue(newEmailET.text.toString())
                currentUserDb.child("workAddress").setValue(addressET.text.toString())
                currentUserDb.child("workStart").setValue(startTime.hour.toString() + ":" + startTime.minute.toString())
                currentUserDb.child("workEnd").setValue(endTime.hour.toString() + ":" + endTime.minute.toString())
                currentUserDb.child("workAddress").setValue(addressET.text.toString())
                currentUserDb.child("transportation").setValue(preferredTransport)
                currentUserDb.child("maxCommuteTime").setValue(commuteTimeSpinner.selectedItem.toString())
                currentUserDb.child("demographic").setValue(demographic)
                currentUserDb.child("walkability").setValue(walkability)
                currentUserDb.child("budget").setValue(budgetET.text.toString())
                currentUserDb.child("size").setValue(sizeET.text.toString())

            } else {
                // password must be a certain mystery length long, probably eight
                Log.d("FAILED PASSWORD ---" , " $password")
            }

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
    }

}