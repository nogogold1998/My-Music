package com.example.mymusic.ui.nowplaying

import android.os.Bundle
import android.widget.SeekBar
import com.example.mymusic.R
import com.example.mymusic.data.model.LocalAudio
import com.example.mymusic.databinding.ActivityNowPlayingBinding
import com.example.mymusic.ui.MediaBaseActivity

class NowPlayingActivity : MediaBaseActivity() {
    private lateinit var binding: ActivityNowPlayingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNowPlayingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        with(binding){
            buttonPlayPause.setOnClickListener {
                if (isPlay)
                    mediaService?.pause()
                else
                    mediaService?.play()
            }
            buttonPrevious.setOnClickListener { mediaService?.previous() }
            buttonNext.setOnClickListener { mediaService?.next() }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mediaService?.seekTo(seekBar.progress)
                }
            })
        }
    }

    // fixme logic nhap. nhang` ca^n` xem lai. voi' ham` onSongChanged
    override fun changeNowPlaying(localAudio: LocalAudio) {
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

    override fun onTick(currentPosition: Int) {
        with(binding) {
            textCurrent.text = android.text.format.DateUtils.formatElapsedTime(currentPosition.toLong() / 1000)
            seekBar.progress = currentPosition
            // isPlay = true
        }
    }

    override fun onSongChanged(songId: Long) {
        mediaService?.nowPlaying()?.let { changeNowPlaying(it) }
    }

    override fun onPlayPause(isPlay: Boolean) {
        super.onPlayPause(isPlay)
        binding.buttonPlayPause.setImageResource(
            if (isPlay) R.drawable.ic_round_pause else R.drawable.ic_round_play
        )
    }
}
