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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.presentation.adapter.TrackAdapter
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerActivity
import com.example.playlistmaker.util.applyEdgeToEdge

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

    private lateinit var viewModel: SearchViewModel

    // Debounce клика — UI-логика, остаётся в Activity
    private var isClickAllowed = true
    private val clickHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val clickDebounceRunnable = Runnable { isClickAllowed = true }

    // Флаг: не реагировать на doOnTextChanged пока мы сами программно ставим текст
    private var isRestoringText = false

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // --- Инициализация ViewModel через ViewModelProvider ---
        val factory = SearchViewModelFactory(
            owner = this,
            searchInteractor = Creator.provideSearchTracksInteractor(),
            historyInteractor = Creator.provideSearchHistoryInteractor(this)
        )
        viewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]

        // --- Инициализация Views ---
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
        clearEditSearchButton = findViewById(R.id.clearEditSearchButton)

        setupRecyclerView()
        setupHistoryRecycler()
        setupListeners(btnBack)
        observeViewModel()
    }

    // ---- Подписка на LiveData ----

    private fun observeViewModel() {
        // Восстанавливаем текст в поле из SavedStateHandle (ViewModel сам хранит запрос)
        viewModel.currentQuery.observe(this) { query ->
            if (searchEditText.text.toString() != query) {
                isRestoringText = true
                searchEditText.setText(query)
                searchEditText.setSelection(query.length)
                isRestoringText = false
            }
            clearEditSearchButton.visibility =
                if (query.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.searchState.observe(this) { state ->
            when (state) {
                is SearchViewModel.SearchState.Idle -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    adapter.clearData()
                }
                is SearchViewModel.SearchState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
                is SearchViewModel.SearchState.Content -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateData(state.tracks)
                }
                is SearchViewModel.SearchState.Empty -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateData(emptyList())
                }
                is SearchViewModel.SearchState.NetworkError -> {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.showNoConnection()
                }
            }
        }

        viewModel.historyState.observe(this) { state ->
            when (state) {
                is SearchViewModel.HistoryState.Hidden -> {
                    containerSearchHistory.visibility = View.GONE
                    historyTitle.visibility = View.GONE
                    historyRecycler.visibility = View.GONE
                    clearHistoryButton.visibility = View.GONE
                }
                is SearchViewModel.HistoryState.Visible -> {
                    historyAdapter.updateData(state.tracks)
                    containerSearchHistory.visibility = View.VISIBLE
                    historyTitle.visibility = View.VISIBLE
                    historyRecycler.visibility = View.VISIBLE
                    clearHistoryButton.visibility = View.VISIBLE
                }
            }
        }
    }

    // ---- Настройка слушателей ----

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
            viewModel.onSearchFocused(hasFocus, searchEditText.text.toString())
        }

        searchEditText.doOnTextChanged { text, _, _, _ ->
            // Игнорируем изменения, которые мы сами вызвали при восстановлении текста
            if (!isRestoringText) {
                viewModel.onQueryChanged(text?.toString() ?: "")
            }
        }
    }

    // ---- RecyclerView ----

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

    // ---- Вспомогательные ----

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

    override fun onDestroy() {
        super.onDestroy()
        clickHandler.removeCallbacks(clickDebounceRunnable)
    }
}
