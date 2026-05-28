package com.example.playlistmaker.ui.medialibrary.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavouriteTracksInteractor
import com.example.playlistmaker.domain.models.Track
import kotlinx.coroutines.launch

class FavouriteTracksViewModel(
    private val favouriteInteractor: FavouriteTracksInteractor
) : ViewModel() {

    sealed class ScreenState {
        object Empty : ScreenState()
        data class Content(val tracks: List<Track>) : ScreenState()
    }

    private val _screenState = MutableLiveData<ScreenState>(ScreenState.Empty)
    val screenState: LiveData<ScreenState> = _screenState

    init {
        viewModelScope.launch {
            favouriteInteractor.getAllTracks().collect { tracks ->
                _screenState.postValue(
                    if (tracks.isEmpty()) ScreenState.Empty else ScreenState.Content(tracks)
                )
            }
        }
    }
}
