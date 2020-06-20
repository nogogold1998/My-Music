package com.example.mymusic

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import androidx.media2.player.MediaPlayer

class MediaPlaybackService : MediaBrowserServiceCompat() {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: MediaPlayer
    private lateinit var stateBuilder: PlaybackStateCompat.Builder

    override fun onCreate() {
        super.onCreate()

        // create a MediaSessionCompat
        mediaSession = MediaSessionCompat(this, TAG).apply {
            // enable callback from MediaButtons and TransportControls
            // setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
            //     or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            // )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(MySessionCallback())

            // set the session's token so that client activities can communicate with it
            setSessionToken(sessionToken)
        }

        // ****************************
        player = MediaPlayer(this)
        val mediaMetaData = MediaMetadataCompat.Builder().build()
        val state = PlaybackStateCompat.Builder()
        val callback = object : MediaSessionCompat.Callback() {}
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        // browsing not allowed
        if (MY_EMPTY_MEDIA_ROOT_ID == parentId) {
            result.sendResult(null)
            return
        }

        // assume for example that the music catalog is already loaded/cached

        val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()

        // check if this is the root menu
        if (MY_MEDIA_ROOT_ID == parentId) {
            // build the MediaItem objects for the top level,
            // and put the in the mediaItems list...
        } else {
            // examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // (optional) control the level of access for the specified package name
        // you'll need to write your own logic to do this.
        return if (allowBrowsing(clientPackageName, clientUid)) {
            // return a root id that clients can use with onLoadChildren() to retrieve
            // the content hierarchy
            BrowserRoot(MY_MEDIA_ROOT_ID, null)
        } else {
            // clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. this disables the ability to browse for content
            BrowserRoot(MY_EMPTY_MEDIA_ROOT_ID, null)
        }
    }

    fun buildNotification() {
        // get the session's metadata
        val controller = mediaSession.controller
        val mediaMetaData = controller.metadata
        val description = mediaMetaData.description

        val builder = NotificationCompat.Builder(baseContext, MEDIA_CHANNEL_ID).apply {
            // add the metadata for the currently playing track
            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)

            // enable launching the player by clicking the notification
            setContentIntent(controller.sessionActivity)

            // stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    baseContext,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // add an app icon and set its accent color
            setSmallIcon(R.drawable.ic_launcher_foreground)
            color = ContextCompat.getColor(baseContext, R.color.colorPrimaryDark)

            // add a pause button
            addAction(NotificationCompat.Action(R.drawable))
        }
    }

    companion object {
        private const val TAG = "MediaPlaybackService"
        const val MEDIA_CHANNEL_ID = "mediaChannel"
    }
}

class MySessionCallback : MediaSessionCompat.Callback() {
    override fun onPlay() {
        super.onPlay()
        TODO("Build and display the notification when the player starts playing")
        // val controller =
    }
}
