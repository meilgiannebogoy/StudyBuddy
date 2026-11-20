package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CardStatus
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.QuizEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LearningToolsViewModel(
    private val studyRepository: StudyRepository
) : ViewModel() {

    val quizzes: StateFlow<List<QuizEntity>> = studyRepository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizQuestions: StateFlow<List<QuizQuestionEntity>> = studyRepository.allQuizQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = studyRepository.allQuizAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val decks: StateFlow<List<FlashcardDeckEntity>> = studyRepository.allDecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlashcards: StateFlow<List<FlashcardEntity>> = studyRepository.allFlashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeQuestionsForQuiz(quizId: Long): Flow<List<QuizQuestionEntity>> =
        studyRepository.observeQuestionsForQuiz(quizId)

    fun observeAttemptsForQuiz(quizId: Long): Flow<List<QuizAttemptEntity>> =
        studyRepository.observeAttemptsForQuiz(quizId)

    fun observeCardsForDeck(deckId: Long): Flow<List<FlashcardEntity>> =
        studyRepository.observeCardsForDeck(deckId)

    suspend fun getQuizById(quizId: Long): QuizEntity? = studyRepository.getQuizById(quizId)
    suspend fun getDeckById(deckId: Long): FlashcardDeckEntity? = studyRepository.getDeckById(deckId)
    suspend fun getAttemptById(attemptId: Long): QuizAttemptEntity? = studyRepository.getAttemptById(attemptId)
    suspend fun getQuestionsForQuiz(quizId: Long): List<QuizQuestionEntity> =
        studyRepository.getQuestionsForQuiz(quizId)

    // Quiz CRUD
    suspend fun createOrUpdateQuiz(id: Long, title: String, subject: String): Long {
        return if (id == 0L) {
            studyRepository.insertQuiz(QuizEntity(title = title.trim(), subject = subject.trim()))
        } else {
            val existing = studyRepository.getQuizById(id)
            if (existing != null) {
                studyRepository.updateQuiz(existing.copy(title = title.trim(), subject = subject.trim()))
            }
            id
        }
    }

    fun deleteQuiz(quiz: QuizEntity) {
        viewModelScope.launch { studyRepository.deleteQuiz(quiz) }
    }

    fun saveQuestion(question: QuizQuestionEntity) {
        viewModelScope.launch {
            if (question.id == 0L) {
                studyRepository.insertQuestion(question)
            } else {
                studyRepository.updateQuestion(question)
            }
        }
    }

    fun deleteQuestion(question: QuizQuestionEntity) {
        viewModelScope.launch { studyRepository.deleteQuestion(question) }
    }

    suspend fun submitQuizAttempt(
        quiz: QuizEntity,
        questions: List<QuizQuestionEntity>,
        selectedAnswers: List<Int>
    ): Long {
        var correctCount = 0
        questions.forEachIndexed { index, q ->
            if (selectedAnswers.getOrNull(index) == q.correctIndex) {
                correctCount++
            }
        }
        val total = questions.size.coerceAtLeast(1)
        val percentage = ((correctCount.toFloat() / total) * 100f).toInt()
        val attempt = QuizAttemptEntity(
            quizId = quiz.id,
            quizTitle = quiz.title,
            subject = quiz.subject,
            score = correctCount,
            totalQuestions = questions.size,
            percentage = percentage,
            answersJson = StudyRepository.encodeIntList(selectedAnswers)
        )
        return studyRepository.insertQuizAttempt(attempt)
    }

    // Flashcard CRUD & Study
    suspend fun createOrUpdateDeck(id: Long, title: String, subject: String): Long {
        return if (id == 0L) {
            studyRepository.insertDeck(FlashcardDeckEntity(title = title.trim(), subject = subject.trim()))
        } else {
            val existing = studyRepository.getDeckById(id)
            if (existing != null) {
                studyRepository.updateDeck(existing.copy(title = title.trim(), subject = subject.trim()))
            }
            id
        }
    }

    fun deleteDeck(deck: FlashcardDeckEntity) {
        viewModelScope.launch { studyRepository.deleteDeck(deck) }
    }

    fun saveFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            if (card.id == 0L) {
                studyRepository.insertCard(card)
            } else {
                studyRepository.updateCard(card)
            }
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch { studyRepository.deleteCard(card) }
    }

    fun markCardStatus(card: FlashcardEntity, status: CardStatus) {
        viewModelScope.launch {
            studyRepository.markCardStatus(card, status)
        }
    }

    fun resetDeckProgress(deckId: Long) {
        viewModelScope.launch {
            studyRepository.resetDeckProgress(deckId)
        }
    }

    class Factory(
        private val studyRepository: StudyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LearningToolsViewModel(studyRepository) as T
        }
    }
}
