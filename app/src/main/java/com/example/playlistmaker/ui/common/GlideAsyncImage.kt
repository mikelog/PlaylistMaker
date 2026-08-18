package com.example.playlistmaker.ui.common

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R

/**
 * Loads [model] (a URL, local [java.io.File], or any other Glide-supported model) into an
 * ImageView via Glide, wrapped for use inside Compose. Reused across screens that display
 * track/playlist artwork.
 */
@Composable
fun GlideAsyncImage(
    model: Any?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            Glide.with(imageView.context)
                .load(model)
                .placeholder(R.drawable.placeholder_album)
                .error(R.drawable.placeholder_album)
                .centerCrop()
                .into(imageView)
        }
    )
}
