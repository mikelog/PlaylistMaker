package com.example.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.models.Track

class SearchViewModel(
    private val searchInteractor: SearchTracksInteractor,
    private val historyInteractor: SearchHistoryInteractor,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ---- State sealed classes ----

    sealed class SearchState {
        object Idle : SearchState()
        object Loading : SearchState()
        data class Content(val tracks: List<Track>) : SearchState()
        object Empty : SearchState()
        object NetworkError : SearchState()
    }

    sealed class HistoryState {
        object Hidden : HistoryState()
        data class Visible(val tracks: List<Track>) : HistoryState()
    }

    // ---- LiveData ----

    private val _searchState = MutableLiveData<SearchState>(SearchState.Idle)
    val searchState: LiveData<SearchState> = _searchState

    private val _historyState = MutableLiveData<HistoryState>(HistoryState.Hidden)
    val historyState: LiveData<HistoryState> = _historyState

    // Текущий запрос — хранится в SavedStateHandle, переживает пересоздание Activity
    private val _currentQuery = savedStateHandle.getLiveData(KEY_QUERY, "")
    val currentQuery: LiveData<String> = _currentQuery

    // ---- Debounce ----

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(_currentQuery.value ?: "") }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val KEY_QUERY = "search_query"
    }

    // ---- Инициализация: восстанавливаем состояние если был активный запрос ----

    init {
        val restoredQuery = savedStateHandle.get<String>(KEY_QUERY).orEmpty()
        if (restoredQuery.isNotBlank()) {
            performSearch(restoredQuery)
        }
    }

    // ---- Публичные методы ----

    fun onQueryChanged(query: String) {
        savedStateHandle[KEY_QUERY] = query
        if (query.isBlank()) {
            handler.removeCallbacks(searchRunnable)
            _searchState.value = SearchState.Idle
        } else {
            searchDebounce()
        }
    }

    fun onSearchFocused(hasFocus: Boolean, currentQuery: String) {
        if (hasFocus && currentQuery.isBlank()) {
            showHistoryIfNotEmpty()
        } else if (!hasFocus) {
            _historyState.value = HistoryState.Hidden
        }
    }

    fun onQueryCleared() {
        handler.removeCallbacks(searchRunnable)
        savedStateHandle[KEY_QUERY] = ""
        _searchState.value = SearchState.Idle
        showHistoryIfNotEmpty()
    }

    fun onSearchAction() {
        handler.removeCallbacks(searchRunnable)
        performSearch(_currentQuery.value ?: "")
    }

    fun onRetry() {
        val query = _currentQuery.value ?: ""
        if (query.isNotBlank()) performSearch(query)
    }

    fun onTrackClicked(track: Track) {
        historyInteractor.addTrack(track)
        val current = _historyState.value
        if (current is HistoryState.Visible) {
            _historyState.value = HistoryState.Visible(historyInteractor.getHistory())
        }
    }

    fun onClearHistory() {
        historyInteractor.clearHistory()
        _historyState.value = HistoryState.Hidden
    }

    // ---- Приватные методы ----

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        _historyState.value = HistoryState.Hidden
        _searchState.value = SearchState.Loading

        searchInteractor.search(query) { tracks, isNetworkError ->
            _searchState.postValue(
                when {
                    isNetworkError -> SearchState.NetworkError
                    tracks.isNullOrEmpty() -> SearchState.Empty
                    else -> SearchState.Content(tracks)
                }
            )
        }
    }

    private fun showHistoryIfNotEmpty() {
        val history = historyInteractor.getHistory()
        _historyState.value = if (history.isNotEmpty()) {
            HistoryState.Visible(history)
        } else {
            HistoryState.Hidden
        }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(searchRunnable)
    }
}
