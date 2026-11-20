package com.example.data.repository

import com.example.data.local.CardStatus
import com.example.data.local.ClassScheduleDao
import com.example.data.local.ClassScheduleEntity
import com.example.data.local.FlashcardDao
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.NoteDao
import com.example.data.local.NoteEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.QuizDao
import com.example.data.local.QuizEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.local.StudySessionDao
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class StudyRepository(
    private val taskDao: TaskDao,
    private val scheduleDao: ClassScheduleDao,
    private val noteDao: NoteDao,
    private val quizDao: QuizDao,
    private val flashcardDao: FlashcardDao,
    private val sessionDao: StudySessionDao
) {
    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.observeAllTasks()
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val nowCompleted = !task.isCompleted
        taskDao.updateTask(
            task.copy(
                isCompleted = nowCompleted,
                completedAtMillis = if (nowCompleted) System.currentTimeMillis() else null
            )
        )
    }

    // Schedule
    val allClasses: Flow<List<ClassScheduleEntity>> = scheduleDao.observeAllClasses()
    suspend fun insertClass(item: ClassScheduleEntity): Long = scheduleDao.insertClass(item)
    suspend fun updateClass(item: ClassScheduleEntity) = scheduleDao.updateClass(item)
    suspend fun deleteClass(item: ClassScheduleEntity) = scheduleDao.deleteClass(item)

    // Notes
    val allNotes: Flow<List<NoteEntity>> = noteDao.observeAllNotes()
    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    // Quizzes
    val allQuizzes: Flow<List<QuizEntity>> = quizDao.observeAllQuizzes()
    val allQuizQuestions: Flow<List<QuizQuestionEntity>> = quizDao.observeAllQuestions()
    val allQuizAttempts: Flow<List<QuizAttemptEntity>> = quizDao.observeAllAttempts()

    fun observeQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>> =
        quizDao.observeQuestionsForQuiz(quizId)

    fun observeAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>> =
        quizDao.observeAttemptsForQuiz(quizId)

    suspend fun getQuizById(quizId: Long): QuizEntity? = quizDao.getQuizById(quizId)
    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity> =
        quizDao.getQuestionsForQuiz(quizId)

    suspend fun insertQuiz(quiz: QuizEntity): Long = quizDao.insertQuiz(quiz)
    suspend fun updateQuiz(quiz: QuizEntity) = quizDao.updateQuiz(quiz)
    suspend fun deleteQuiz(quiz: QuizEntity) = quizDao.deleteQuiz(quiz)

    suspend fun insertQuestion(question: QuizQuestionEntity): Long = quizDao.insertQuestion(question)
    suspend fun updateQuestion(question: QuizQuestionEntity) = quizDao.updateQuestion(question)
    suspend fun deleteQuestion(question: QuizQuestionEntity) = quizDao.deleteQuestion(question)

    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity): Long = quizDao.insertAttempt(attempt)
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity? = quizDao.getAttemptById(attemptId)

    // Flashcards
    val allDecks: Flow<List<FlashcardDeckEntity>> = flashcardDao.observeAllDecks()
    val allFlashcards: Flow<List<FlashcardEntity>> = flashcardDao.observeAllCards()

    fun observeCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>> =
        flashcardDao.observeCardsForDeck(deckId)

    suspend fun getDeckById(deckId: Long): FlashcardDeckEntity? = flashcardDao.getDeckById(deckId)
    suspend fun insertDeck(deck: FlashcardDeckEntity): Long = flashcardDao.insertDeck(deck)
    suspend fun updateDeck(deck: FlashcardDeckEntity) = flashcardDao.updateDeck(deck)
    suspend fun deleteDeck(deck: FlashcardDeckEntity) = flashcardDao.deleteDeck(deck)

    suspend fun insertCard(card: FlashcardEntity): Long = flashcardDao.insertCard(card)
    suspend fun updateCard(card: FlashcardEntity) = flashcardDao.updateCard(card)
    suspend fun deleteCard(card: FlashcardEntity) = flashcardDao.deleteCard(card)
    suspend fun markCardStatus(card: FlashcardEntity, status: CardStatus) {
        flashcardDao.updateCard(
            card.copy(
                status = status.name,
                lastReviewedMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun resetDeckProgress(deckId: Long) = flashcardDao.resetDeckProgress(deckId)

    // Study Sessions
    val allSessions: Flow<List<StudySessionEntity>> = sessionDao.observeAllSessions()
    suspend fun logStudySession(subject: String, durationMinutes: Int, sessionType: String) {
        sessionDao.insertSession(
            StudySessionEntity(
                subject = subject,
                durationMinutes = durationMinutes,
                sessionType = sessionType
            )
        )
    }

    companion object {
        fun encodeStringList(items: List<String>): String {
            val array = JSONArray()
            items.forEach { array.put(it) }
            return array.toString()
        }

        fun decodeStringList(json: String): List<String> {
            return try {
                val array = JSONArray(json)
                List(array.length()) { index -> array.getString(index) }
            } catch (_: Exception) {
                emptyList()
            }
        }

        fun encodeIntList(items: List<Int>): String {
            val array = JSONArray()
            items.forEach { array.put(it) }
            return array.toString()
        }

        fun decodeIntList(json: String): List<Int> {
            return try {
                val array = JSONArray(json)
                List(array.length()) { index -> array.getInt(index) }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
