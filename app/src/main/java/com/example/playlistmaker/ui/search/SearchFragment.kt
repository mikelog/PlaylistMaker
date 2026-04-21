package com.example.playlistmaker.ui.search

import com.example.playlistmaker.ui.audioplayer.AudioPlayerFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.search.SearchContent
import com.example.playlistmaker.presentation.search.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<SearchViewModel>()
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private var isClickAllowed = true
    private val clickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val clickDebounceRunnable = Runnable { isClickAllowed = true }
    private var isRestoringText = false

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupHistoryRecycler()
        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            if (binding.searchEditText.text.toString() != state.query) {
                isRestoringText = true
                binding.searchEditText.setText(state.query)
                binding.searchEditText.setSelection(state.query.length)
                binding.searchEditText.post { isRestoringText = false }
            }
            binding.clearEditSearchButton.visibility =
                if (state.query.isEmpty()) View.GONE else View.VISIBLE

            val showHistory = state.historyTracks != null &&
                    state.query.isEmpty() &&
                    state.searchContent is SearchContent.Idle

            if (showHistory) {
                historyAdapter.updateData(state.historyTracks ?: emptyList())
                binding.containerSearchHistory.visibility = View.VISIBLE
                binding.historyTitle.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.VISIBLE
                binding.clearHistoryButton.visibility = View.VISIBLE
            } else {
                binding.containerSearchHistory.visibility = View.GONE
                binding.historyTitle.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.GONE
                binding.clearHistoryButton.visibility = View.GONE
            }

            when (state.searchContent) {
                is SearchContent.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.searchRecyclerView.visibility = View.GONE
                    adapter.clearData()
                }
                is SearchContent.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.searchRecyclerView.visibility = View.GONE
                }
                is SearchContent.Tracks -> {
                    binding.progressBar.visibility = View.GONE
                    binding.searchRecyclerView.visibility = View.VISIBLE
                    adapter.updateData(state.searchContent.tracks)
                }
                is SearchContent.Empty -> {
                    binding.progressBar.visibility = View.GONE
                    binding.searchRecyclerView.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                }
                is SearchContent.NetworkError -> {
                    binding.progressBar.visibility = View.GONE
                    binding.searchRecyclerView.visibility = View.VISIBLE
                    adapter.showNoConnection()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.clearEditSearchButton.setOnClickListener {
            binding.searchEditText.text.clear()
            binding.searchEditText.clearFocus()
            hideKeyboard()
            viewModel.onQueryCleared()
        }

        binding.clearHistoryButton.setOnClickListener {
            viewModel.onClearHistory()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchAction()
                binding.searchEditText.clearFocus()
                hideKeyboard()
                true
            } else false
        }

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchFocused(hasFocus)
        }

        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            if (!isRestoringText) {
                viewModel.onQueryChanged(
                    query = text?.toString() ?: "",
                    fieldHasFocus = binding.searchEditText.hasFocus()
                )
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(mutableListOf())
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRecyclerView.adapter = adapter

        binding.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.searchEditText.clearFocus()
                    hideKeyboard()
                }
            }
        })

        adapter.onRetryClick = { viewModel.onRetry() }

        adapter.onTrackClick = { track ->
            if (clickDebounce()) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
                viewModel.onTrackClicked(track)
                findNavController().navigate(
                    R.id.action_searchFragment_to_audioPlayerFragment,
                    AudioPlayerFragment.createArgs(track)
                )
            }
        }
    }

    private fun setupHistoryRecycler() {
        historyAdapter = TrackAdapter(mutableListOf())
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter

        binding.historyRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.searchEditText.clearFocus()
                    hideKeyboard()
                }
            }
        })

        historyAdapter.onTrackClick = { track ->
            if (clickDebounce()) {
                binding.searchEditText.clearFocus()
                hideKeyboard()
                viewModel.onTrackClicked(track)
                findNavController().navigate(
                    R.id.action_searchFragment_to_audioPlayerFragment,
                    AudioPlayerFragment.createArgs(track)
                )
            }
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            clickHandler.postDelayed(clickDebounceRunnable, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    override fun onResume() {
        super.onResume()
        if (binding.searchEditText.hasFocus()) viewModel.onSearchFocused(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clickHandler.removeCallbacks(clickDebounceRunnable)
        _binding = null
    }
}

