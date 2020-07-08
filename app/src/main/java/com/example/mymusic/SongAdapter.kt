package com.example.mymusic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.databinding.ItemSongBinding
import com.example.mymusic.model.Song

class SongAdapter(private val clickListener: (Song) -> Unit) :
    ListAdapter<Song, SongAdapter.ViewHolder>(Song.diffUtils) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSongBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
        holder.itemView.setOnClickListener { clickListener(song) }
    }

    class ViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            binding.run {
                imageCover.setImageBitmap(song.coverImage)
                textTitle.text = song.title
                textArtist.text = song.artist
                textDuration.text = song.durationString
            }
        }
    }
}
