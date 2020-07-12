package com.example.mymusic.ui

import com.example.mymusic.repo.model.Audio

interface MediaContract {
    interface View {
        fun showAudios(audios: List<Audio>)
    }

    interface Presenter {
        fun loadAudios()
    }
}
