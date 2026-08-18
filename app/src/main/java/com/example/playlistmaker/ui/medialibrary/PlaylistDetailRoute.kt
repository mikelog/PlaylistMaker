package com.example.playlistmaker.ui.medialibrary

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistDetailViewModel
import com.example.playlistmaker.ui.root.Routes
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.ui.theme.colorAttr
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlaylistDetailRoute(playlistId: Long, navController: NavController) {
    val viewModel: PlaylistDetailViewModel = koinViewModel { parametersOf(playlistId) }
    val context = LocalContext.current

    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)
    val snackbarHostState = remember { SnackbarHostState() }

    // shareText is a one-shot event whose payload is itself nullable (null = "nothing to
    // share"), so observeAsState() can't tell "no event yet" from "event fired with null".
    // observeForever on the raw LiveData sidesteps that ambiguity entirely.
    val scope = rememberCoroutineScope()
    DisposableEffect(viewModel) {
        val observer = Observer<String?> { text ->
            if (text == null) {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.share_playlist_empty))
                }
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(intent, null))
            }
        }
        viewModel.shareText.observeForever(observer)
        onDispose { viewModel.shareText.removeObserver(observer) }
    }

    val playlistDeleted by viewModel.playlistDeleted.observeAsState()
    LaunchedEffect(playlistDeleted) {
        if (playlistDeleted != null) navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PlaylistDetailScreen(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onTrackClick = { track: Track ->
                navController.navigate(Routes.audioPlayer(track))
            },
            onEditClick = {
                navController.navigate(Routes.editPlaylist(playlistId))
            },
            onShareClick = { viewModel.sharePlaylist() }
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
