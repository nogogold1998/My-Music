package com.example.mymusic.repo.remote

import com.example.mymusic.repo.model.RemoteAudio

interface RemoteAudioSource {
    fun getFetchRemoteAudioList(): List<RemoteAudio>
}
