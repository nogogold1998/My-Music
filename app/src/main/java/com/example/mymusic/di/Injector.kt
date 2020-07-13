package com.example.mymusic.di

import android.content.Context
import com.example.mymusic.data.AudioRepository
import com.example.mymusic.data.AudioRepositoryImpl
import com.example.mymusic.data.local.LocalAudioSourceImpl
import com.example.mymusic.data.remote.FakeRemoteAudioSourceImpl

object Injector {
    fun getAudioRepository(context: Context): AudioRepository {
        val localAudioRepo = LocalAudioSourceImpl()
        val remoteAudioRepo = FakeRemoteAudioSourceImpl()
        return AudioRepositoryImpl.getInstance(context, localAudioRepo, remoteAudioRepo)
    }
}
