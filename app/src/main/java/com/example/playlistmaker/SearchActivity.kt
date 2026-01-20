package com.example.playlistmaker

import ItunesApi
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

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
    private  lateinit var containerSearchHistory: LinearLayout


    private lateinit var api: ItunesApi

    private var lastQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ItunesApi::class.java)

        val btnBack: Button = findViewById(R.id.btnBack)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.searchRecyclerView)

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
            historyRepository.addTrack(track)
            AudioPlayerActivity.start(this, track)
        }
    }

    private fun initSearch() {
        updateClearIcon(null)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString()
                lastQuery = query
                performSearch(query)
                hideKeyboard()
                true
            } else false
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty()) {
                showHistory()
            }
        }

        // Отображение иконки очистки
        searchEditText.doOnTextChanged { text, _, _, _ ->
            updateClearIcon(text)
            if (!text.isNullOrEmpty()) {
                historyTitle.visibility = View.GONE
                historyRecycler.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
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
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        api.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(
                call: Call<TracksResponse>,
                response: Response<TracksResponse>
            ) {
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

    private fun setupHistoryRecycler() {
        historyAdapter = TrackAdapter(mutableListOf())
        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter

        historyAdapter.onTrackClick = { track ->
            historyRepository.addTrack(track)
            AudioPlayerActivity.start(this, track)
        }
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
    }
}
