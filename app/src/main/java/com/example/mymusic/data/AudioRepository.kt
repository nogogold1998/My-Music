package com.example.mymusic.data

import com.example.mymusic.data.model.Audio

interface AudioRepository {
    fun getAllAudios(): List<Audio>
}
