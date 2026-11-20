package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StudyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 5000)
        val channelId = intent.getStringExtra(NotificationHelper.EXTRA_CHANNEL_ID)
            ?: NotificationHelper.CHANNEL_DAILY
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "StudyBuddy Reminder"
        val message = intent.getStringExtra(NotificationHelper.EXTRA_MESSAGE)
            ?: "Stay on track with your study goals today!"

        NotificationHelper(context.applicationContext).showNotification(
            notificationId = notificationId,
            channelId = channelId,
            title = title,
            message = message
        )
    }
}
