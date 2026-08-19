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
import com.example.playlistmaker.ui.medialibrary.viewmodels.EditPlaylistViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditPlaylistRoute(playlistId: Long, navController: NavController) {
    val viewModel: EditPlaylistViewModel = koinViewModel { parametersOf(playlistId) }
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
    val editingPlaylist by viewModel.editingPlaylist.observeAsState()
    val playlistCreated by viewModel.playlistCreated.observeAsState()

    LaunchedEffect(playlistCreated) {
        if (playlistCreated != null) navController.popBackStack()
    }

    PlaylistEditorScreen(
        toolbarTitle = stringResource(R.string.edit_playlist_title),
        submitButtonText = stringResource(R.string.save_button),
        coverUri = coverUri,
        fallbackCoverPath = editingPlaylist?.coverPath ?: "",
        initialName = editingPlaylist?.name,
        initialDescription = editingPlaylist?.description,
        confirmDiscardOnBack = false,
        onCoverAreaClick = {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onSubmit = { name, description -> viewModel.saveEditedPlaylist(name, description) },
        onBack = { navController.popBackStack() }
    )
}
