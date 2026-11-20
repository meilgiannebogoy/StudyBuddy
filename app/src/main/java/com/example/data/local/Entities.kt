package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskPriority(val label: String, val weight: Int) {
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1)
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val subject: String,
    val dueDateMillis: Long,
    val priority: String = TaskPriority.MEDIUM.name,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long? = null,
    val reminderEnabled: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "class_schedules")
data class ClassScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectName: String,
    val instructor: String,
    val room: String,
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val colorTag: Int = 0,
    val reminderEnabled: Boolean = true
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subject: String,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class QuestionType(val label: String) {
    MULTIPLE_CHOICE("Multiple Choice"),
    TRUE_FALSE("True or False")
}

@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("quizId")]
)
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val questionText: String,
    val questionType: String = QuestionType.MULTIPLE_CHOICE.name,
    val optionsJson: String,
    val correctIndex: Int,
    val explanation: String
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizId: Long,
    val quizTitle: String,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val answersJson: String,
    val completedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "flashcard_decks")
data class FlashcardDeckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subject: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

enum class CardStatus {
    NEW,
    NEEDS_PRACTICE,
    MASTERED
}

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardDeckEntity::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deckId")]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val status: String = CardStatus.NEW.name,
    val lastReviewedMillis: Long? = null
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val sessionType: String = "FOCUS",
    val completedAtMillis: Long = System.currentTimeMillis()
)
