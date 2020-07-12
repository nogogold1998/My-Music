package com.example.mymusic.repo.local

import android.content.Context
import com.example.mymusic.repo.model.LocalAudio

interface LocalAudioSource {
    fun getAllLocalAudio(context: Context): List<LocalAudio>
}
