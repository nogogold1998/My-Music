package com.example.mymusic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.model.Song
import kotlinx.android.synthetic.main.item_song.view.*

class SongAdapter(private val clickListener: (Int) -> Unit) :
    ListAdapter<Song, SongAdapter.ViewHolder>(Song.diffUtils) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_song, parent, false)
        checkNotNull(view)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
        holder.itemView.setOnClickListener { clickListener(position) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(song: Song) {
            itemView.run {
                this.imageCover.setImageBitmap(song.coverImage)
                this.textTitle.text = song.title
                this.textArtist.text = song.artist
                this.textDuration.text = song.durationString
            }
        }
    }
}
