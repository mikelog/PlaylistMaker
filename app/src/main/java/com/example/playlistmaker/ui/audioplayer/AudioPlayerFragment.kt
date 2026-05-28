package com.example.playlistmaker.ui.audioplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.audioplayer.AddToPlaylistResult
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerViewModel
import com.example.playlistmaker.ui.medialibrary.adapters.BottomSheetPlaylistAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_TRACK = "arg_track"
        fun createArgs(track: Track) = Bundle().apply { putParcelable(ARG_TRACK, track) }
    }

    private val track: Track by lazy {
        requireArguments().getParcelable(ARG_TRACK, Track::class.java)
            ?: error("Track must be provided")
    }

    private val viewModel: AudioPlayerViewModel by viewModel { parametersOf(track) }
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val bottomSheetAdapter = BottomSheetPlaylistAdapter { playlist ->
        viewModel.addTrackToPlaylist(playlist)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playerToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonPlay.setOnClickListener { viewModel.onPlayPauseClicked() }
        binding.buttonFavorite.setOnClickListener { viewModel.onFavoriteClicked() }

        setupBottomSheet()
        bindTrack(track)
        observeViewModel()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isFitToContents = false
            halfExpandedRatio = 0.6f
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                _binding?.overlay?.visibility = when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> View.GONE
                    else -> View.VISIBLE
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                _binding?.overlay?.alpha = (slideOffset + 1f) / 2f
            }
        })

        binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecyclerView.adapter = bottomSheetAdapter

        binding.buttonAddPlaylist.setOnClickListener {
            viewModel.loadPlaylists()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        binding.newPlaylistButton.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_audioPlayerFragment_to_newPlaylistFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            binding.buttonPlay.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause_100 else R.drawable.ic_play_100
            )
            binding.buttonPlay.isEnabled = state.isPlayEnabled
            binding.buttonPlay.alpha = if (state.isPlayEnabled) 1f else 0.5f
            binding.textProgress.text = state.progress
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            binding.buttonFavorite.setImageResource(
                if (isFavorite) R.drawable.ic_favorite_active_50 else R.drawable.ic_favorite_50
            )
        }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter.submitList(playlists)
        }

        viewModel.addToPlaylistResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddToPlaylistResult.Success -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    showSnackbar(getString(R.string.added_to_playlist_toast, result.playlistName))
                }
                is AddToPlaylistResult.AlreadyAdded -> {
                    showSnackbar(getString(R.string.already_in_playlist_toast, result.playlistName))
                }
            }
        }
    }

    private fun bindTrack(track: Track) {
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.collectionName.text = track.collectionName
        binding.releaseDate.text = track.getReleaseYear()
        binding.primaryGenreName.text = track.primaryGenreName
        binding.country.text = track.country
        binding.trackTime.text = track.trackTime

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder_album)
            .error(R.drawable.placeholder_album)
            .centerCrop()
            .into(binding.albumArt)
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

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPaused()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
