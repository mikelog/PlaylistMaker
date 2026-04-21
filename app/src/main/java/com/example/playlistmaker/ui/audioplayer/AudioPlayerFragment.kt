package com.example.playlistmaker.ui.audioplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerViewModel
import com.google.android.material.appbar.MaterialToolbar
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

        bindTrack(track)
        observeViewModel()
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

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPaused()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
