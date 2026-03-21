package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var albumArt: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var collectionName: TextView
    private lateinit var country: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var trackTime: TextView
    private lateinit var textProgress: TextView
    private lateinit var btnBack: MaterialButton
    private lateinit var buttonPlay: ImageButton

    private lateinit var player: PlayerController

    companion object {
        private const val EXTRA_TRACK = "com.example.playlistmaker.EXTRA_TRACK"

        fun start(activity: AppCompatActivity, track: Track) {
            val intent = Intent(activity, AudioPlayerActivity::class.java)
            intent.putExtra(EXTRA_TRACK, track)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // Инициализация UI
        btnBack = findViewById(R.id.btnBack)
        albumArt = findViewById(R.id.albumArt)
        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        collectionName = findViewById(R.id.collectionName)
        country = findViewById(R.id.country)
        releaseDate = findViewById(R.id.releaseDate)
        primaryGenreName = findViewById(R.id.primaryGenreName)
        trackTime = findViewById(R.id.trackTime)
        textProgress = findViewById(R.id.textProgress)
        buttonPlay = findViewById(R.id.buttonPlay)

        btnBack.setOnClickListener { finish() }

        // Инициализация PlayerController
        player = MediaPlayerController()
        player.setOnStateChangeListener { state ->
            when (state) {
                PlayerController.State.PLAYING -> buttonPlay.setImageResource(R.drawable.ic_pause_100)
                else -> buttonPlay.setImageResource(R.drawable.ic_play_100)
            }
            buttonPlay.isEnabled = state != PlayerController.State.DEFAULT
            buttonPlay.alpha = if (buttonPlay.isEnabled) 1f else 0.5f
        }
        player.setOnProgressUpdateListener { ms ->
            textProgress.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(ms)
        }

        // Получаем Track из intent
        val track = intent.getParcelableExtra(EXTRA_TRACK, Track::class.java)
        track?.let {
            bindTrack(it)
            player.prepare(it.previewUrl)
        }

        buttonPlay.setOnClickListener {
            when (player.state) {
                PlayerController.State.PLAYING -> player.pause()
                PlayerController.State.PREPARED, PlayerController.State.PAUSED -> player.play()
                else -> {}
            }
        }
    }

    private fun bindTrack(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        collectionName.text = track.collectionName ?: ""
        releaseDate.text = track.getReleaseYear()
        primaryGenreName.text = track.primaryGenreName ?: ""
        country.text = track.country ?: ""
        trackTime.text = track.trackTime

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder_album)
            .error(R.drawable.placeholder_album)
            .centerCrop()
            .into(albumArt)
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
