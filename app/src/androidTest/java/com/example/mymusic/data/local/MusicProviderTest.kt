package com.example.mymusic.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicProviderTest {

    lateinit var context: Context
    lateinit var localAudioSource: LocalAudioSource

    @Before
    fun setup() {
        context = getApplicationContext<Context>()
        localAudioSource = LocalAudioSourceImpl()
    }

    @Test
    fun getSongsByMediaMetadataRetriever() {
        val songsList = localAudioSource.getAllLocalAudio(context)

        assertEquals(3, songsList.size)
    }
}
