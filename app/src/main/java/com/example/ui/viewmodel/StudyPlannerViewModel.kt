package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ClassScheduleEntity
import com.example.data.local.NoteEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyPlannerViewModel(
    private val studyRepository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val tasks: StateFlow<List<TaskEntity>> = studyRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<ClassScheduleEntity>> = studyRepository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = studyRepository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySessionEntity>> = studyRepository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Task operations
    fun saveTask(task: TaskEntity) {
        viewModelScope.launch {
            val id = if (task.id == 0L) {
                studyRepository.insertTask(task)
            } else {
                studyRepository.updateTask(task)
                task.id
            }
            val savedTask = task.copy(id = id)
            val prefs = preferencesRepository.userPreferences.first()
            if (prefs.notificationsEnabled && prefs.taskRemindersEnabled) {
                notificationHelper.scheduleTaskReminder(savedTask)
            } else {
                notificationHelper.cancelTaskReminder(id)
            }
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            studyRepository.toggleTaskCompletion(task)
            if (!task.isCompleted) {
                notificationHelper.cancelTaskReminder(task.id)
            } else {
                val prefs = preferencesRepository.userPreferences.first()
                if (prefs.notificationsEnabled && prefs.taskRemindersEnabled) {
                    notificationHelper.scheduleTaskReminder(task.copy(isCompleted = false))
                }
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            studyRepository.deleteTask(task)
            notificationHelper.cancelTaskReminder(task.id)
        }
    }

    // Class Schedule operations
    fun saveClassesForDays(
        existingId: Long,
        subjectName: String,
        instructor: String,
        room: String,
        days: List<Int>,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        colorTag: Int,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferences.first()
            val shouldNotify = prefs.notificationsEnabled && prefs.classRemindersEnabled && reminderEnabled
            if (existingId != 0L && days.size == 1) {
                val updated = ClassScheduleEntity(
                    id = existingId,
                    subjectName = subjectName.trim(),
                    instructor = instructor.trim(),
                    room = room.trim(),
                    dayOfWeek = days.first(),
                    startHour = startHour,
                    startMinute = startMinute,
                    endHour = endHour,
                    endMinute = endMinute,
                    colorTag = colorTag,
                    reminderEnabled = reminderEnabled
                )
                studyRepository.updateClass(updated)
                if (shouldNotify) notificationHelper.scheduleClassReminder(updated)
                else notificationHelper.cancelClassReminder(updated.id)
            } else {
                if (existingId != 0L) {
                    // Replace existing entry if editing days
                    classes.value.find { it.id == existingId }?.let {
                        studyRepository.deleteClass(it)
                        notificationHelper.cancelClassReminder(it.id)
                    }
                }
                days.distinct().forEach { day ->
                    val entity = ClassScheduleEntity(
                        subjectName = subjectName.trim(),
                        instructor = instructor.trim(),
                        room = room.trim(),
                        dayOfWeek = day,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        colorTag = colorTag,
                        reminderEnabled = reminderEnabled
                    )
                    val newId = studyRepository.insertClass(entity)
                    if (shouldNotify) {
                        notificationHelper.scheduleClassReminder(entity.copy(id = newId))
                    }
                }
            }
        }
    }

    fun deleteClass(item: ClassScheduleEntity) {
        viewModelScope.launch {
            studyRepository.deleteClass(item)
            notificationHelper.cancelClassReminder(item.id)
        }
    }

    // Note operations
    suspend fun getNoteById(id: Long): NoteEntity? = studyRepository.getNoteById(id)

    suspend fun saveNote(note: NoteEntity): Long {
        val now = System.currentTimeMillis()
        return if (note.id == 0L) {
            studyRepository.insertNote(note.copy(createdAtMillis = now, updatedAtMillis = now))
        } else {
            studyRepository.updateNote(note.copy(updatedAtMillis = now))
            note.id
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            studyRepository.deleteNote(note)
        }
    }

    // Profile & Notification Settings
    fun updateProfile(name: String, subtitle: String, dailyGoalMinutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updateProfile(name, subtitle, dailyGoalMinutes)
        }
    }

    fun updateNotificationSettings(
        notificationsEnabled: Boolean,
        taskReminders: Boolean,
        classReminders: Boolean,
        timerNotifications: Boolean,
        dailyReminder: Boolean,
        dailyHour: Int,
        dailyMinute: Int
    ) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationPreferences(
                notificationsEnabled = notificationsEnabled,
                taskReminders = taskReminders,
                classReminders = classReminders,
                timerNotifications = timerNotifications,
                dailyReminder = dailyReminder,
                dailyHour = dailyHour,
                dailyMinute = dailyMinute
            )
            if (notificationsEnabled && dailyReminder) {
                notificationHelper.scheduleDailyReminder(dailyHour, dailyMinute)
            } else {
                notificationHelper.cancelDailyReminder()
            }
        }
    }

    fun sendTestNotification() {
        notificationHelper.showNotification(
            notificationId = 8888,
            channelId = NotificationHelper.CHANNEL_DAILY,
            title = "StudyBuddy Reminders Active",
            message = "You'll receive timely reminders for deadlines, classes, and Pomodoro sessions."
        )
    }

    class Factory(
        private val studyRepository: StudyRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val notificationHelper: NotificationHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudyPlannerViewModel(studyRepository, preferencesRepository, notificationHelper) as T
        }
    }
}
