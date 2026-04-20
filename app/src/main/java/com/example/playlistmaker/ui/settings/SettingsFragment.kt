package com.example.playlistmaker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private lateinit var themeSwitch: SwitchMaterial
    private val viewModel by viewModel<SettingsViewModel>()
    private var isSwitchInitializing = false

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeSwitch = view.findViewById(R.id.themeSwitcher)

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            isSwitchInitializing = true
            themeSwitch.isChecked = state.isDarkTheme
            isSwitchInitializing = false
        }

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchInitializing) viewModel.onThemeToggled(isChecked)
        }

        view.findViewById<android.widget.LinearLayout>(R.id.shareApp)
            ?.setOnClickListener { viewModel.onShareAppClicked() }
        view.findViewById<android.widget.LinearLayout>(R.id.contactSupport)
            ?.setOnClickListener { viewModel.onOpenSupportClicked() }
        view.findViewById<android.widget.LinearLayout>(R.id.userAgreement)
            ?.setOnClickListener { viewModel.onOpenTermsClicked() }
    }
}
