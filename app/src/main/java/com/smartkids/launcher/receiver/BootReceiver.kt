package com.smartkids.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartkids.launcher.service.ScreenTimeManager
import com.smartkids.launcher.service.ScreenTimeService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(
                ScreenTimeService.buildIntent(
                    context,
                    ScreenTimeManager.DEFAULT_DAILY_LIMIT_MINUTES
                )
            )
        }
    }
}