package com.example.mymusic.data.remote

import com.example.mymusic.data.model.RemoteAudio

class FakeRemoteAudioSourceImpl : RemoteAudioSource {
    override fun getFetchRemoteAudioList(): List<RemoteAudio> {
        return emptyList()
    }
}
