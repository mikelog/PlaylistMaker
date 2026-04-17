package com.example.playlistmaker.presentation.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.medialibrary.fragments.MediaLibraryFragment
import com.example.playlistmaker.presentation.search.SearchActivity
import com.example.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var mainContent: View

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragmentContainer = findViewById(R.id.fragmentContainer)
        mainContent = findViewById(R.id.mainContent)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(top = statusBar.top, bottom = navBar.bottom)
            insets
        }

        findViewById<Button>(R.id.btnSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        findViewById<Button>(R.id.btnMedia).setOnClickListener {
            openMediaLibrary()
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateContentVisibility()
        }
    }

    private fun openMediaLibrary() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, MediaLibraryFragment.newInstance())
            addToBackStack(null)
        }
        updateContentVisibility()
    }

    private fun updateContentVisibility() {
        val hasFragments = supportFragmentManager.backStackEntryCount > 0
        fragmentContainer.visibility = if (hasFragments) View.VISIBLE else View.GONE
        mainContent.visibility = if (hasFragments) View.GONE else View.VISIBLE
    }
}