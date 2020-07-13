package com.example.mymusic.data

import android.content.Context
import com.example.mymusic.data.local.LocalAudioSource
import com.example.mymusic.data.model.Audio
import com.example.mymusic.data.remote.RemoteAudioSource

class AudioRepositoryImpl private constructor(
    private val context: Context,
    private val localAudioSource: LocalAudioSource,
    private val remoteAudioSource: RemoteAudioSource
) : AudioRepository {
    override fun getAllAudios(): List<Audio> {
        @Suppress("UNUSED_VARIABLE")
        val remoteAudio = remoteAudioSource.getFetchRemoteAudioList()
        // fixme: do something after fetch remote Audio
        return localAudioSource.getAllLocalAudio(context)
    }

    companion object {
        private var instance: AudioRepositoryImpl? = null

        fun getInstance(
            context: Context,
            localAudioSource: LocalAudioSource,
            remoteAudioSource: RemoteAudioSource
        ): AudioRepository = instance ?: synchronized(this) {
            if (instance == null) {
                instance = AudioRepositoryImpl(context, localAudioSource, remoteAudioSource)
            }
            return instance!!
        }
    }
}
