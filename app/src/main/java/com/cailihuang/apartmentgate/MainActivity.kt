package com.cailihuang.apartmentgate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import com.cailihuang.apartmentgate.ListFragment

class MainActivity : AppCompatActivity() {

    // TODO change variable name
    companion object {
        lateinit var jsonListings: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMapFragment()
        initJson()

        navigationView.setOnNavigationItemSelectedListener {item ->
            when (item.itemId) {
                R.id.navigation_search -> {
                    println("search pressed")
                    initMapFragment()
                    true
                }
                R.id.navigation_popular -> {
                    println("popular pressed")
                    true
                }
                R.id.navigation_favorites -> {
                    println("favorites pressed")
                    true
                }
                R.id.navigation_profile -> {
                    println("profile pressed")
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun initMapFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, MapFragment.newInstance())
                .commit()
    }

    private fun initJson() {
        jsonListings = assets.open("apartmentscom-sf.json").bufferedReader().use {
            it.readText()
        }
    }

}