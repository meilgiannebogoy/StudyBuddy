package com.example.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.ClassScheduleEntity
import com.example.data.local.TaskEntity
import java.util.Calendar

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_TIMER = "study_timer_channel"
        const val CHANNEL_TASKS = "task_deadline_channel"
        const val CHANNEL_SCHEDULE = "class_schedule_channel"
        const val CHANNEL_DAILY = "daily_reminder_channel"

        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        private const val DAILY_REMINDER_REQ_CODE = 9001
    }

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TIMER,
                    context.getString(R.string.channel_timer_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.channel_timer_desc)
                },
                NotificationChannel(
                    CHANNEL_TASKS,
                    context.getString(R.string.channel_tasks_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.channel_tasks_desc)
                },
                NotificationChannel(
                    CHANNEL_SCHEDULE,
                    context.getString(R.string.channel_schedule_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.channel_schedule_desc)
                },
                NotificationChannel(
                    CHANNEL_DAILY,
                    context.getString(R.string.channel_daily_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.channel_daily_desc)
                }
            )
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        notificationId: Int,
        channelId: String,
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission()) return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(
                if (channelId == CHANNEL_TIMER) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime
        }
    }

    fun showTimerFinishedNotification(wasFocusSession: Boolean, subject: String) {
        val title = if (wasFocusSession) {
            "Focus Session Complete!"
        } else {
            "Break Finished!"
        }
        val message = if (wasFocusSession) {
            "Great work on $subject! Time for a well-deserved break."
        } else {
            "Break is over. Ready to jump back into $subject?"
        }
        showNotification(
            notificationId = 1001,
            channelId = CHANNEL_TIMER,
            title = title,
            message = message
        )
    }

    fun scheduleTaskReminder(task: TaskEntity) {
        if (!task.reminderEnabled || task.isCompleted) {
            cancelTaskReminder(task.id)
            return
        }
        val now = System.currentTimeMillis()
        val oneHourBefore = task.dueDateMillis - 60 * 60 * 1000L
        val triggerAt = when {
            oneHourBefore > now -> oneHourBefore
            task.dueDateMillis > now -> task.dueDateMillis
            else -> return
        }
        val reqCode = (20000 + (task.id % 10000)).toInt()
        val priorityPrefix = if (task.priority == "HIGH") "Important Task: " else "Upcoming Deadline: "
        scheduleAlarm(
            requestCode = reqCode,
            triggerAtMillis = triggerAt,
            channelId = CHANNEL_TASKS,
            title = "$priorityPrefix${task.title}",
            message = "${task.subject} assignment is due soon."
        )
    }

    fun cancelTaskReminder(taskId: Long) {
        val reqCode = (20000 + (taskId % 10000)).toInt()
        cancelAlarm(reqCode)
    }

    fun scheduleClassReminder(item: ClassScheduleEntity) {
        if (!item.reminderEnabled) {
            cancelClassReminder(item.id)
            return
        }
        val cal = Calendar.getInstance()
        val targetCalendarDay = when (item.dayOfWeek) {
            1 -> Calendar.MONDAY
            2 -> Calendar.TUESDAY
            3 -> Calendar.WEDNESDAY
            4 -> Calendar.THURSDAY
            5 -> Calendar.FRIDAY
            6 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }
        cal.set(Calendar.HOUR_OF_DAY, item.startHour)
        cal.set(Calendar.MINUTE, item.startMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MINUTE, -15)

        val currentDay = cal.get(Calendar.DAY_OF_WEEK)
        var daysUntil = (targetCalendarDay - currentDay + 7) % 7
        if (daysUntil == 0 && cal.timeInMillis <= System.currentTimeMillis()) {
            daysUntil = 7
        }
        cal.add(Calendar.DAY_OF_YEAR, daysUntil)

        val reqCode = (40000 + (item.id % 10000)).toInt()
        val roomInfo = if (item.room.isNotBlank()) " in ${item.room}" else ""
        scheduleAlarm(
            requestCode = reqCode,
            triggerAtMillis = cal.timeInMillis,
            channelId = CHANNEL_SCHEDULE,
            title = "Class Starting Soon: ${item.subjectName}",
            message = "Starts in 15 minutes$roomInfo."
        )
    }

    fun cancelClassReminder(classId: Long) {
        val reqCode = (40000 + (classId % 10000)).toInt()
        cancelAlarm(reqCode)
    }

    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildReceiverPendingIntent(
            requestCode = DAILY_REMINDER_REQ_CODE,
            channelId = CHANNEL_DAILY,
            title = "Time to Study with StudyBuddy",
            message = "Check your pending tasks, review flashcards, or start a Pomodoro focus session!"
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder() {
        cancelAlarm(DAILY_REMINDER_REQ_CODE)
    }

    private fun scheduleAlarm(
        requestCode: Int,
        triggerAtMillis: Long,
        channelId: String,
        title: String,
        message: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = buildReceiverPendingIntent(requestCode, channelId, title, message)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, StudyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildReceiverPendingIntent(
        requestCode: Int,
        channelId: String,
        title: String,
        message: String
    ): PendingIntent {
        val intent = Intent(context, StudyReminderReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
