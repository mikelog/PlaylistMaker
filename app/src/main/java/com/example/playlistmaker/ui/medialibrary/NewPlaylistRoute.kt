package com.example.playlistmaker.ui.medialibrary

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.medialibrary.viewmodels.NewPlaylistViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun NewPlaylistRoute(navController: NavController) {
    val viewModel: NewPlaylistViewModel = koinViewModel()
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.onCoverSelected(uri)
        }
    }

    val coverUri by viewModel.coverUri.observeAsState()
    val playlistCreated by viewModel.playlistCreated.observeAsState()

    LaunchedEffect(playlistCreated) {
        val name = playlistCreated ?: return@LaunchedEffect
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("playlist_created_name", name)
        navController.popBackStack()
    }

    PlaylistEditorScreen(
        toolbarTitle = stringResource(R.string.new_playlist_title),
        submitButtonText = stringResource(R.string.create_playlist_button),
        coverUri = coverUri,
        fallbackCoverPath = "",
        initialName = null,
        initialDescription = null,
        confirmDiscardOnBack = true,
        onCoverAreaClick = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onSubmit = { name, description -> viewModel.createPlaylist(name, description) },
        onBack = { navController.popBackStack() }
    )
}
