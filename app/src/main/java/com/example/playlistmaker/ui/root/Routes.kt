package com.example.playlistmaker.ui.root

import android.net.Uri
import com.example.playlistmaker.domain.models.Track
import com.google.gson.Gson

/**
 * Route templates for Navigation Compose. Template strings (with "{arg}" placeholders) are
 * also used to match the current destination when deciding bottom-bar visibility.
 */
object Routes {
    const val SEARCH = "search"
    const val MEDIA_LIBRARY = "mediaLibrary"
    const val SETTINGS = "settings"
    const val AUDIO_PLAYER = "audioPlayer/{trackJson}"
    const val NEW_PLAYLIST = "newPlaylist"
    const val EDIT_PLAYLIST = "editPlaylist/{playlistId}"
    const val PLAYLIST_DETAIL = "playlistDetail/{playlistId}"

    /** Destinations that hide the bottom bar, matching the original RootActivity behavior. */
    val HIDE_BOTTOM_BAR = setOf(AUDIO_PLAYER, NEW_PLAYLIST, EDIT_PLAYLIST, PLAYLIST_DETAIL)

    fun audioPlayer(track: Track): String =
        "audioPlayer/${Uri.encode(Gson().toJson(track))}"

    fun editPlaylist(playlistId: Long): String = "editPlaylist/$playlistId"

    fun playlistDetail(playlistId: Long): String = "playlistDetail/$playlistId"

    fun trackFromJson(trackJson: String?): Track =
        Gson().fromJson(Uri.decode(trackJson), Track::class.java)
}
