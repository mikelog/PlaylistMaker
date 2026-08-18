package com.example.playlistmaker.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.R
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.search.SearchContent
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.ui.common.TrackRow
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onTrackClick: (Track) -> Unit
) {
    val state by viewModel.screenState.observeAsState()
    val screenState = state ?: return

    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val textColor = colorAttr(MaterialR.attr.colorOnSecondary)

    // The field owns its own editing state; screenState.query only mirrors it for the
    // ViewModel's own use (debounce, history, etc.) and must never be written back into
    // the field mid-composition, or the IME's composing session gets cancelled every keystroke.
    var query by remember {
        mutableStateOf(TextFieldValue(screenState.query, TextRange(screenState.query.length)))
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun hideKeyboard() {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

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
                text = stringResource(R.string.search),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        SearchField(
            value = query,
            onValueChange = { newValue ->
                query = newValue
                viewModel.onQueryChanged(newValue.text, fieldHasFocus = true)
            },
            onClear = {
                query = TextFieldValue("")
                hideKeyboard()
                viewModel.onQueryCleared()
            },
            onSearchAction = {
                viewModel.onSearchAction()
                hideKeyboard()
            },
            onFocusChanged = { hasFocus -> viewModel.onSearchFocused(hasFocus) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            val showHistory = screenState.historyTracks != null &&
                    screenState.query.isEmpty() &&
                    screenState.searchContent is SearchContent.Idle

            when (screenState.searchContent) {
                is SearchContent.Loading -> {
                    CircularProgressIndicator(
                        color = colorAttr(MaterialR.attr.colorOnSecondary),
                        modifier = Modifier
                            .padding(top = 144.dp)
                            .align(Alignment.TopCenter)
                            .size(44.dp)
                    )
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (showHistory) {
                            SearchHistorySection(
                                tracks = screenState.historyTracks.orEmpty(),
                                onTrackClick = { track ->
                                    hideKeyboard()
                                    viewModel.onTrackClicked(track)
                                    onTrackClick(track)
                                },
                                onClearHistory = { viewModel.onClearHistory() },
                                onScrolling = { hideKeyboard() },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            SearchResultsList(
                                content = screenState.searchContent,
                                onTrackClick = { track ->
                                    hideKeyboard()
                                    viewModel.onTrackClicked(track)
                                    onTrackClick(track)
                                },
                                onRetry = { viewModel.onRetry() },
                                onScrolling = { hideKeyboard() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun SearchField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClear: () -> Unit,
    onSearchAction: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // A plain Material3 TextField enforces a much taller minimum touch target than this
    // design's compact 36dp pill, which clips the text almost entirely when forced smaller.
    // BasicTextField gives full control over that sizing instead.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .onFocusChanged { onFocusChanged(it.isFocused) },
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search_16),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(20.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp, end = 44.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color.Black,
                fontSize = 16.sp
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(colorAttr(MaterialR.attr.colorPrimary)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onSearchAction() }
            ),
            decorationBox = { innerTextField ->
                if (value.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search),
                        color = colorAttr(R.attr.search_hint_color),
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        if (value.text.isNotEmpty()) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search_clear_16),
                    contentDescription = stringResource(R.string.clear),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun SearchHistorySection(
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
    onClearHistory: () -> Unit,
    onScrolling: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) onScrolling()
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.you_were_searching),
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            items(tracks, key = { it.trackId }) { track ->
                TrackRow(track = track, onClick = { onTrackClick(track) })
            }
        }
        Button(
            onClick = onClearHistory,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 6.dp)
        ) {
            Text(stringResource(R.string.clear_history))
        }
    }
}

@Composable
private fun SearchResultsList(
    content: SearchContent,
    onTrackClick: (Track) -> Unit,
    onRetry: () -> Unit,
    onScrolling: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (content) {
        is SearchContent.Tracks -> {
            val listState = rememberLazyListState()
            LaunchedEffect(listState.isScrollInProgress) {
                if (listState.isScrollInProgress) onScrolling()
            }
            LazyColumn(
                state = listState,
                modifier = modifier,
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(content.tracks, key = { it.trackId }) { track ->
                    TrackRow(track = track, onClick = { onTrackClick(track) })
                }
            }
        }

        is SearchContent.Empty -> {
            Box(modifier = modifier) {
                Placeholder(
                    imageRes = R.drawable.placeholder_nothing_found,
                    text = stringResource(R.string.nothing_found)
                )
            }
        }

        is SearchContent.NetworkError -> {
            Box(modifier = modifier) {
                Placeholder(
                    imageRes = R.drawable.placeholder_connection_error,
                    text = stringResource(R.string.connection_problem_message),
                    actionText = stringResource(R.string.update),
                    onAction = onRetry
                )
            }
        }

        else -> Box(modifier = modifier)
    }
}

@Composable
private fun Placeholder(
    imageRes: Int,
    text: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(102.dp))
        Image(
            painter = painterResource(imageRes),
            contentDescription = null
        )
        Text(
            text = text,
            color = colorAttr(MaterialR.attr.colorOnSecondary),
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(312.dp)
                .padding(top = 16.dp)
        )
        if (actionText != null && onAction != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = 24.dp)) {
                Text(actionText)
            }
        }
    }
}
