package com.example.playlistmaker.ui.audioplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.audioplayer.AddToPlaylistResult
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerViewModel
import com.example.playlistmaker.ui.common.GlideAsyncImage
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    track: Track,
    viewModel: AudioPlayerViewModel,
    onBackClick: () -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    val screenState by viewModel.screenState.observeAsState(AudioPlayerViewModel.PlayerScreenState())
    val isFavorite by viewModel.isFavorite.observeAsState(track.isFavorite)
    val playlists by viewModel.playlists.observeAsState(emptyList())
    val addToPlaylistResult by viewModel.addToPlaylistResult.observeAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(addToPlaylistResult) {
        when (val result = addToPlaylistResult) {
            is AddToPlaylistResult.Success -> {
                showBottomSheet = false
                snackbarHostState.showSnackbar(
                    context.getString(R.string.added_to_playlist_toast, result.playlistName)
                )
            }
            is AddToPlaylistResult.AlreadyAdded -> {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.already_in_playlist_toast, result.playlistName)
                )
            }
            null -> Unit
        }
    }

    PlaylistMakerTheme {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24),
                        contentDescription = null,
                        tint = textColor
                    )
                }
            }

            GlideAsyncImage(
                model = track.getCoverArtwork(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )

            Text(
                text = track.trackName,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp)
            )
            Text(
                text = track.artistName,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 24.dp, top = 12.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        viewModel.loadPlaylists()
                        showBottomSheet = true
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_playlist_add_50),
                        contentDescription = null,
                        tint = textColor
                    )
                }

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable(enabled = screenState.isPlayEnabled) { viewModel.onPlayPauseClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            if (screenState.isPlaying) R.drawable.ic_pause_100 else R.drawable.ic_play_100
                        ),
                        contentDescription = null,
                        tint = textColor.copy(alpha = if (screenState.isPlayEnabled) 1f else 0.5f),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = { viewModel.onFavoriteClicked() },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFavorite) R.drawable.ic_favorite_active_50 else R.drawable.ic_favorite_50
                        ),
                        contentDescription = null,
                        tint = textColor
                    )
                }
            }

            Text(
                text = screenState.progress,
                color = textColor,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                TrackInfoRow(stringResource(R.string.track_duration), track.trackTime)
                TrackInfoRow(stringResource(R.string.track_album), track.collectionName)
                TrackInfoRow(stringResource(R.string.track_release_date), track.getReleaseYear())
                TrackInfoRow(stringResource(R.string.track_genre), track.primaryGenreName)
                TrackInfoRow(stringResource(R.string.track_cuntry), track.country)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            // Snackbar colors are intentionally inverted from the screen's own text/background
            // pair (dark-on-light in day theme, light-on-dark in night theme) to stay legible
            // against either background, matching the app's original Fragment-level Snackbar.
            Snackbar(
                snackbarData = data,
                containerColor = textColor,
                contentColor = backgroundColor
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = backgroundColor
        ) {
            AddToPlaylistSheetContent(
                playlists = playlists,
                onNewPlaylistClick = {
                    showBottomSheet = false
                    onCreatePlaylistClick()
                },
                onPlaylistClick = { playlist -> viewModel.addTrackToPlaylist(playlist) }
            )
        }
    }
    }
}

@Composable
private fun TrackInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colorAttr(R.attr.yp_gray_color),
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 13.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun AddToPlaylistSheetContent(
    playlists: List<Playlist>,
    onNewPlaylistClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.add_to_playlist_title),
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 19.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onNewPlaylistClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        ) {
            Text(stringResource(R.string.new_playlist))
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(playlists, key = { it.playlistId }) { playlist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPlaylistClick(playlist) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlideAsyncImage(
                        model = if (playlist.coverPath.isNotEmpty()) File(playlist.coverPath) else null,
                        modifier = Modifier.size(50.dp)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = playlist.name,
                            color = colorAttr(MaterialR.attr.colorOnSecondary),
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                        Text(
                            text = pluralStringResource(
                                R.plurals.track_count,
                                playlist.trackCount,
                                playlist.trackCount
                            ),
                            color = colorAttr(R.attr.yp_gray_color),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
