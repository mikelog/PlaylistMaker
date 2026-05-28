package com.example.playlistmaker.ui.medialibrary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.ui.audioplayer.AudioPlayerFragment
import com.example.playlistmaker.ui.medialibrary.viewmodels.FavouriteTracksViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavouriteTracksFragment : Fragment() {

    private var _binding: FragmentFavouriteTracksBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<FavouriteTracksViewModel>()
    private lateinit var adapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TrackAdapter(mutableListOf())
        adapter.onTrackClick = { track ->
            findNavController().navigate(
                R.id.action_mediaLibraryFragment_to_audioPlayerFragment,
                AudioPlayerFragment.createArgs(track)
            )
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavouriteTracksViewModel.ScreenState.Empty -> {
                    binding.placeholderImage.visibility = View.VISIBLE
                    binding.placeholderMessage.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                is FavouriteTracksViewModel.ScreenState.Content -> {
                    binding.placeholderImage.visibility = View.GONE
                    binding.placeholderMessage.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    adapter.updateData(state.tracks)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavouriteTracksFragment()
    }
}
