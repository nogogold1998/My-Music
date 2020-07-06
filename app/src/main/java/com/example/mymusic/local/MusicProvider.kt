package com.example.mymusic.local

import android.provider.MediaStore

val mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

val projection = arrayOf(
    MediaStore.Audio.Media.TITLE,
    MediaStore.Audio.Media._ID,
    MediaStore.Audio.Media.ALBUM,
    MediaStore.Audio.Media.ALBUM_ID,
    MediaStore.Audio.Media.ARTIST,
    MediaStore.Audio.Media.ARTIST_ID,
    MediaStore.Audio.Media.DURATION
)
val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

// val cursor =
//     contentResolver?.query(
//         uri,
//         projection,
//         selection,
//         null,
//         MediaStore.Audio.Media.TITLE
//     )
