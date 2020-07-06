package com.example.mymusic.model

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class Song(
    val mediaId: String,
    val title: String,
    val subtitle: String,
    val albumArtUri: Uri,
    val browsable: Boolean,
    var playbackRes: Int
){
    companion object{
        val diffUtils = object: DiffUtil.ItemCallback<Song>(){
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }
}
