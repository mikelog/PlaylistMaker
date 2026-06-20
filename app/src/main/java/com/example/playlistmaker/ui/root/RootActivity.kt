package com.example.playlistmaker.ui.root

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.FragmentContainerView

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val navHost = findViewById<FragmentContainerView>(R.id.nav_host_fragment)
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideBottomNav = destination.id == R.id.audioPlayerFragment
                    || destination.id == R.id.newPlaylistFragment
                    || destination.id == R.id.playlistDetailFragment
                    || destination.id == R.id.editPlaylistFragment
            bottomNav.visibility = if (hideBottomNav) View.GONE else View.VISIBLE
            navHost.setPadding(0, 0, 0, if (hideBottomNav) 0 else bottomNav.height)
        }
    }
}
