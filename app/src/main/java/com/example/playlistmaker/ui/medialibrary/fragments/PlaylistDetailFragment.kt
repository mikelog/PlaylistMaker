package com.example.playlistmaker.ui.medialibrary.fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.drawable.InsetDrawable
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistDetailBinding
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.medialibrary.adapters.PlaylistTrackAdapter
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class PlaylistDetailFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailBinding? = null
    private val binding get() = _binding!!

    private val playlistId: Long by lazy {
        requireArguments().getLong(ARG_PLAYLIST_ID)
    }

    private val viewModel: PlaylistDetailViewModel by viewModel { parametersOf(playlistId) }
    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private val trackAdapter = PlaylistTrackAdapter(
        onTrackClick = { track -> openPlayer(track) },
        onTrackLongClick = { track -> showDeleteTrackDialog(track) }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.tracksBottomSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.menuBottomSheet).apply {
            isHideable = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    _binding?.menuOverlay?.visibility =
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) View.GONE else View.VISIBLE
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    _binding?.menuOverlay?.alpha = (slideOffset + 1f) / 2f
                }
            })
        }

        binding.menuOverlay.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = trackAdapter

        binding.shareButton.setOnClickListener { viewModel.sharePlaylist() }
        binding.menuButton.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.menuItemShare.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.sharePlaylist()
        }
        binding.menuItemEdit.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(
                R.id.action_playlistDetailFragment_to_editPlaylistFragment,
                EditPlaylistFragment.createArgs(playlistId)
            )
        }
        binding.menuItemDelete.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }

        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            binding.playlistName.text = playlist.name

            if (playlist.description.isNotBlank()) {
                binding.playlistDescription.text = playlist.description
                binding.playlistDescription.visibility = View.VISIBLE
            } else {
                binding.playlistDescription.visibility = View.GONE
            }

            val coverFile = if (playlist.coverPath.isNotEmpty()) File(playlist.coverPath) else null
            if (coverFile != null && coverFile.exists()) {
                Glide.with(this).load(coverFile).centerCrop().into(binding.coverImage)
                Glide.with(this).load(coverFile).centerCrop().into(binding.menuPlaylistCover)
            } else {
                binding.coverImage.setImageResource(R.drawable.placeholder_album)
                binding.menuPlaylistCover.setImageResource(R.drawable.placeholder_album)
            }

            binding.menuPlaylistName.text = playlist.name
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.submitList(tracks)
            updateDurationAndCount(tracks.size, viewModel.totalDuration.value ?: "0")
            val count = tracks.size
            binding.menuPlaylistCount.text =
                resources.getQuantityString(R.plurals.track_count, count, count)
            binding.emptyTracksMessage.visibility =
                if (tracks.isEmpty()) View.VISIBLE else View.GONE
            binding.tracksRecyclerView.visibility =
                if (tracks.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalDuration.observe(viewLifecycleOwner) { minutes ->
            updateDurationAndCount(viewModel.tracks.value?.size ?: 0, minutes)
        }

        viewModel.shareText.observe(viewLifecycleOwner) { text ->
            if (text == null) {
                showSnackbar(getString(R.string.share_playlist_empty))
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                startActivity(Intent.createChooser(intent, null))
            }
        }

        viewModel.playlistDeleted.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun updateDurationAndCount(count: Int, minutes: String) {
        val mins = minutes.toIntOrNull() ?: 0
        val durationStr = resources.getQuantityString(R.plurals.minutes_count, mins, minutes)
        val countStr = resources.getQuantityString(R.plurals.track_count, count, count)
        binding.durationAndCount.text = "$durationStr • $countStr"
    }

    private fun openPlayer(track: Track) {
        findNavController().navigate(
            R.id.action_playlistDetailFragment_to_audioPlayerFragment,
            Bundle().apply { putParcelable("arg_track", track) }
        )
    }

    private fun showDeleteTrackDialog(track: Track) {
        showStyledDialog(
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_PlaylistMaker_Dialog)
                .setTitle(R.string.delete_track_dialog_title)
                .setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(R.string.yes) { _, _ -> viewModel.removeTrack(track.trackId) }
        )
    }

    private fun showDeletePlaylistDialog() {
        showStyledDialog(
            MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_PlaylistMaker_Dialog)
                .setTitle(R.string.delete_playlist_title)
                .setMessage(R.string.delete_playlist_message)
                .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
                .setPositiveButton(R.string.delete_playlist_confirm) { _, _ -> viewModel.deletePlaylist() }
        )
    }

    private fun showStyledDialog(builder: MaterialAlertDialogBuilder) {
        val dialog = builder.create()
        dialog.show()
        val window = dialog.window ?: return
        val bg = window.decorView.background
        if (bg is InsetDrawable) {
            window.setBackgroundDrawable(bg.drawable)
        }
        val coverWidth = binding.coverImage.width
        if (coverWidth > 0) {
            window.setLayout(coverWidth, LayoutParams.WRAP_CONTENT)
        }
    }

    private fun showSnackbar(message: String) {
        val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        val bgColor = if (isNight) Color.WHITE else Color.parseColor("#1A1B22")
        val textColor = if (isNight) Color.parseColor("#1A1B22") else Color.WHITE
        val snackbar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_SHORT
        )
        snackbar.setTextColor(textColor)
        snackbar.view.background = ColorDrawable(bgColor)
        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PLAYLIST_ID = "arg_playlist_id"

        fun createArgs(playlistId: Long) = Bundle().apply {
            putLong(ARG_PLAYLIST_ID, playlistId)
        }
    }
}
