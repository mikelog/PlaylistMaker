package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val rootView = findViewById<View>(R.id.searchRoot)
        val toolBar = findViewById<View>(R.id.searchToolbar)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val toolbarParams = toolBar.layoutParams as android.widget.LinearLayout.LayoutParams
            toolbarParams.topMargin = statusBar.top
            toolBar.layoutParams = toolbarParams
            view.updatePadding(bottom = navBar.bottom)
            insets
        }

        val btnBack: Button = findViewById(R.id.btnBack)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.searchRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        historyTitle = findViewById(R.id.historyTitle)
        historyRecycler = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        containerSearchHistory = findViewById(R.id.containerSearchHistory)
        clearEditSearchButton = findViewById(R.id.clearEditSearchButton)

        setupRecyclerView()
        setupHistoryRecycler()
        setupListeners(btnBack)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.screenState.observe(this) { state ->

            if (searchEditText.text.toString() != state.query) {
                isRestoringText = true
                searchEditText.setText(state.query)
                searchEditText.setSelection(state.query.length)
                searchEditText.post { isRestoringText = false }
            }
            clearEditSearchButton.visibility =
                if (state.query.isEmpty()) View.GONE else View.VISIBLE

            if (state.historyTracks != null) {
                historyAdapter.updateData(state.historyTracks)
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

    private fun setupListeners(btnBack: Button) {
        btnBack.setOnClickListener { finish() }

        clearEditSearchButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
            viewModel.onQueryCleared()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.onClearHistory()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.onSearchAction()
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.onRetryClick = { viewModel.onRetry() }
        adapter.onTrackClick = { track ->
            if (clickDebounce()) {
                viewModel.onTrackClicked(track)
                AudioPlayerActivity.start(this, track)
            }
        }
    }

    private fun setupHistoryRecycler() {
        historyAdapter = TrackAdapter(mutableListOf())
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter

        historyAdapter.onTrackClick = { track ->
            if (clickDebounce()) {
                viewModel.onTrackClicked(track)
                AudioPlayerActivity.start(this, track)
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        searchEditText.clearFocus()
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
        if (searchEditText.hasFocus()) {
            viewModel.onSearchFocused(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clickHandler.removeCallbacks(clickDebounceRunnable)
    }
}