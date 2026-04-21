package com.example.playlistmaker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import com.google.android.material.switchmaterial.SwitchMaterial
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<SettingsViewModel>()
    private var isSwitchInitializing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            isSwitchInitializing = true
            binding.themeSwitcher.isChecked = state.isDarkTheme
            isSwitchInitializing = false
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            if (!isSwitchInitializing) viewModel.onThemeToggled(isChecked)
        }

        binding.shareApp.setOnClickListener { viewModel.onShareAppClicked() }
        binding.contactSupport.setOnClickListener { viewModel.onOpenSupportClicked() }
        binding.userAgreement.setOnClickListener { viewModel.onOpenTermsClicked() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
