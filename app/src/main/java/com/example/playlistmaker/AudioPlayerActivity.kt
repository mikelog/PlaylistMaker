package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class AudioPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // Получаем все данные из Intent
        val track = Track(
            trackId = intent.getLongExtra("trackId", 0L),
            trackName = intent.getStringExtra("trackName") ?: "",
            artistName = intent.getStringExtra("artistName") ?: "",
            trackTime = intent.getStringExtra("trackTime") ?: "00:00",
            artworkUrl100 = intent.getStringExtra("artworkUrl100") ?: "",
            collectionName = intent.getStringExtra("collectionName") ?: "",
            releaseDate = intent.getStringExtra("releaseDate") ?: "",
            primaryGenreName = intent.getStringExtra("primaryGenreName") ?: "",
            country = intent.getStringExtra("country") ?: ""
        )

        // Находим элементы UI
        val btnBack: Button = findViewById(R.id.btnBack)
        val albumArt: ImageView = findViewById(R.id.albumArt)
        val trackName: TextView = findViewById(R.id.trackName)
        val artistName: TextView = findViewById(R.id.artistName)
        val collectionName: TextView = findViewById(R.id.collectionName)
        val country: TextView = findViewById(R.id.country)
        val releaseDate: TextView = findViewById(R.id.releaseDate)
        val primaryGenreName: TextView = findViewById(R.id.primaryGenreName)
        val trackTime: TextView = findViewById(R.id.trackTime)

        // Кнопка назад
        btnBack.setOnClickListener { onBackPressed() }
        trackName.text = track.trackName
        artistName.text = track.artistName
        collectionName.text = track.collectionName
        releaseDate.text = track.getReleaseYear()
        primaryGenreName.text = track.primaryGenreName
        country.text = track.country
        trackTime.text = track.trackTime
        // Загружаем обложку
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.track_art_placeholder)
            .error(R.drawable.track_art_placeholder)
            .centerCrop()
            .into(albumArt)
    }

    companion object {
        fun start(activity: AppCompatActivity, track: Track) {
            val intent = Intent(activity, AudioPlayerActivity::class.java)
            intent.putExtra("trackId", track.trackId)
            intent.putExtra("trackName", track.trackName)
            intent.putExtra("artistName", track.artistName)
            intent.putExtra("trackTime", track.trackTime)
            intent.putExtra("artworkUrl100", track.artworkUrl100)
            intent.putExtra("collectionName", track.collectionName)
            intent.putExtra("releaseDate", track.releaseDate)
            intent.putExtra("primaryGenreName", track.primaryGenreName)
            intent.putExtra("country", track.country)
            activity.startActivity(intent)
        }
    }
}
