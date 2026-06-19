package com.example.playlistmaker.ui.medialibrary.fragments

import androidx.appcompat.app.AppCompatDelegate
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.ui.medialibrary.adapters.PlaylistAdapter
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistsViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<PlaylistsViewModel>()
    private val adapter = PlaylistAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.playlistsRecyclerView.adapter = adapter
        binding.playlistsRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val side = (16 * resources.displayMetrics.density).toInt()
            val gap = (8 * resources.displayMetrics.density).toInt()
            val row = (16 * resources.displayMetrics.density).toInt()
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val pos = parent.getChildAdapterPosition(view)
                val col = pos % 2
                outRect.left = if (col == 0) side else gap / 2
                outRect.right = if (col == 0) gap / 2 else side
                outRect.top = if (pos < 2) 0 else row
                outRect.bottom = 0
            }
        })

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_mediaLibraryFragment_to_newPlaylistFragment)
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<String>("playlist_created_name")
            ?.observe(viewLifecycleOwner) { name ->
                if (name != null) {
                    val isNight = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
                    val bgColor = if (isNight) Color.WHITE else Color.parseColor("#1A1B22")
                    val textColor = if (isNight) Color.parseColor("#1A1B22") else Color.WHITE
                    val snackbar = Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        getString(R.string.playlist_created_toast, name),
                        Snackbar.LENGTH_SHORT
                    )
                    snackbar.setTextColor(textColor)
                    snackbar.view.background = ColorDrawable(bgColor)
                    snackbar.show()
                    findNavController().currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>("playlist_created_name")
                }
            }

        viewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            val isEmpty = playlists.isEmpty()
            binding.placeholderImage.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.placeholderMessage.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.playlistsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
            adapter.submitList(playlists)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}
