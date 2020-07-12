package com.example.mymusic.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.text.format.DateUtils
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mymusic.MediaManager
import com.example.mymusic.MediaService
import com.example.mymusic.R
import com.example.mymusic.databinding.ActivityMediaPlayerBinding
import com.example.mymusic.dj.Injector
import com.example.mymusic.repo.model.Audio
import com.example.mymusic.repo.model.LocalAudio
import jp.wasabeef.glide.transformations.BlurTransformation

class MediaActivity : AppCompatActivity(), MediaManager.Listener, MediaContract.View {
    private lateinit var localAudioAdapter: LocalAudioAdapter
    private lateinit var binding: ActivityMediaPlayerBinding
    private lateinit var presenter: MediaPresenter

    private var receiver: MediaControlReceiver? = null
    private var mediaService: MediaService? = null

    private var mediaServiceBound: Boolean = false
    private var isPlay: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as? MediaService.MediaBinder
            mediaService = binder?.getMediaService()
            mediaService?.nowPlaying()?.let {
                changeNowPlaying(it)
                isPlay = true
            }
            mediaServiceBound = mediaService != null
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mediaService = null
            mediaServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter = MediaPresenter(Injector.getAudioRepository(this), this)

        localAudioAdapter = LocalAudioAdapter {
            mediaService?.loadPlaylist(localAudioAdapter.currentList)
            mediaService?.playSongWithId(it.id)
        }

        receiver = MediaControlReceiver(this)

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

        with(bottomPlayback) {
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

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MediaService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        val filter = IntentFilter().apply {
            addAction(MediaService.ACTION_ON_TICK)
            addAction(MediaService.ACTION_PLAYBACK_PLAY)
            addAction(MediaService.ACTION_PLAYBACK_PAUSE)
            addAction(MediaService.ACTION_SONG_CHANGE)
        }
        registerReceiver(receiver, filter)

        if (allPermissionGranted()) {
            presenter.loadAudios()
        } else {
            requestPermissions(
                REQUEST_PERMISSIONS,
                REQUEST_CODE
            )
        }
    }

    override fun onStop() {
        super.onStop()
        if(isPlay){
            startService(Intent(this, MediaService::class.java))
        }
        if (mediaServiceBound) {
            // ContextCompat.startForegroundService(this, Intent(this, MediaService::class.java))
            unbindService(serviceConnection)
            mediaServiceBound = false
        }
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        receiver = null
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
        with(binding.bottomPlayback) {
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

    private fun changeNowPlaying(song: LocalAudio) {
        with(binding.bottomPlayback) {
            textTitle.text = song.title
            textArtist.text = song.artist
            textDuration.text = song.durationString
            seekBar.max = song.duration
            Glide.with(imageCover).load(song.coverImage)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 10)))
                .centerCrop()
                .into(imageCover)
        }
    }

    override fun onPlayPause(isPlay: Boolean) {
        binding.bottomPlayback.buttonPlayPause.setImageResource(
            if (isPlay) R.drawable.ic_round_pause else R.drawable.ic_round_play
        )
        this@MediaActivity.isPlay = isPlay
    }

    private fun allPermissionGranted() =
        REQUEST_PERMISSIONS.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    companion object {
        private const val TAG = "MediaActivity"
        private val REQUEST_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val REQUEST_CODE = 11
    }
}

private class MediaControlReceiver(private val listener: MediaManager.Listener) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            when (intent.action) {
                MediaService.ACTION_SONG_CHANGE ->
                    intent.getLongExtra(MediaService.EXTRA_SONG_ID, -1)
                        .takeIf { it != -1L }
                        ?.let { id -> listener.onSongChanged(id) }

                MediaService.ACTION_PLAYBACK_PAUSE -> listener.onPlayPause(false)
                MediaService.ACTION_PLAYBACK_PLAY -> listener.onPlayPause(true)
                MediaService.ACTION_ON_TICK -> {
                    val position =
                        intent.getIntExtra(MediaService.EXTRA_SONG_CURRENT_POSITION, 0)
                    listener.onTick(position)
                }
                else -> Log.d("HelperReceiver", "onReceive: unknown action")
            }
        }
    }
}
