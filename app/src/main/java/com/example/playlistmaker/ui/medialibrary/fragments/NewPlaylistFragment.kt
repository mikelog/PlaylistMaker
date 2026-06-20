package com.example.playlistmaker.ui.medialibrary.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.google.android.material.snackbar.Snackbar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.ui.medialibrary.viewmodels.NewPlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

open class NewPlaylistFragment : Fragment() {

    protected var _binding: FragmentNewPlaylistBinding? = null
    protected val binding get() = _binding!!

    open val viewModel: NewPlaylistViewModel by viewModel()

    private val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            requireContext().contentResolver.takePersistableUriPermission(
                uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.onCoverSelected(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { handleBackPress() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { handleBackPress() }
        })

        binding.coverArea.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.createButton.isEnabled = !s.isNullOrBlank()
            }
        })

        binding.createButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()
            viewModel.createPlaylist(name, description)
        }

        viewModel.coverUri.observe(viewLifecycleOwner) { uri ->
            if (uri != null) {
                binding.coverImage.visibility = View.VISIBLE
                binding.coverPlaceholder.visibility = View.GONE
                Glide.with(this).load(uri).centerCrop().into(binding.coverImage)
            } else {
                binding.coverImage.visibility = View.GONE
                binding.coverPlaceholder.visibility = View.VISIBLE
            }
        }

        viewModel.playlistCreated.observe(viewLifecycleOwner) { name ->
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set("playlist_created_name", name)
            findNavController().popBackStack()
        }
    }

    private fun hasUnsavedData(): Boolean {
        return !binding.nameInput.text.isNullOrBlank()
            || !binding.descriptionInput.text.isNullOrBlank()
            || viewModel.coverUri.value != null
    }

    protected open fun handleBackPress() {
        if (hasUnsavedData()) {
            showExitDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_PlaylistMaker_Dialog)
            .setTitle(R.string.exit_playlist_dialog_title)
            .setMessage(R.string.exit_playlist_dialog_message)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.finish) { _, _ -> findNavController().popBackStack() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NewPlaylistFragment()
    }
}
