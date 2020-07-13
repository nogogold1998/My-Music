package com.example.mymusic.ui.media

import com.example.mymusic.data.AudioRepository

class MediaPresenter(
    private val audioRepository: AudioRepository,
    private val mediaView: MediaContract.View
) : MediaContract.Presenter {

    override fun loadAudios() {
        val audios = audioRepository.getAllAudios()
        mediaView.showAudios(audios)
    }
}
