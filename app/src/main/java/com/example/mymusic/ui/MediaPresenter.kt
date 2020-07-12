package com.example.mymusic.ui

import com.example.mymusic.repo.AudioRepository

class MediaPresenter(
    private val audioRepository: AudioRepository,
    private val mediaView: MediaContract.View
) : MediaContract.Presenter {

    override fun loadAudios() {
        val audios = audioRepository.getAllAudios()
        mediaView.showAudios(audios)
    }
}
