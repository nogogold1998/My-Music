package com.example.mymusic.ui.media

import com.example.mymusic.data.model.Audio

interface MediaContract {
    interface View {
        fun showAudios(audios: List<Audio>)
    }

    interface Presenter {
        fun loadAudios()
    }
}
