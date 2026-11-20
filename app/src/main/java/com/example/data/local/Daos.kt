package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?
}

@Dao
interface ClassScheduleDao {
    @Query("SELECT * FROM class_schedules ORDER BY dayOfWeek ASC, startHour ASC, startMinute ASC")
    fun observeAllClasses(): Flow<List<ClassScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(item: ClassScheduleEntity): Long

    @Update
    suspend fun updateClass(item: ClassScheduleEntity)

    @Delete
    suspend fun deleteClass(item: ClassScheduleEntity)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAtMillis DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes ORDER BY createdAtMillis DESC")
    fun observeAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    suspend fun getQuizById(quizId: Long): QuizEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Update
    suspend fun updateQuiz(quiz: QuizEntity)

    @Delete
    suspend fun deleteQuiz(quiz: QuizEntity)

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    fun observeQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions")
    fun observeAllQuestions(): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE quizId = :quizId ORDER BY id ASC")
    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuizQuestionEntity): Long

    @Update
    suspend fun updateQuestion(question: QuizQuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuizQuestionEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY completedAtMillis DESC")
    fun observeAllAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE quizId = :quizId ORDER BY completedAtMillis DESC")
    fun observeAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE id = :attemptId")
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: QuizAttemptEntity): Long
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcard_decks ORDER BY createdAtMillis DESC")
    fun observeAllDecks(): Flow<List<FlashcardDeckEntity>>

    @Query("SELECT * FROM flashcard_decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Long): FlashcardDeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long

    @Update
    suspend fun updateDeck(deck: FlashcardDeckEntity)

    @Delete
    suspend fun deleteDeck(deck: FlashcardDeckEntity)

    @Query("SELECT * FROM flashcards")
    fun observeAllCards(): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id ASC")
    fun observeCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Delete
    suspend fun deleteCard(card: FlashcardEntity)

    @Query("UPDATE flashcards SET status = 'NEW', lastReviewedMillis = NULL WHERE deckId = :deckId")
    suspend fun resetDeckProgress(deckId: Long)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY completedAtMillis DESC")
    fun observeAllSessions(): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long
}
