package com.example.mymusic.repo.remote

import com.example.mymusic.repo.model.RemoteAudio

class FakeRemoteAudioSourceImpl : RemoteAudioSource {
    override fun getFetchRemoteAudioList(): List<RemoteAudio> {
        return emptyList()
    }
}
