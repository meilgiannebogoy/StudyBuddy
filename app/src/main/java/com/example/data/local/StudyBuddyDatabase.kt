package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        ClassScheduleEntity::class,
        NoteEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        QuizAttemptEntity::class,
        FlashcardDeckEntity::class,
        FlashcardEntity::class,
        StudySessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudyBuddyDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun noteDao(): NoteDao
    abstract fun quizDao(): QuizDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: StudyBuddyDatabase? = null

        fun getInstance(context: Context): StudyBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StudyBuddyDatabase::class.java,
                    "studybuddy_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
