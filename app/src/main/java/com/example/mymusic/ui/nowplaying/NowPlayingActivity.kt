package com.example.mymusic.ui.nowplaying

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.example.mymusic.MediaService
import com.example.mymusic.R
import com.example.mymusic.databinding.ActivityNowPlayingBinding
import com.example.mymusic.receiver.MediaControlReceiver
import com.example.mymusic.repo.model.LocalAudio
import com.example.mymusic.ui.MediaBaseActivity

class NowPlayingActivity : MediaBaseActivity() {


    override fun changeNowPlaying(localAudio: LocalAudio){
        with(binding) {
            textTitle.text = localAudio.title
            textArtist.text = localAudio.artist
            textDuration.text = localAudio.durationString
            seekBar.max = localAudio.duration
            com.bumptech.glide.Glide.with(imageCover).load(localAudio.coverImage)
                .apply(
                    com.bumptech.glide.request.RequestOptions.bitmapTransform(
                        jp.wasabeef.glide.transformations.BlurTransformation(
                            25,
                            10
                        )
                    ))
                .centerCrop()
                .into(imageCover)
        }
    }
}
