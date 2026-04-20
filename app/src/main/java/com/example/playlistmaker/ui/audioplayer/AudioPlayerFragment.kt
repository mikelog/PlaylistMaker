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
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerViewModel
import com.google.android.material.appbar.MaterialToolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AudioPlayerFragment : Fragment() {

    companion object {
        private const val ARG_TRACK = "arg_track"

        fun createArgs(track: Track): Bundle {
            return Bundle().apply { putParcelable(ARG_TRACK, track) }
        }
    }

    private val track: Track by lazy {
        requireArguments().getParcelable(ARG_TRACK, Track::class.java)
            ?: error("Track must be provided")
    }

    private val viewModel: AudioPlayerViewModel by viewModel { parametersOf(track) }

    private lateinit var albumArt: ImageView
    private lateinit var trackNameTv: TextView
    private lateinit var artistNameTv: TextView
    private lateinit var collectionNameTv: TextView
    private lateinit var countryTv: TextView
    private lateinit var releaseDateTv: TextView
    private lateinit var primaryGenreNameTv: TextView
    private lateinit var trackTimeTv: TextView
    private lateinit var textProgress: TextView
    private lateinit var buttonPlay: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_audio_player, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialToolbar>(R.id.playerToolbar)
            .setNavigationOnClickListener { findNavController().popBackStack() }

        albumArt = view.findViewById(R.id.albumArt)
        trackNameTv = view.findViewById(R.id.trackName)
        artistNameTv = view.findViewById(R.id.artistName)
        collectionNameTv = view.findViewById(R.id.collectionName)
        countryTv = view.findViewById(R.id.country)
        releaseDateTv = view.findViewById(R.id.releaseDate)
        primaryGenreNameTv = view.findViewById(R.id.primaryGenreName)
        trackTimeTv = view.findViewById(R.id.trackTime)
        textProgress = view.findViewById(R.id.textProgress)
        buttonPlay = view.findViewById(R.id.buttonPlay)

        buttonPlay.setOnClickListener { viewModel.onPlayPauseClicked() }

        bindTrack(track)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            buttonPlay.setImageResource(
                if (state.isPlaying) R.drawable.ic_pause_100 else R.drawable.ic_play_100
            )
            buttonPlay.isEnabled = state.isPlayEnabled
            buttonPlay.alpha = if (state.isPlayEnabled) 1f else 0.5f
            textProgress.text = state.progress
        }
    }

    private fun bindTrack(track: Track) {
        trackNameTv.text = track.trackName
        artistNameTv.text = track.artistName
        collectionNameTv.text = track.collectionName
        releaseDateTv.text = track.getReleaseYear()
        primaryGenreNameTv.text = track.primaryGenreName
        countryTv.text = track.country
        trackTimeTv.text = track.trackTime

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder_album)
            .error(R.drawable.placeholder_album)
            .centerCrop()
            .into(albumArt)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onActivityPaused()
    }
}
