package com.example.mymusic

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusic.local.SongProvider
import com.example.mymusic.model.Song
import kotlinx.android.synthetic.main.activity_media_player.*

class MediaActivity : AppCompatActivity() {
    private val songs = MutableLiveData<List<Song>>().apply { value = emptyList() }
    private lateinit var songAdapter: SongAdapter

    private var mediaService: MediaService? = null
    private var mediaServiceBound: Boolean = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as? MediaService.MediaBinder
            mediaService = binder?.getMediaService()
            mediaServiceBound = mediaService != null
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mediaService = null
            mediaServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        if (!allPermissionGranted()) {
            songs.value = (SongProvider.getSongsByMediaMetadataRetriever(this))
        } else {
            requestPermissions(REQUEST_PERMISSIONS, REQUEST_CODE)
        }

        songAdapter = SongAdapter {
            mediaService?.playAtPosition(it)
        }
        recyclerView?.run {
            adapter = songAdapter
            layoutManager = LinearLayoutManager(
                this@MediaActivity,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
        songs.observe(this, Observer {
            if (it != null) {
                mediaService?.loadMedia(it)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MediaService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(serviceConnection)
        mediaServiceBound = false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            songs.value = SongProvider.getSongsByMediaMetadataRetriever(this)
        }
    }

    private fun allPermissionGranted() =
        REQUEST_PERMISSIONS.all { checkCallingPermission(it) == PackageManager.PERMISSION_GRANTED }

    companion object {
        private val REQUEST_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        private const val REQUEST_CODE = 11
    }
}
