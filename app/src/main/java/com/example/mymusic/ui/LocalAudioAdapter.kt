package com.example.mymusic.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.databinding.ItemSongBinding
import com.example.mymusic.repo.model.LocalAudio

class LocalAudioAdapter(private val clickListener: (LocalAudio) -> Unit) :
    ListAdapter<LocalAudio, LocalAudioAdapter.ViewHolder>(LocalAudio.diffUtils) {

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
        fun bind(localAudio: LocalAudio) {
            binding.run {
                imageCover.setImageBitmap(localAudio.coverImage)
                textTitle.text = localAudio.title
                textArtist.text = localAudio.artist
                textDuration.text = localAudio.durationString
            }
        }
    }
}
