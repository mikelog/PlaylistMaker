package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.Track
import com.google.android.material.button.MaterialButton

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var albumArt: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var collectionName: TextView
    private lateinit var country: TextView
    private lateinit var releaseDate: TextView
    private lateinit var primaryGenreName: TextView
    private lateinit var trackTime: TextView
    private lateinit var btnBack: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // init views
        btnBack = findViewById(R.id.btnBack)
        albumArt = findViewById(R.id.albumArt)
        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        collectionName = findViewById(R.id.collectionName)
        country = findViewById(R.id.country)
        releaseDate = findViewById(R.id.releaseDate)
        primaryGenreName = findViewById(R.id.primaryGenreName)
        trackTime = findViewById(R.id.trackTime)

        btnBack.setOnClickListener { finish() }

        // Получаем Track
        val track = intent.getParcelableExtra<Track>(EXTRA_TRACK)
        track?.let { bindTrack(it) }
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
            .placeholder(R.drawable.track_art_placeholder)
            .error(R.drawable.track_art_placeholder)
            .centerCrop()
            .into(albumArt)
    }

    companion object {
        private const val EXTRA_TRACK = "com.example.playlistmaker.EXTRA_TRACK"

        fun start(activity: AppCompatActivity, track: Track) {
            val intent = Intent(activity, AudioPlayerActivity::class.java)
            intent.putExtra(EXTRA_TRACK, track)
            activity.startActivity(intent)
        }
    }
}
