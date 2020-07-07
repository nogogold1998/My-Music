package com.example.mymusic.model

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.text.format.DateUtils
import androidx.recyclerview.widget.DiffUtil

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int
) {
    var coverImage: Bitmap? = null

    val mediaUri: Uri
        get() = getUriFromId(id)

    val durationString: String
        get() = DateUtils.formatElapsedTime(duration.toLong() / 1000)

    companion object {
        fun getUriFromId(mediaId: Long): Uri =
            Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + mediaId)

        val diffUtils = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }
}
