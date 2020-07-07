package com.example.mymusic.local

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import com.example.mymusic.model.Song

object SongProvider {
    fun getSongsByMediaMetadataRetriever(context: Context): List<Song> {
        val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val result = mutableListOf<Song>()
        context.contentResolver?.query(
            mediaUri,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.TITLE
        ).use { cursor ->
            val retriever = MediaMetadataRetriever()
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                do {
                    val mediaId = cursor.getLong(0)
                    retriever.setDataSource(context, Song.getUriFromId(mediaId))
                    val art = retriever.embeddedPicture
                    val song = Song(
                        mediaId,
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            .toInt()
                    ).apply { coverImage = BitmapFactory.decodeByteArray(art, 0, art.size) }
                    result.add(song)
                } while (cursor.moveToNext())
            }
        }
        return result
    }

    @Deprecated("cannot get bitmap directly", ReplaceWith("Glide library"))
    fun getSongsByContentResolver(context: Context): List<Song> {
        val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                add(MediaStore.Audio.Media.DURATION)
        }.toTypedArray()

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val result = mutableListOf<Song>()
        context.contentResolver?.query(
            mediaUri,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.TITLE
        ).use { cursor ->
            if (cursor != null && cursor.count > 0 && cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                    val title = cursor.getString(cursor.getColumnIndex(projection[1]))
                    val artist = cursor.getString(cursor.getColumnIndex(projection[2]))
                    val album = cursor.getString(cursor.getColumnIndex(projection[3]))
                    val duration = projection.getOrNull(5)?.let {
                        cursor.getInt(cursor.getColumnIndex(it))
                    } ?: 0
                    result.add(Song(id, title, artist, album, duration))
                } while (cursor.moveToNext())
            }
        }
        return result
    }
}
