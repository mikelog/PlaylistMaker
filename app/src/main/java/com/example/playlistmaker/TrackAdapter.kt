package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private val tracks: MutableList<Track>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onTrackClick: ((Track) -> Unit)? = null
    var onRetryClick: (() -> Unit)? = null

    enum class State {
        IDLE,
        DATA,
        EMPTY,
        NO_CONNECTION
    }

    private var state: State = State.DATA

    override fun getItemViewType(position: Int): Int {
        return when (state) {
            State.DATA -> VIEW_TYPE_TRACK
            State.EMPTY -> VIEW_TYPE_EMPTY
            State.NO_CONNECTION -> VIEW_TYPE_NO_CONNECTION
            State.IDLE -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRACK -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_track, parent, false)
                TrackViewHolder(view)
            }
            VIEW_TYPE_EMPTY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_empty, parent, false)
                PlaceholderViewHolder(view)
            }
            VIEW_TYPE_NO_CONNECTION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_no_connection, parent, false)
                PlaceholderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrackViewHolder && state == State.DATA) {
            val track = tracks[position]
            holder.bind(track)

            holder.itemView.setOnClickListener {
                onTrackClick?.invoke(track)
            }

        } else if (holder is PlaceholderViewHolder && state == State.NO_CONNECTION) {
            holder.itemView.findViewById<Button>(R.id.buttonRetry)
                ?.setOnClickListener { onRetryClick?.invoke() }
        }
    }

    override fun getItemCount(): Int {
        return when (state) {
            State.DATA -> tracks.size
            State.EMPTY, State.NO_CONNECTION -> 1
            State.IDLE -> 0
        }
    }

    fun updateData(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        state = if (tracks.isEmpty()) State.EMPTY else State.DATA
        notifyDataSetChanged()
    }

    fun showNoConnection() {
        tracks.clear()
        state = State.NO_CONNECTION
        notifyDataSetChanged()
    }

    fun clearData() {
        tracks.clear()
        state = State.IDLE
        notifyDataSetChanged()
    }

    class PlaceholderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val VIEW_TYPE_TRACK = 1
        private const val VIEW_TYPE_EMPTY = 0
        private const val VIEW_TYPE_NO_CONNECTION = 2
    }
}
