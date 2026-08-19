package com.example.playlistmaker.ui.medialibrary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.common.AppAlertDialog
import com.example.playlistmaker.ui.common.GlideAsyncImage
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistDetailViewModel
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: PlaylistDetailViewModel,
    onBackClick: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    val playlist by viewModel.playlist.observeAsState()
    val tracks by viewModel.tracks.observeAsState(emptyList())
    val totalDuration by viewModel.totalDuration.observeAsState("0")

    var showMenuSheet by remember { mutableStateOf(false) }
    var trackPendingDelete by remember { mutableStateOf<Track?>(null) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState()
    )

    PlaylistMakerTheme {
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 300.dp,
        sheetContainerColor = backgroundColor,
        sheetDragHandle = { SheetDragHandle() },
        sheetContent = {
            if (tracks.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_playlist_tracks),
                    color = textColor,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(tracks, key = { it.trackId }) { track ->
                        PlaylistTrackRow(
                            track = track,
                            onClick = { onTrackClick(track) },
                            onLongClick = { trackPendingDelete = track }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
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

            val coverPath = playlist?.coverPath.orEmpty()
            GlideAsyncImage(
                model = if (coverPath.isNotEmpty()) File(coverPath) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(4.dp))
            )

            Text(
                text = playlist?.name.orEmpty(),
                color = textColor,
                fontSize = 19.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )

            val description = playlist?.description.orEmpty()
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    color = textColor,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp)
                )
            }

            val minutes = totalDuration.toIntOrNull() ?: 0
            Text(
                text = pluralStringResource(R.plurals.minutes_count, minutes, totalDuration) +
                        " • " +
                        pluralStringResource(R.plurals.track_count, tracks.size, tracks.size),
                color = textColor,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share_24),
                        contentDescription = null,
                        tint = textColor
                    )
                }
                IconButton(onClick = { showMenuSheet = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert_24),
                        contentDescription = null,
                        tint = textColor
                    )
                }
            }
        }
    }

    if (showMenuSheet && playlist != null) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = backgroundColor
        ) {
            PlaylistMenuSheetContent(
                coverPath = playlist!!.coverPath,
                name = playlist!!.name,
                trackCount = tracks.size,
                onShare = {
                    showMenuSheet = false
                    onShareClick()
                },
                onEdit = {
                    showMenuSheet = false
                    onEditClick()
                },
                onDelete = {
                    showMenuSheet = false
                    showDeletePlaylistDialog = true
                }
            )
        }
    }

    trackPendingDelete?.let { track ->
        AppAlertDialog(
            onDismissRequest = { trackPendingDelete = null },
            title = { Text(stringResource(R.string.delete_track_dialog_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeTrack(track.trackId)
                        trackPendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.blue_background)
                    )
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { trackPendingDelete = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.blue_background)
                    )
                ) {
                    Text(stringResource(R.string.no))
                }
            }
        )
    }

    if (showDeletePlaylistDialog) {
        AppAlertDialog(
            onDismissRequest = { showDeletePlaylistDialog = false },
            title = { Text(stringResource(R.string.delete_playlist_title)) },
            text = { Text(stringResource(R.string.delete_playlist_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeletePlaylistDialog = false
                        viewModel.deletePlaylist()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.blue_background)
                    )
                ) { Text(stringResource(R.string.delete_playlist_confirm)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeletePlaylistDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = colorResource(R.color.blue_background)
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    }
}

@Composable
private fun SheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(colorAttr(R.attr.yp_gray_color), RoundedCornerShape(2.dp))
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaylistTrackRow(track: Track, onClick: () -> Unit, onLongClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideAsyncImage(model = track.artworkUrl100, modifier = Modifier.size(46.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = track.trackName,
                color = colorAttr(MaterialR.attr.colorOnSecondary),
                fontSize = 16.sp,
                maxLines = 1
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = track.artistName,
                    color = colorAttr(R.attr.yp_gray_color),
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = track.trackTime,
                    color = colorAttr(R.attr.yp_gray_color),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun PlaylistMenuSheetContent(
    coverPath: String,
    name: String,
    trackCount: Int,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideAsyncImage(
                model = if (coverPath.isNotEmpty()) File(coverPath) else null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = name, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(
                    text = pluralStringResource(R.plurals.track_count, trackCount, trackCount),
                    color = textColor,
                    fontSize = 11.sp
                )
            }
        }

        MenuItemRow(text = stringResource(R.string.menu_item_share), onClick = onShare)
        MenuItemRow(text = stringResource(R.string.menu_item_edit), onClick = onEdit)
        MenuItemRow(text = stringResource(R.string.menu_item_delete_playlist), onClick = onDelete, bottomPadding = 16.dp)
    }
}

@Composable
private fun MenuItemRow(text: String, onClick: () -> Unit, bottomPadding: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = bottomPadding)
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 16.sp
        )
    }
}
