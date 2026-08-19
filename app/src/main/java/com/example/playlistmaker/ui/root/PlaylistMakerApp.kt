package com.example.playlistmaker.ui.root

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.audioplayer.AudioPlayerRoute
import com.example.playlistmaker.ui.medialibrary.EditPlaylistRoute
import com.example.playlistmaker.ui.medialibrary.MediaLibraryRoute
import com.example.playlistmaker.ui.medialibrary.NewPlaylistRoute
import com.example.playlistmaker.ui.medialibrary.PlaylistDetailRoute
import com.example.playlistmaker.ui.search.SearchRoute
import com.example.playlistmaker.ui.settings.SettingsRoute
import com.example.playlistmaker.ui.theme.PlaylistMakerTheme
import com.example.playlistmaker.ui.theme.colorAttr
import com.google.android.material.R as MaterialR

private data class BottomBarItem(
    val route: String,
    val labelRes: Int,
    val iconRes: Int
)

private val BOTTOM_BAR_ITEMS = listOf(
    BottomBarItem(Routes.SEARCH, R.string.search, R.drawable.ic_search_24),
    BottomBarItem(Routes.MEDIA_LIBRARY, R.string.library, R.drawable.ic_media_24),
    BottomBarItem(Routes.SETTINGS, R.string.settings, R.drawable.ic_settings_24)
)

@Composable
fun PlaylistMakerApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute !in Routes.HIDE_BOTTOM_BAR

    PlaylistMakerTheme {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    PlaylistMakerBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Routes.MEDIA_LIBRARY,
                modifier = Modifier.padding(bottom = if (showBottomBar) paddingValues.calculateBottomPadding() else 0.dp)
            ) {
                composable(Routes.SEARCH) {
                    SearchRoute(navController)
                }
                composable(Routes.MEDIA_LIBRARY) { backStackEntry ->
                    MediaLibraryRoute(
                        savedStateHandle = backStackEntry.savedStateHandle,
                        navController = navController
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsRoute()
                }
                composable(
                    route = Routes.AUDIO_PLAYER,
                    arguments = listOf(navArgument("trackJson") { type = NavType.StringType })
                ) { backStackEntry ->
                    val track = Routes.trackFromJson(backStackEntry.arguments?.getString("trackJson"))
                    AudioPlayerRoute(
                        track = track,
                        onBackClick = { navController.popBackStack() },
                        onCreatePlaylistClick = { navController.navigate(Routes.NEW_PLAYLIST) }
                    )
                }
                composable(Routes.NEW_PLAYLIST) {
                    NewPlaylistRoute(navController)
                }
                composable(
                    route = Routes.EDIT_PLAYLIST,
                    arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    EditPlaylistRoute(playlistId, navController)
                }
                composable(
                    route = Routes.PLAYLIST_DETAIL,
                    arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    PlaylistDetailRoute(playlistId, navController)
                }
            }
        }
    }
}

@Composable
private fun PlaylistMakerBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val backgroundColor = colorAttr(MaterialR.attr.colorOnPrimary)
    val unselectedColor = colorAttr(MaterialR.attr.colorOnSecondary)
    val selectedColor = Color(0xFF2F6CFF)

    BottomAppBar(containerColor = backgroundColor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BOTTOM_BAR_ITEMS.forEach { item ->
                val selected = currentRoute == item.route
                val tint = if (selected) selectedColor else unselectedColor
                Column(
                    modifier = Modifier
                        .clickable(enabled = !selected) { onNavigate(item.route) }
                        .padding(vertical = 4.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                        tint = tint
                    )
                    Text(
                        text = stringResource(item.labelRes),
                        color = tint,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
