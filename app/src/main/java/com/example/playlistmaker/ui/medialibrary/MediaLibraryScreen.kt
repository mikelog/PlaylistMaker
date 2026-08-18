package com.example.playlistmaker.ui.medialibrary

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.common.GlideAsyncImage
import com.example.playlistmaker.ui.common.TrackRow
import com.example.playlistmaker.ui.medialibrary.viewmodels.FavouriteTracksViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistsViewModel
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr
import kotlinx.coroutines.launch

@Composable
fun MediaLibraryScreen(
    favouriteTracksViewModel: FavouriteTracksViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onTrackClick: (Track) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onNewPlaylistClick: () -> Unit
) {
    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    val tabTitles = listOf(
        stringResource(R.string.favourite_tracks),
        stringResource(R.string.playlists)
    )
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()

    PlaylistMakerTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(start = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.library),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = backgroundColor,
                contentColor = textColor
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title, fontSize = 14.sp) },
                        selectedContentColor = textColor,
                        unselectedContentColor = textColor
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> FavouriteTracksTab(favouriteTracksViewModel, onTrackClick)
                    1 -> PlaylistsTab(playlistsViewModel, onPlaylistClick, onNewPlaylistClick)
                }
            }
        }
    }
}

@Composable
private fun FavouriteTracksTab(
    viewModel: FavouriteTracksViewModel,
    onTrackClick: (Track) -> Unit
) {
    val state by viewModel.screenState.observeAsState()

    when (val screenState = state) {
        is FavouriteTracksViewModel.ScreenState.Content -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(screenState.tracks, key = { it.trackId }) { track ->
                    TrackRow(track = track, onClick = { onTrackClick(track) })
                }
            }
        }

        else -> {
            EmptyMediaLibraryPlaceholder(
                topPadding = 106.dp,
                message = stringResource(R.string.empty_medialibrary_message)
            )
        }
    }
}

@Composable
private fun PlaylistsTab(
    viewModel: PlaylistsViewModel,
    onPlaylistClick: (Playlist) -> Unit,
    onNewPlaylistClick: () -> Unit
) {
    val playlists by viewModel.playlists.observeAsState(emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onNewPlaylistClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp)
        ) {
            Text(stringResource(R.string.new_playlist), fontSize = 14.sp)
        }

        if (playlists.isEmpty()) {
            EmptyMediaLibraryPlaceholder(
                topPadding = 46.dp,
                message = stringResource(R.string.empty_playlists_message)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(playlists, key = { it.playlistId }) { playlist ->
                    PlaylistGridItem(playlist = playlist, onClick = { onPlaylistClick(playlist) })
                }
            }
        }
    }
}

@Composable
private fun PlaylistGridItem(playlist: Playlist, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        GlideAsyncImage(
            model = if (playlist.coverPath.isNotEmpty()) java.io.File(playlist.coverPath) else null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
        )
        Text(
            text = playlist.name,
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 12.sp,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = pluralStringResource(R.plurals.track_count, playlist.trackCount, playlist.trackCount),
            color = colorResource(R.color.yp_gray),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun EmptyMediaLibraryPlaceholder(topPadding: Dp, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.ic_empty_medialibrary_120),
            contentDescription = null,
            modifier = Modifier.height(120.dp)
        )
        Text(
            text = message,
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )
    }
}
