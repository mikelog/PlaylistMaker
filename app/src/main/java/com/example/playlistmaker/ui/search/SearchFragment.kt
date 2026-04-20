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
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.search.SearchContent
import com.example.playlistmaker.presentation.search.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var historyTitle: TextView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var clearEditSearchButton: ImageButton
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var containerSearchHistory: LinearLayout
    private lateinit var progressBar: ProgressBar

    private val viewModel by viewModel<SearchViewModel>()

    private var isClickAllowed = true
    private val clickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val clickDebounceRunnable = Runnable { isClickAllowed = true }

    private var isRestoringText = false

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        fun newInstance() = SearchFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        recyclerView = view.findViewById(R.id.searchRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        historyTitle = view.findViewById(R.id.historyTitle)
        historyRecycler = view.findViewById(R.id.historyRecyclerView)
        clearHistoryButton = view.findViewById(R.id.clearHistoryButton)
        containerSearchHistory = view.findViewById(R.id.containerSearchHistory)
        clearEditSearchButton = view.findViewById(R.id.clearEditSearchButton)

        setupRecyclerView()
        setupHistoryRecycler()
        setupListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            if (searchEditText.text.toString() != state.query) {
                isRestoringText = true
                searchEditText.setText(state.query)
                searchEditText.setSelection(state.query.length)
                searchEditText.post { isRestoringText = false }
            }
            clearEditSearchButton.visibility =
                if (state.query.isEmpty()) View.GONE else View.VISIBLE

            val showHistory = state.historyTracks != null &&
                    state.query.isEmpty() &&
                    state.searchContent is SearchContent.Idle

            if (showHistory) {
                historyAdapter.updateData(state.historyTracks ?: emptyList())
                containerSearchHistory.visibility = View.VISIBLE
                historyTitle.visibility = View.VISIBLE
                historyRecycler.visibility = View.VISIBLE
                clearHistoryButton.visibility = View.VISIBLE
            } else {
                containerSearchHistory.visibility = View.GONE
                historyTitle.visibility = View.GONE
                historyRecycler.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
            }

            when (state.searchContent) {
                is SearchContent.Idle -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    adapter.clearData()
                }
                is SearchContent.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                is SearchContent.Tracks -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateData(state.searchContent.tracks)
                }
                is SearchContent.Empty -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                }
                is SearchContent.NetworkError -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.showNoConnection()
                }
            }
        }
    }

    private fun setupListeners() {
        clearEditSearchButton.setOnClickListener {
            searchEditText.text.clear()
            searchEditText.clearFocus()
            hideKeyboard()
            viewModel.onQueryCleared()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.onClearHistory()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchAction()
                searchEditText.clearFocus()
                hideKeyboard()
                true
            } else false
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchFocused(hasFocus)
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            if (!isRestoringText) {
                viewModel.onQueryChanged(
                    query = text?.toString() ?: "",
                    fieldHasFocus = searchEditText.hasFocus()
                )
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    searchEditText.clearFocus()
                    hideKeyboard()
                }
            }
        })

        adapter.onRetryClick = { viewModel.onRetry() }

        adapter.onTrackClick = { track ->
            if (clickDebounce()) {
                searchEditText.clearFocus()
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
        historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        historyRecycler.adapter = historyAdapter

        historyRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    searchEditText.clearFocus()
                    hideKeyboard()
                }
            }
        })

        historyAdapter.onTrackClick = { track ->
            if (clickDebounce()) {
                searchEditText.clearFocus()
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
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
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
        if (searchEditText.hasFocus()) viewModel.onSearchFocused(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clickHandler.removeCallbacks(clickDebounceRunnable)
    }
}
