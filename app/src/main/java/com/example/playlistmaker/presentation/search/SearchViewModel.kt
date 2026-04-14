package com.example.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.models.Track

class SearchViewModel(
    private val searchInteractor: SearchTracksInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _screenState = MutableLiveData(ScreenState())
    val screenState: LiveData<ScreenState> = _screenState

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        performSearch(_screenState.value?.query ?: "")
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }

    fun onQueryChanged(query: String, fieldHasFocus: Boolean) {
        Log.d("SEARCH_DEBUG", "VM onQueryChanged: $query focus=$fieldHasFocus")
        val current = _screenState.value ?: ScreenState()
        if (query.isBlank()) {
            handler.removeCallbacks(searchRunnable)
            _screenState.value = current.copy(
                query = query,
                searchContent = SearchContent.Idle,
                historyTracks = if (fieldHasFocus) getHistoryOrNull() else null
            )
        } else {
            _screenState.value = current.copy(
                query = query,
                historyTracks = null
            )
            searchDebounce()
        }
    }

    fun onSearchFocused(hasFocus: Boolean) {
        val current = _screenState.value ?: ScreenState()
        if (hasFocus && current.query.isBlank()) {
            _screenState.value = current.copy(historyTracks = getHistoryOrNull())
        } else if (!hasFocus) {
            _screenState.value = current.copy(historyTracks = null)
        }
    }

    fun onQueryCleared() {
        handler.removeCallbacks(searchRunnable)
        _screenState.value = ScreenState(
            query = "",
            searchContent = SearchContent.Idle,
            historyTracks = getHistoryOrNull()
        )
    }

    fun onSearchAction() {
        handler.removeCallbacks(searchRunnable)
        performSearch(_screenState.value?.query ?: "")
    }

    fun onRetry() {
        val query = _screenState.value?.query ?: ""
        if (query.isNotBlank()) performSearch(query)
    }

    fun onTrackClicked(track: Track) {
        historyInteractor.addTrack(track)
        val current = _screenState.value ?: return
        if (current.historyTracks != null) {
            _screenState.value = current.copy(historyTracks = historyInteractor.getHistory())
        }
    }

    fun onClearHistory() {
        historyInteractor.clearHistory()
        _screenState.value = (_screenState.value ?: ScreenState()).copy(historyTracks = null)
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        val current = _screenState.value ?: ScreenState()
        _screenState.value = current.copy(
            searchContent = SearchContent.Loading,
            historyTracks = null
        )
        searchInteractor.search(query) { tracks, isNetworkError ->
            val content = when {
                isNetworkError -> SearchContent.NetworkError
                tracks.isNullOrEmpty() -> SearchContent.Empty
                else -> SearchContent.Tracks(tracks)
            }
            _screenState.postValue(
                (_screenState.value ?: ScreenState()).copy(searchContent = content)
            )
        }
    }

    private fun getHistoryOrNull(): List<Track>? {
        val history = historyInteractor.getHistory()
        return history.ifEmpty { null }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(searchRunnable)
    }
}
