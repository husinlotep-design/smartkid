package com.smartkids.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenTimeService : Service() {

    @Inject
    lateinit var screenTimeManager: ScreenTimeManager

    companion object {
        const val CHANNEL_ID = "smartkids_screen_time"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_LIMIT_MINUTES = "limit_minutes"

        fun buildIntent(context: Context, limitMinutes: Int): Intent =
            Intent(context, ScreenTimeService::class.java).apply {
                putExtra(EXTRA_LIMIT_MINUTES, limitMinutes)
            }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val limit = intent?.getIntExtra(
            EXTRA_LIMIT_MINUTES,
            ScreenTimeManager.DEFAULT_DAILY_LIMIT_MINUTES
        ) ?: ScreenTimeManager.DEFAULT_DAILY_LIMIT_MINUTES

        // Must call startForeground within 5 seconds of onStartCommand
        startForeground(NOTIFICATION_ID, buildNotification(limit))

        screenTimeManager.startTracking(limit)

        return START_STICKY
    }

    override fun onDestroy() {
        screenTimeManager.stopTracking()
        super.onDestroy()
    }

    // Not a bound service — return null
    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Screen Time Tracker",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Tracks SmartKids daily screen time"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(limitMinutes: Int): Notification {
        val elapsed = screenTimeManager.uiState.value.elapsedMinutes
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SmartKids Active")
            .setContentText("Screen time: $elapsed / $limitMinutes min")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}