package com.example.mymusic.data.remote

import com.example.mymusic.data.model.RemoteAudio

interface RemoteAudioSource {
    fun getFetchRemoteAudioList(): List<RemoteAudio>
}
