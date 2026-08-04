package com.example.playlistmaker.data.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.player.MediaPlayerRepository
import com.example.playlistmaker.domain.player.PlayerServiceInterface
import com.example.playlistmaker.domain.player.PlayerState
import com.example.playlistmaker.ui.root.RootActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlayerPlaybackService : Service(), PlayerServiceInterface, KoinComponent {

    private val mediaPlayerRepository: MediaPlayerRepository by inject()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())

    private var trackUrl: String = ""
    private var trackName: String = ""
    private var artistName: String = ""

    inner class LocalBinder : Binder() {
        fun getService(): PlayerPlaybackService = this@PlayerPlaybackService
    }

    private val binder = LocalBinder()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        trackUrl = intent?.getStringExtra(EXTRA_TRACK_URL).orEmpty()
        trackName = intent?.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        artistName = intent?.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        preparePlayer()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopPlaybackCompletely()
        return false
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopPlaybackCompletely()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun stopPlaybackCompletely() {
        progressJob?.cancel()
        mediaPlayerRepository.release()
        stopForegroundNotification()
        stopSelf()
    }

    private fun preparePlayer() {
        if (trackUrl.isBlank()) {
            _playerState.value = PlayerState(isPlayEnabled = false)
            return
        }
        mediaPlayerRepository.prepare(
            url = trackUrl,
            onPrepared = {
                _playerState.value = _playerState.value.copy(isPlayEnabled = true)
            },
            onCompletion = {
                progressJob?.cancel()
                _playerState.value = PlayerState(isPlaying = false, isPlayEnabled = true, progressMs = 0)
                stopForegroundNotification()
            }
        )
    }

    override fun getPlayerState(): StateFlow<PlayerState> = _playerState.asStateFlow()

    override fun play() {
        mediaPlayerRepository.play()
        _playerState.value = _playerState.value.copy(isPlaying = true)
        startProgressUpdates()
    }

    override fun pause() {
        mediaPlayerRepository.pause()
        progressJob?.cancel()
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = serviceScope.launch {
            while (isActive) {
                _playerState.value = _playerState.value.copy(
                    progressMs = mediaPlayerRepository.getCurrentPosition()
                )
                delay(PROGRESS_UPDATE_DELAY_MS)
            }
        }
    }

    override fun startForegroundNotification() {
        if (!_playerState.value.isPlaying) return
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    override fun stopForegroundNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, RootActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play_100)
            .setContentTitle(getString(R.string.player_notification_title))
            .setContentText(getString(R.string.player_notification_text, artistName, trackName))
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.player_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_TRACK_URL = "extra_track_url"
        const val EXTRA_TRACK_NAME = "extra_track_name"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"
        private const val CHANNEL_ID = "player_playback_channel"
        private const val NOTIFICATION_ID = 100
        private const val PROGRESS_UPDATE_DELAY_MS = 300L
    }
}
