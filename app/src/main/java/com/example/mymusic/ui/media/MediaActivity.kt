package com.example.mymusic.ui.media

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mymusic.R
import com.example.mymusic.data.model.Audio
import com.example.mymusic.data.model.LocalAudio
import com.example.mymusic.databinding.ActivityMediaPlayerBinding
import com.example.mymusic.di.Injector
import com.example.mymusic.ui.MediaBaseActivity
import com.example.mymusic.ui.nowplaying.NowPlayingActivity
import jp.wasabeef.glide.transformations.BlurTransformation

class MediaActivity : MediaBaseActivity(),
    MediaContract.View {
    private lateinit var localAudioAdapter: LocalAudioAdapter
    private lateinit var binding: ActivityMediaPlayerBinding
    private lateinit var presenter: MediaPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = MediaPresenter(
            Injector.getAudioRepository(this), this
        )

        localAudioAdapter = LocalAudioAdapter {
            mediaService?.loadPlaylist(localAudioAdapter.currentList)
            mediaService?.playSongWithId(it.id)
        }

        initView()
    }

    private fun initView() = with(binding) {
        recyclerView.run {
            adapter = localAudioAdapter
            layoutManager = LinearLayoutManager(
                this@MediaActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        }

        with(bottomContainer) {
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
            playbackBar.setOnClickListener {
                startActivity(Intent(this@MediaActivity, NowPlayingActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (allPermissionGranted()) {
            presenter.loadAudios()
        } else {
            requestPermissions(
                REQUEST_PERMISSIONS,
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            presenter.loadAudios()
        }
    }

    override fun showAudios(audios: List<Audio>) {
        val localAudios = audios.filterIsInstance<LocalAudio>()
        localAudioAdapter.submitList(localAudios)
    }

    override fun onTick(currentPosition: Int) {
        with(binding.bottomContainer) {
            textCurrent.text = DateUtils.formatElapsedTime(currentPosition.toLong() / 1000)
            seekBar.progress = currentPosition
            // isPlay = true
        }
    }

    override fun onSongChanged(songId: Long) {
        localAudioAdapter.currentList.find { it.id == songId }?.let { song ->
            changeNowPlaying(song)
        }
    }

    override fun changeNowPlaying(localAudio: LocalAudio) {
        with(binding.bottomContainer) {
            playbackBar.visibility = View.VISIBLE
            textTitle.text = localAudio.title
            textArtist.text = localAudio.artist
            textDuration.text = localAudio.durationString
            seekBar.max = localAudio.duration
            Glide.with(imageCover).load(localAudio.coverImage)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 10)))
                .centerCrop()
                .into(imageCover)
        }
    }

    override fun onPlayPause(isPlay: Boolean) {
        super.onPlayPause(isPlay)
        binding.bottomContainer.buttonPlayPause.setImageResource(
            if (isPlay) R.drawable.ic_round_pause else R.drawable.ic_round_play
        )
    }

    private fun allPermissionGranted() =
        REQUEST_PERMISSIONS.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    companion object {
        private const val TAG = "MediaActivity"
        private val REQUEST_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val REQUEST_CODE = 11
    }
}

