package com.msam.ringkesin.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.msam.ringkesin.RingkesinApp
import com.msam.ringkesin.ui.localization.S

class RecordingService : Service() {

    private lateinit var manager: RecordingManager
    private var showNotification: Boolean = true

    private fun uiLang(): String =
        getSharedPreferences("ringkesin_settings", MODE_PRIVATE)
            .getString("ui_lang", "en") ?: "en"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        manager = (applicationContext as RingkesinApp).recordingManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showNotification = intent?.getBooleanExtra("show_notification", true) ?: true
        if (showNotification) {
            val notification = createNotification(S.recordingEllipsis(uiLang()))
            startForeground(NOTIFICATION_ID, notification)
        } else {
            // Android tetap butuh FG service — pakai silent channel biar gak ganggu
            val silentNotif = createSilentNotification()
            startForeground(NOTIFICATION_ID, silentNotif)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        manager.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    fun updateNotificationText(text: String) {
        if (!showNotification) return
        val notification = createNotification(text)
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                S.voiceRecording(uiLang()),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = S.notificationChannelDescription(uiLang())
                setShowBadge(false)
            }
            val mgr = getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ringkesin")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createSilentNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setSilent(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "ringkesin_recording"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context, showNotification: Boolean = true) {
            val intent = Intent(context, RecordingService::class.java).apply {
                putExtra("show_notification", showNotification)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, RecordingService::class.java))
        }
    }
}
