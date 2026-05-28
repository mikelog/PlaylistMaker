package com.example.playlistmaker.ui.medialibrary.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class NewPlaylistViewModel(
    private val playlistInteractor: PlaylistInteractor,
    private val context: Context
) : ViewModel() {

    private val _coverUri = MutableLiveData<Uri?>(null)
    val coverUri: LiveData<Uri?> = _coverUri

    private val _playlistCreated = MutableLiveData<String>()
    val playlistCreated: LiveData<String> = _playlistCreated

    fun onCoverSelected(uri: Uri) {
        _coverUri.value = uri
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            val coverPath = _coverUri.value?.let { saveCoverToPrivateStorage(it) } ?: ""
            val playlist = Playlist(
                name = name,
                description = description,
                coverPath = coverPath,
                trackIds = emptyList(),
                trackCount = 0
            )
            playlistInteractor.createPlaylist(playlist)
            _playlistCreated.value = name
        }
    }

    private fun saveCoverToPrivateStorage(uri: Uri): String {
        val dir = File(context.filesDir, "playlist_covers")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "cover_${System.currentTimeMillis()}.jpg")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bitmap = BitmapFactory.decodeStream(input) ?: return ""
                FileOutputStream(file).use { output ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
                }
            }
        } catch (e: Exception) {
            return ""
        }
        return file.absolutePath
    }
}
