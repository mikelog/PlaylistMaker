package com.example.playlistmaker

import ItunesApi
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


const val BASE_URL = "https://itunes.apple.com"

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter
    private lateinit var historyRepository: SearchHistoryRepository
    private lateinit var historyTitle: TextView
    private lateinit var historyRecycler: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var containerSearchHistory: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var api: ItunesApi
    private var lastQuery: String = ""

    // Handler для debounce
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(lastQuery) }

    // Debounce для кликов
    private var isClickAllowed = true
    private val clickDebounceRunnable = Runnable { isClickAllowed = true }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L // 2 секунды
        private const val CLICK_DEBOUNCE_DELAY = 1000L // 1 секунда
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val rootView = findViewById<View>(R.id.searchRoot)
        val toolBar = findViewById<View>(R.id.searchToolbar)
        applyEdgeToEdge(rootView = rootView, topView = toolBar)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ItunesApi::class.java)

        val btnBack: Button = findViewById(R.id.btnBack)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.searchRecyclerView)
        progressBar = findViewById(R.id.progressBar)

        historyTitle = findViewById(R.id.historyTitle)
        historyRecycler = findViewById(R.id.historyRecyclerView)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)
        containerSearchHistory = findViewById(R.id.containerSearchHistory)

        setupHistoryRecycler()

        historyRepository = SearchHistoryRepositoryImpl(
            getSharedPreferences("playlist_prefs", MODE_PRIVATE),
            Gson()
        )

        btnBack.setOnClickListener { finish() }
        setupRecyclerView()
        initSearch()

        clearHistoryButton.setOnClickListener {
            historyRepository.clearHistory()
            historyTitle.visibility = View.GONE
            historyRecycler.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
            containerSearchHistory.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.onRetryClick = {
            if (lastQuery.isNotBlank()) {
                performSearch(lastQuery)
            }
        }
        adapter.onTrackClick = { track ->
            if (clickDebounce()) {
                historyRepository.addTrack(track)
                AudioPlayerActivity.start(this, track)
            }
        }
    }

    private fun initSearch() {
        updateClearIcon(null)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString()
                lastQuery = query
                searchDebounce() // Убираем старый debounce
                performSearch(query)
                hideKeyboard()
                true
            } else false
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                showHistory()
            } else if (!hasFocus) {
                hideHistory()
            }
        }

        // Отображение иконки очистки
        searchEditText.doOnTextChanged { text, _, _, _ ->
            updateClearIcon(text)
            if (!text.isNullOrEmpty()) {
                hideHistory()
                lastQuery = text.toString()
                searchDebounce()
            } else {
                // Отменяем поиск если поле очищено
                searchDebounce()
                adapter.clearData()
                if (searchEditText.hasFocus()) {
                    showHistory()
                }
            }
        }

        // Нажатие на крестик очистки
        searchEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = searchEditText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val clearIconStart =
                        searchEditText.width -
                                searchEditText.paddingEnd -
                                drawableEnd.intrinsicWidth

                    if (event.x >= clearIconStart) {
                        searchEditText.text.clear()
                        hideKeyboard()
                        adapter.clearData()
                        showHistory()
                        return@setOnTouchListener true
                    }
                }
            }
            false
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

        api.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(
                call: Call<TracksResponse>,
                response: Response<TracksResponse>
            ) {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                if (response.isSuccessful) {
                    val tracks = response.body()?.results
                        ?.mapNotNull { it.toTrack() }
                        ?: emptyList()

                    if (tracks.isEmpty()) {
                        adapter.updateData(emptyList())
                    } else {
                        adapter.updateData(tracks)
                    }

                } else {
                    adapter.showNoConnection()
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                // Скрываем прогресс-бар
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                adapter.showNoConnection()
            }
        })
    }

    private fun updateClearIcon(text: CharSequence?) {
        val searchIcon = ContextCompat.getDrawable(this, R.drawable.ic_search_16)
        val clearIcon =
            if (text.isNullOrEmpty()) null
            else ContextCompat.getDrawable(this, R.drawable.ic_search_clear_16)
        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon,
            null,
            clearIcon,
            null
        )
    }
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        searchEditText.clearFocus()
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
        if (restoredQuery.isNotBlank()) {
            performSearch(restoredQuery)
        }
    }

    private fun showHistory() {
        val history = historyRepository.getHistory()

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
                if (track.previewUrl.isNullOrBlank()) {
                    api.lookup(track.trackId).enqueue(object : Callback<TracksResponse> {
                        override fun onResponse(
                            call: Call<TracksResponse>,
                            response: Response<TracksResponse>
                        ) {
                            val updatedTrack = response.body()?.results
                                ?.firstOrNull()
                                ?.toTrack()

                            if (updatedTrack != null) {
                                historyRepository.addTrack(updatedTrack)
                                AudioPlayerActivity.start(this@SearchActivity, updatedTrack)
                            }
                        }

                        override fun onFailure(call: Call<TracksResponse>, t: Throwable) {}
                    })
                } else {
                    historyRepository.addTrack(track)
                    AudioPlayerActivity.start(this@SearchActivity, track)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
        handler.removeCallbacks(clickDebounceRunnable)
    }
}