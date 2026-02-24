package com.example.playlistmaker.presentation.search

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.util.applyEdgeToEdge
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerActivity
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.util.Creator

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

    private lateinit var searchInteractor: SearchTracksInteractor
    private lateinit var historyInteractor: SearchHistoryInteractor

    private var lastQuery: String = ""
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(lastQuery) }
    private var isClickAllowed = true
    private val clickDebounceRunnable = Runnable { isClickAllowed = true }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Получаем интеракторы через Creator
        searchInteractor = Creator.provideSearchTracksInteractor()
        historyInteractor = Creator.provideSearchHistoryInteractor(this)

        val rootView = findViewById<View>(R.id.searchRoot)
        val toolBar = findViewById<View>(R.id.searchToolbar)
        applyEdgeToEdge(rootView = rootView, topView = toolBar)

        val btnBack: Button = findViewById(R.id.btnBack)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.searchRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        historyTitle = findViewById(R.id.historyTitle)
        historyRecycler = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        containerSearchHistory = findViewById(R.id.containerSearchHistory)

        setupHistoryRecycler()

        btnBack.setOnClickListener { finish() }
        setupRecyclerView()
        initSearch()

        clearHistoryButton.setOnClickListener {
            historyInteractor.clearHistory()
            hideHistory()
        }

        clearEditSearchButton = findViewById(R.id.clearEditSearchButton)

        clearEditSearchButton.setOnClickListener {
            searchEditText.text.clear()
            hideKeyboard()
            adapter.clearData()
            showHistory()
        }
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.onRetryClick = {
            if (lastQuery.isNotBlank()) performSearch(lastQuery)
        }
        adapter.onTrackClick = { track ->
            if (clickDebounce()) {
                historyInteractor.addTrack(track)
                AudioPlayerActivity.start(this, track)
            }
        }
    }

    private fun initSearch() {
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                lastQuery = searchEditText.text.toString()
                searchDebounce()
                performSearch(lastQuery)
                hideKeyboard()
                true
            } else false
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) showHistory()
            else if (!hasFocus) hideHistory()
        }
        searchEditText.doOnTextChanged { text, _, _, _ ->
            clearEditSearchButton.visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
            if (!text.isNullOrEmpty()) {
                hideHistory()
                lastQuery = text.toString()
                searchDebounce()
            } else {
                searchDebounce()
                adapter.clearData()
                if (searchEditText.hasFocus()) showHistory()
            }
        }
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        if (lastQuery.isNotBlank()) {
            handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed(clickDebounceRunnable, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        searchInteractor.search(query) { tracks, isNetworkError ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                when {
                    isNetworkError -> adapter.showNoConnection()
                    tracks.isNullOrEmpty() -> adapter.updateData(emptyList())
                    else -> adapter.updateData(tracks)
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        searchEditText.clearFocus()
    }

    private fun showHistory() {
        val history = historyInteractor.getHistory()
        if (history.isNotEmpty()) {
            historyAdapter.updateData(history)
            historyTitle.visibility = View.VISIBLE
            historyRecycler.visibility = View.VISIBLE
            clearHistoryButton.visibility = View.VISIBLE
            containerSearchHistory.visibility = View.VISIBLE
        }
    }

    private fun hideHistory() {
        historyTitle.visibility = View.GONE
        historyRecycler.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        containerSearchHistory.visibility = View.GONE
    }

    private fun setupHistoryRecycler() {
        historyAdapter = TrackAdapter(mutableListOf())
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter
        historyAdapter.onTrackClick = { track ->
            if (clickDebounce()) {
                historyInteractor.addTrack(track)
                AudioPlayerActivity.start(this, track)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, lastQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredQuery = savedInstanceState.getString(SEARCH_QUERY_KEY).orEmpty()
        searchEditText.setText(restoredQuery)
        searchEditText.setSelection(restoredQuery.length)
        lastQuery = restoredQuery
        if (restoredQuery.isNotBlank()) performSearch(restoredQuery)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(clickDebounceRunnable)
    }
}
