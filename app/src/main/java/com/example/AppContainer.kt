package com.example

import android.content.Context
import com.example.ai.AiStudyRepository
import com.example.data.local.StudyBuddyDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.notifications.NotificationHelper

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database by lazy { StudyBuddyDatabase.getInstance(appContext) }

    val studyRepository: StudyRepository by lazy {
        StudyRepository(
            taskDao = database.taskDao(),
            scheduleDao = database.classScheduleDao(),
            noteDao = database.noteDao(),
            quizDao = database.quizDao(),
            flashcardDao = database.flashcardDao(),
            sessionDao = database.studySessionDao()
        )
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(appContext)
    }

    val aiStudyRepository: AiStudyRepository by lazy {
        AiStudyRepository()
    }
}
