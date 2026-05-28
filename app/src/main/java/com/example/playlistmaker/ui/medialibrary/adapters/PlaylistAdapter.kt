package com.example.playlistmaker.ui.medialibrary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ItemPlaylistGridBinding

import com.example.playlistmaker.domain.models.Playlist
import java.io.File

class PlaylistAdapter(
    private val onItemClick: (Playlist) -> Unit = {}
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    private var playlists: List<Playlist> = emptyList()

    fun submitList(list: List<Playlist>) {
        playlists = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(private val binding: ItemPlaylistGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            binding.trackCount.text = binding.root.context.resources.getQuantityString(
                R.plurals.track_count, playlist.trackCount, playlist.trackCount
            )
            val coverFile = if (playlist.coverPath.isNotEmpty()) File(playlist.coverPath) else null
            Glide.with(binding.root)
                .load(coverFile)
                .placeholder(R.drawable.placeholder_album)
                .error(R.drawable.placeholder_album)
                .centerCrop()
                .into(binding.coverImage)

            binding.root.setOnClickListener { onItemClick(playlist) }
        }
    }
}
