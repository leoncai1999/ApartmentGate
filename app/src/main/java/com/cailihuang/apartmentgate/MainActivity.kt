package com.cailihuang.apartmentgate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView.setOnNavigationItemSelectedListener {item ->
            when (item.itemId) {
                R.id.navigation_search -> {
                    println("search pressed")
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

}