package com.noirmusic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.noirmusic.R
import com.noirmusic.model.Song

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song, Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.songName)
        val artistName: TextView = itemView.findViewById(R.id.artistName)
        val duration: TextView = itemView.findViewById(R.id.duration)
        val songIcon: ImageView = itemView.findViewById(R.id.songIcon)
        val moreButton: ImageButton = itemView.findViewById(R.id.moreButton)

        fun bind(song: Song, position: Int) {
            songName.text = song.title
            artistName.text = song.artist
            duration.text = song.getFormattedDuration()

            itemView.setOnClickListener {
                onSongClick(song, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position], position)
    }

    override fun getItemCount(): Int = songs.size
}
