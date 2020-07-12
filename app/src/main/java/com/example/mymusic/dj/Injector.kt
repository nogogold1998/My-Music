package com.example.mymusic.dj

import android.content.Context
import com.example.mymusic.repo.AudioRepository
import com.example.mymusic.repo.AudioRepositoryImpl
import com.example.mymusic.repo.local.LocalAudioSourceImpl
import com.example.mymusic.repo.remote.FakeRemoteAudioSourceImpl

object Injector {
    fun getAudioRepository(context: Context): AudioRepository {
        val localAudioRepo = LocalAudioSourceImpl()
        val remoteAudioRepo = FakeRemoteAudioSourceImpl()
        return AudioRepositoryImpl.getInstance(context, localAudioRepo, remoteAudioRepo)
    }
}
