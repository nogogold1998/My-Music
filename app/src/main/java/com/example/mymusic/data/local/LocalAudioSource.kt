package com.example.mymusic.data.local

import android.content.Context
import com.example.mymusic.data.model.LocalAudio

interface LocalAudioSource {
    fun getAllLocalAudio(context: Context): List<LocalAudio>
}
