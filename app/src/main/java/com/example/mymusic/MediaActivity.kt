package com.example.mymusic

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusic.local.SongProvider
import com.example.mymusic.model.Song
import kotlinx.android.synthetic.main.activity_media_player.*

class MediaActivity : AppCompatActivity() {
    private val songsList: List<Song> by lazy { SongProvider.getSongsByMediaMetadataRetriever(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media_player)

        recyclerView?.run {
            adapter = SongAdapter {
                Toast.makeText(this@MediaActivity, it.title, Toast.LENGTH_SHORT).show()
            }.apply { submitList(songsList) }
            layoutManager =
                LinearLayoutManager(
                    this@MediaActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
        }
    }
}
