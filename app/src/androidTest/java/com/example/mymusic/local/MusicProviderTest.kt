package com.example.mymusic.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicProviderTest {

    lateinit var context: Context

    @Before
    fun setup() {
        context = getApplicationContext<Context>()
    }

    @Test
    fun getSongsByMediaMetadataRetriever() {
        val songsList = SongProvider.getSongsByMediaMetadataRetriever(context)
        songsList.forEach {
            println(it.toString())
        }
        assertEquals(3, songsList.size)
    }
    @Test
    fun getSongsByContentProvider() {
        val songsList = SongProvider.getSongsByContentResolver(context)
        songsList.forEach {
            println(it.toString())
        }
        assertEquals(3, songsList.size)
    }
}
