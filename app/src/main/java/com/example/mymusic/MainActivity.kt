package com.example.mymusic

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartService.setOnClickListener {
        }
    }
}
//val pendingIntent: PendingIntent =
//         Intent(this, ExampleActivity::class.java).let { notificationIntent ->
//             PendingIntent.getActivity(this, 0, notificationIntent, 0)
//         }
//
// val notification: Notification = Notification.Builder(this, CHANNEL_DEFAULT_IMPORTANCE)
//         .setContentTitle(getText(R.string.notification_title))
//         .setContentText(getText(R.string.notification_message))
//         .setSmallIcon(R.drawable.icon)
//         .setContentIntent(pendingIntent)
//         .setTicker(getText(R.string.ticker_text))
//         .build()
//
// startForeground(ONGOING_NOTIFICATION_ID, notification)
