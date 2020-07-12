package com.example.mymusic.repo.local

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.example.mymusic.repo.model.LocalAudio

class LocalAudioSourceImpl: LocalAudioSource {
    override fun getAllLocalAudio(context: Context): List<LocalAudio> {
        val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val result = mutableListOf<LocalAudio>()
        context.contentResolver?.query(
            mediaUri,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val retriever = MediaMetadataRetriever()
                do {
                    val mediaId = cursor.getLong(0)
                    retriever.setDataSource(context, LocalAudio.getUriFromId(mediaId))
                    val localAudio = extractLocalAudio(retriever, mediaId)
                    result.add(localAudio)
                } while (cursor.moveToNext())
            }
        }
        return result
    }

    private fun extractLocalAudio(
        retriever: MediaMetadataRetriever,
        mediaId: Long
    ): LocalAudio {
        val art = retriever.embeddedPicture
        return LocalAudio(
            mediaId,
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                .toInt()
        ).apply { coverImage = BitmapFactory.decodeByteArray(art, 0, art.size) }
    }
}
