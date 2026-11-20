package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "studybuddy_prefs")

data class UserPreferences(
    val studentName: String = "Alex",
    val studentSubtitle: String = "Computer Science • Study Mode",
    val dailyStudyGoalMinutes: Int = 120,
    val pomodoroFocusMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val notificationsEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val classRemindersEnabled: Boolean = true,
    val timerNotificationsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 19,
    val dailyReminderMinute: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val STUDENT_SUBTITLE = stringPreferencesKey("student_subtitle")
        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val POMODORO_FOCUS_MINUTES = intPreferencesKey("pomodoro_focus_minutes")
        val POMODORO_BREAK_MINUTES = intPreferencesKey("pomodoro_break_minutes")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val TASK_REMINDERS_ENABLED = booleanPreferencesKey("task_reminders_enabled")
        val CLASS_REMINDERS_ENABLED = booleanPreferencesKey("class_reminders_enabled")
        val TIMER_NOTIFICATIONS_ENABLED = booleanPreferencesKey("timer_notifications_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            studentName = prefs[Keys.STUDENT_NAME] ?: "Alex",
            studentSubtitle = prefs[Keys.STUDENT_SUBTITLE] ?: "Student • Focused Learner",
            dailyStudyGoalMinutes = prefs[Keys.DAILY_GOAL_MINUTES] ?: 120,
            pomodoroFocusMinutes = prefs[Keys.POMODORO_FOCUS_MINUTES] ?: 25,
            pomodoroBreakMinutes = prefs[Keys.POMODORO_BREAK_MINUTES] ?: 5,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            taskRemindersEnabled = prefs[Keys.TASK_REMINDERS_ENABLED] ?: true,
            classRemindersEnabled = prefs[Keys.CLASS_REMINDERS_ENABLED] ?: true,
            timerNotificationsEnabled = prefs[Keys.TIMER_NOTIFICATIONS_ENABLED] ?: true,
            dailyReminderEnabled = prefs[Keys.DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderHour = prefs[Keys.DAILY_REMINDER_HOUR] ?: 19,
            dailyReminderMinute = prefs[Keys.DAILY_REMINDER_MINUTE] ?: 0
        )
    }

    suspend fun updateProfile(name: String, subtitle: String, dailyGoalMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.STUDENT_NAME] = name.trim().ifEmpty { "Student" }
            prefs[Keys.STUDENT_SUBTITLE] = subtitle.trim()
            prefs[Keys.DAILY_GOAL_MINUTES] = dailyGoalMinutes.coerceIn(15, 600)
        }
    }

    suspend fun updateTimerDurations(focusMinutes: Int, breakMinutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.POMODORO_FOCUS_MINUTES] = focusMinutes.coerceIn(1, 120)
            prefs[Keys.POMODORO_BREAK_MINUTES] = breakMinutes.coerceIn(1, 60)
        }
    }

    suspend fun updateNotificationPreferences(
        notificationsEnabled: Boolean,
        taskReminders: Boolean,
        classReminders: Boolean,
        timerNotifications: Boolean,
        dailyReminder: Boolean,
        dailyHour: Int,
        dailyMinute: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = notificationsEnabled
            prefs[Keys.TASK_REMINDERS_ENABLED] = taskReminders
            prefs[Keys.CLASS_REMINDERS_ENABLED] = classReminders
            prefs[Keys.TIMER_NOTIFICATIONS_ENABLED] = timerNotifications
            prefs[Keys.DAILY_REMINDER_ENABLED] = dailyReminder
            prefs[Keys.DAILY_REMINDER_HOUR] = dailyHour.coerceIn(0, 23)
            prefs[Keys.DAILY_REMINDER_MINUTE] = dailyMinute.coerceIn(0, 59)
        }
    }
}
