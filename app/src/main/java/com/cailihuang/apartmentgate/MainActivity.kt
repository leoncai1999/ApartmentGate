package com.cailihuang.apartmentgate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import com.cailihuang.apartmentgate.ListFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(MapFragment.newInstance())

        navigationView.setOnNavigationItemSelectedListener {item ->
            when (item.itemId) {
                R.id.navigation_search -> {
                    println("search pressed")
                    setFragment(MapFragment.newInstance())
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

    fun setFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commit()
    }
}