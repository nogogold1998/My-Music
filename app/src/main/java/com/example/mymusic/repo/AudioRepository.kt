package com.example.mymusic.repo

import com.example.mymusic.repo.model.Audio

interface AudioRepository {
    fun getAllAudios(): List<Audio>
}
