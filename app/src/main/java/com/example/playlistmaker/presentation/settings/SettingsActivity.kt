package com.example.playlistmaker.presentation.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeSwitch: SwitchMaterial
    private lateinit var viewModel: SettingsViewModel

    // Флаг для подавления ложных событий при программной установке switch
    private var isSwitchInitializing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // --- Инициализация ViewModel через ViewModelProvider ---
        viewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(
                settingsInteractor = Creator.provideSettingsInteractor(this),
                sharingInteractor = Creator.provideSharingInteractor(this)
            )
        )[SettingsViewModel::class.java]

        themeSwitch = findViewById(R.id.themeSwitcher)

        // --- Подписка на LiveData ---
        viewModel.screenState.observe(this) { state ->
            isSwitchInitializing = true
            themeSwitch.isChecked = state.isDarkTheme
            isSwitchInitializing = false
        }

        // --- Слушатели ---
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
