package com.example.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchTracksInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val _screenState = MutableLiveData(ScreenState())
    val screenState: LiveData<ScreenState> = _screenState

    private var searchDebounceJob: Job? = null
    private var clickDebounceJob: Job? = null

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    fun onQueryChanged(query: String, fieldHasFocus: Boolean) {
        val current = _screenState.value ?: ScreenState()
        if (query.isBlank()) {
            searchDebounceJob?.cancel()
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
            scheduleSearch(query)
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
        searchDebounceJob?.cancel()
        _screenState.value = ScreenState(
            query = "",
            searchContent = SearchContent.Idle,
            historyTracks = getHistoryOrNull()
        )
    }

    fun onSearchAction() {
        searchDebounceJob?.cancel()
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

    fun clickDebounce(): Boolean {
        if (clickDebounceJob?.isActive == true) return false
        clickDebounceJob = viewModelScope.launch {
            delay(CLICK_DEBOUNCE_DELAY)
        }
        return true
    }

    fun onClearHistory() {
        historyInteractor.clearHistory()
        _screenState.value = (_screenState.value ?: ScreenState()).copy(historyTracks = null)
    }

    private fun scheduleSearch(query: String) {
        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(query)
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) return
        val current = _screenState.value ?: ScreenState()
        _screenState.value = current.copy(
            searchContent = SearchContent.Loading,
            historyTracks = null
        )
        viewModelScope.launch {
            searchInteractor.search(query).collect { (tracks, isNetworkError) ->
                val content = when {
                    isNetworkError -> SearchContent.NetworkError
                    tracks.isNullOrEmpty() -> SearchContent.Empty
                    else -> SearchContent.Tracks(tracks)
                }
                _screenState.value =
                    (_screenState.value ?: ScreenState()).copy(searchContent = content)
            }
        }
    }

    private fun getHistoryOrNull(): List<Track>? {
        val history = historyInteractor.getHistory()
        return history.ifEmpty { null }
    }
}
