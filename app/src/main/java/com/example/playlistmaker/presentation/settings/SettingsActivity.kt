package com.example.playlistmaker.presentation.settings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchMaterial
    private val viewModel by viewModel<SettingsViewModel>()

    private var isSwitchInitializing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val rootView = findViewById<android.view.View>(R.id.settingsRoot)
        val toolBar = findViewById<android.view.View>(R.id.settingsToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val toolbarParams = toolBar.layoutParams as android.widget.LinearLayout.LayoutParams
            toolbarParams.topMargin = statusBar.top
            toolBar.layoutParams = toolbarParams
            view.updatePadding(bottom = navBar.bottom)
            insets
        }

        themeSwitch = findViewById(R.id.themeSwitcher)

        viewModel.screenState.observe(this) { state ->
            isSwitchInitializing = true
            themeSwitch.isChecked = state.isDarkTheme
            isSwitchInitializing = false
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchInitializing) {
                viewModel.onThemeToggled(isChecked)
            }
        }

        findViewById<android.widget.Button>(R.id.btnBack)
            ?.setOnClickListener { finish() }

        findViewById<android.widget.LinearLayout>(R.id.shareApp)
            ?.setOnClickListener { viewModel.onShareAppClicked() }

        findViewById<android.widget.LinearLayout>(R.id.contactSupport)
            ?.setOnClickListener { viewModel.onOpenSupportClicked() }

        findViewById<android.widget.LinearLayout>(R.id.userAgreement)
            ?.setOnClickListener { viewModel.onOpenTermsClicked() }
    }
}
