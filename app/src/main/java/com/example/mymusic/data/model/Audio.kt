package com.example.mymusic.data.model

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.text.format.DateUtils
import androidx.recyclerview.widget.DiffUtil

sealed class Audio {
    companion object {
        val diffUtils = object : DiffUtil.ItemCallback<Audio>() {
            override fun areItemsTheSame(oldItem: Audio, newItem: Audio): Boolean {
                return when {
                    oldItem is RemoteAudio && newItem is RemoteAudio -> oldItem.mediaId == newItem.mediaId
                    oldItem is LocalAudio && newItem is LocalAudio -> oldItem.id == newItem.id
                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: Audio, newItem: Audio): Boolean {
                return oldItem == newItem
            }
        }
    }
}

data class RemoteAudio(
    val mediaId: String,
    val title: String,
    val subtitle: String,
    val albumArtUri: Uri,
    val browsable: Boolean,
    var playbackRes: Int
) : Audio()

data class LocalAudio(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Int
) : Audio() {
    var coverImage: Bitmap? = null

    val mediaUri: Uri
        get() = getUriFromId(id)

    val durationString: String
        get() = DateUtils.formatElapsedTime(duration.toLong() / 1000)

    companion object {
        fun getUriFromId(mediaId: Long): Uri =
            Uri.parse(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString() + "/" + mediaId)

        val diffUtils = object : DiffUtil.ItemCallback<LocalAudio>() {
            override fun areItemsTheSame(oldItem: LocalAudio, newItem: LocalAudio): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: LocalAudio, newItem: LocalAudio): Boolean {
                return oldItem == newItem
            }
        }
    }
}
