package com.cailihuang.apartmentgate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import com.cailihuang.apartmentgate.ListFragment

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var jsonListings: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setFragment(MapFragment.newInstance())

        navigationView.setOnNavigationItemSelectedListener {item ->
            when (item.itemId) {
                R.id.navigation_search -> {
                    setFragment(MapFragment.newInstance())
                    true
                }
                R.id.navigation_popular -> {
                    setFragment(TrendingFragment.newInstance())
                    true
                }
                R.id.navigation_favorites -> {
                    setFragment(FavoritesFragment.newInstance())
                    true
                }
                R.id.navigation_profile -> {
                    setFragment(ProfileFragment.newInstance())
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
                .addToBackStack(null)
                .commit()
    }
}