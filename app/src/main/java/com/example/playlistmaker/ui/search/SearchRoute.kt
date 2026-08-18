package com.example.playlistmaker.ui.search

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.ui.root.Routes
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchRoute(navController: NavController) {
    val viewModel: SearchViewModel = koinViewModel()
    SearchScreen(
        viewModel = viewModel,
        onTrackClick = { track -> navController.navigate(Routes.audioPlayer(track)) }
    )
}
