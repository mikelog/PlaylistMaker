package com.example.playlistmaker.ui.audioplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.playlistmaker.data.player.service.PlayerPlaybackService
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.player.PlaybackTrackInfo
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AudioPlayerRoute(
    track: Track,
    onBackClick: () -> Unit,
    onCreatePlaylistClick: () -> Unit
) {
    val viewModel: AudioPlayerViewModel = koinViewModel { parametersOf(track) }
    val context = LocalContext.current

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(track.trackId) {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val service = (binder as PlayerPlaybackService.LocalBinder).getService()
                viewModel.onServiceConnected(service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                viewModel.onServiceDisconnected()
            }
        }

        val trackInfo = PlaybackTrackInfo(
            previewUrl = track.previewUrl,
            trackName = track.trackName,
            artistName = track.artistName
        )
        val intent = Intent(context, PlayerPlaybackService::class.java).apply {
            putExtra(PlayerPlaybackService.EXTRA_TRACK_INFO, trackInfo)
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)

        onDispose {
            context.unbindService(connection)
            viewModel.onServiceDisconnected()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onScreenForegrounded()
                Lifecycle.Event.ON_STOP -> viewModel.onScreenBackgrounded()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AudioPlayerScreen(
        track = track,
        viewModel = viewModel,
        onBackClick = onBackClick,
        onCreatePlaylistClick = onCreatePlaylistClick
    )
}
