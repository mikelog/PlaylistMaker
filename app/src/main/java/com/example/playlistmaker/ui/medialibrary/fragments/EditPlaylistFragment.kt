package com.example.playlistmaker.ui.medialibrary.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.medialibrary.viewmodels.EditPlaylistViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.NewPlaylistViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class EditPlaylistFragment : NewPlaylistFragment() {

    private val playlistId: Long by lazy {
        requireArguments().getLong(ARG_PLAYLIST_ID)
    }

    private val editViewModel: EditPlaylistViewModel by viewModel { parametersOf(playlistId) }
    override val viewModel: NewPlaylistViewModel get() = editViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.edit_playlist_title)
        binding.createButton.text = getString(R.string.save_button)

        binding.createButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()
            editViewModel.saveEditedPlaylist(name, description)
        }

        editViewModel.editingPlaylist.observe(viewLifecycleOwner) { playlist ->
            if (binding.nameInput.text.isNullOrBlank()) {
                binding.nameInput.setText(playlist.name)
            }
            if (binding.descriptionInput.text.isNullOrBlank() && playlist.description.isNotEmpty()) {
                binding.descriptionInput.setText(playlist.description)
            }
            if (editViewModel.coverUri.value == null && playlist.coverPath.isNotEmpty()) {
                val file = File(playlist.coverPath)
                if (file.exists()) {
                    binding.coverImage.visibility = View.VISIBLE
                    binding.coverPlaceholder.visibility = View.GONE
                    Glide.with(this).load(file).centerCrop().into(binding.coverImage)
                }
            }
        }
    }

    override fun handleBackPress() {
        findNavController().popBackStack()
    }

    companion object {
        const val ARG_PLAYLIST_ID = "arg_playlist_id"

        fun createArgs(playlistId: Long) = Bundle().apply {
            putLong(ARG_PLAYLIST_ID, playlistId)
        }
    }
}
