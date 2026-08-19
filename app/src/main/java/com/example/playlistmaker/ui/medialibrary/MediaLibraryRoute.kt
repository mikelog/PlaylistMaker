package com.example.playlistmaker.ui.medialibrary

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.medialibrary.viewmodels.FavouriteTracksViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.ui.root.Routes
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.ui.theme.colorAttr
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaLibraryRoute(
    savedStateHandle: SavedStateHandle,
    navController: NavController
) {
    val favouriteTracksViewModel: FavouriteTracksViewModel = koinViewModel()
    val playlistsViewModel: PlaylistsViewModel = koinViewModel()

    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        playlistsViewModel.loadPlaylists()
    }

    val playlistCreatedName by savedStateHandle
        .getLiveData<String>("playlist_created_name")
        .observeAsState()

    LaunchedEffect(playlistCreatedName) {
        val name = playlistCreatedName ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(context.getString(R.string.playlist_created_toast, name))
        savedStateHandle.remove<String>("playlist_created_name")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MediaLibraryScreen(
            favouriteTracksViewModel = favouriteTracksViewModel,
            playlistsViewModel = playlistsViewModel,
            onTrackClick = { track: Track ->
                navController.navigate(Routes.audioPlayer(track))
            },
            onPlaylistClick = { playlist ->
                navController.navigate(Routes.playlistDetail(playlist.playlistId))
            },
            onNewPlaylistClick = {
                navController.navigate(Routes.NEW_PLAYLIST)
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = textColor,
                contentColor = backgroundColor
            )
        }
    }
}
