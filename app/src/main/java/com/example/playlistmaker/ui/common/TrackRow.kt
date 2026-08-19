package com.example.playlistmaker.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playlistmaker.domain.models.Track
import com.google.android.material.R as MaterialR
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.theme.colorAttr

/** A single track row (artwork, name, artist, time). Reused by Search and Favourite Tracks. */
@Composable
fun TrackRow(track: Track, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlideAsyncImage(
            model = track.artworkUrl100,
            modifier = Modifier.size(46.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = track.trackName,
                color = colorAttr(MaterialR.attr.colorOnSecondary),
                fontSize = 16.sp,
                maxLines = 1
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = track.artistName,
                    color = colorAttr(R.attr.yp_gray_color),
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = track.trackTime,
                    color = colorAttr(R.attr.yp_gray_color),
                    fontSize = 11.sp
                )
            }
        }
    }
}
