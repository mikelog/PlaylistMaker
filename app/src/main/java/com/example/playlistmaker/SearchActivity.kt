package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackAdapter

    private var searchQuery: String = ""

    private val allTracks: List<Track> = listOf(
        Track(
            "Smells Like Teen Spirit",
            "Nirvana",
            "5:01",
            "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Billie Jean",
            "Michael Jackson",
            "4:35",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"
        ),
        Track(
            "Stayin' Alive",
            "Bee Gees",
            "4:10",
            "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Whole Lotta Love",
            "Led Zeppelin",
            "5:33",
            "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"
        ),
        Track(
            "Sweet Child O'Mine",
            "Guns N' Roses",
            "5:03",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
        ),
        Track(
            "Very very very loooooooooooooooooong track title",
            "Guns N' Roses",
            "5:03",
            "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc84-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val btnBack: Button = findViewById(R.id.btnBack)
        searchEditText = findViewById(R.id.searchEditText)
        recyclerView = findViewById(R.id.searchRecyclerView)

        btnBack.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        initSearch()
    }

    private fun setupRecyclerView() {
        adapter = TrackAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun initSearch() {
        updateClearIcon(null)

        searchEditText.doOnTextChanged { text, _, _, _ ->
            searchQuery = text?.toString().orEmpty()
            updateClearIcon(text)
            filterTracks(searchQuery)
        }

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
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    private fun filterTracks(query: String) {
        if (query.isBlank()) {
            adapter.updateData(emptyList())
            return
        }

        val filteredTracks = allTracks.filter { track ->
            track.trackName.contains(query, ignoreCase = true) ||
                    track.artistName.contains(query, ignoreCase = true)
        }

        adapter.updateData(filteredTracks)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredQuery =
            savedInstanceState.getString(SEARCH_QUERY_KEY).orEmpty()
        searchEditText.setText(restoredQuery)
        searchEditText.setSelection(restoredQuery.length)
        filterTracks(restoredQuery)
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
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        searchEditText.clearFocus()
    }

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
    }
}
