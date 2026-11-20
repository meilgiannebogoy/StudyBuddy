package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AiStudyRepository
import com.example.ai.GeneratedFlashcardItem
import com.example.ai.GeneratedQuestionItem
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardEntity
import com.example.data.local.NoteEntity
import com.example.data.local.QuestionType
import com.example.data.local.QuizEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "AiAssistantViewModel"

enum class AiStudyMode(val title: String, val subtitle: String) {
    QUIZ("Quiz Maker", "Generate quiz questions from lesson notes"),
    FLASHCARDS("Flashcards", "Create study cards from pasted notes"),
    SUMMARIZE("Summarize", "Condense study material into key points"),
    EXPLAIN("Explain", "Break down tough concepts simply"),
    PRACTICE("Practice", "Create practice questions by subject")
}

sealed interface AiResultState {
    data object Idle : AiResultState
    data object Loading : AiResultState
    data class StreamingText(val text: String) : AiResultState
    data class TextSuccess(val text: String) : AiResultState
    data class QuizDraftReady(
        val title: String,
        val subject: String,
        val questions: List<GeneratedQuestionItem>,
        val savedQuizId: Long? = null
    ) : AiResultState
    data class FlashcardDraftReady(
        val title: String,
        val subject: String,
        val cards: List<GeneratedFlashcardItem>,
        val savedDeckId: Long? = null
    ) : AiResultState
    data class Error(val message: String) : AiResultState
}

data class AiAssistantUiState(
    val selectedMode: AiStudyMode = AiStudyMode.QUIZ,
    val subject: String = "Computer Science",
    val inputText: String = "",
    val resultState: AiResultState = AiResultState.Idle,
    val savedNotice: String? = null
)

class AiAssistantViewModel(
    private val aiRepository: AiStudyRepository,
    private val studyRepository: StudyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun selectMode(mode: AiStudyMode) {
        _uiState.update {
            it.copy(
                selectedMode = mode,
                resultState = AiResultState.Idle,
                savedNotice = null
            )
        }
    }

    fun updateSubject(subject: String) {
        _uiState.update { it.copy(subject = subject) }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun runAiTask() {
        val state = _uiState.value
        val trimmedInput = state.inputText.trim()
        if (trimmedInput.isBlank()) {
            _uiState.update {
                it.copy(
                    resultState = AiResultState.Error(
                        "Please enter lesson notes, a concept, or a topic first."
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(resultState = AiResultState.Loading, savedNotice = null) }
            try {
                when (state.selectedMode) {
                    AiStudyMode.QUIZ -> {
                        val draft = aiRepository.generateQuizFromNotes(state.subject, trimmedInput)
                        _uiState.update {
                            it.copy(
                                resultState = AiResultState.QuizDraftReady(
                                    title = draft.suggestedTitle,
                                    subject = state.subject,
                                    questions = draft.questions
                                )
                            )
                        }
                    }

                    AiStudyMode.PRACTICE -> {
                        val draft = aiRepository.generatePracticeQuestions(state.subject, trimmedInput)
                        _uiState.update {
                            it.copy(
                                resultState = AiResultState.QuizDraftReady(
                                    title = draft.suggestedTitle,
                                    subject = state.subject,
                                    questions = draft.questions
                                )
                            )
                        }
                    }

                    AiStudyMode.FLASHCARDS -> {
                        val draft = aiRepository.generateFlashcardsFromNotes(state.subject, trimmedInput)
                        _uiState.update {
                            it.copy(
                                resultState = AiResultState.FlashcardDraftReady(
                                    title = draft.suggestedTitle,
                                    subject = state.subject,
                                    cards = draft.cards
                                )
                            )
                        }
                    }

                    AiStudyMode.SUMMARIZE -> {
                        val accumulated = StringBuilder()
                        withTimeout(60_000L) {
                            aiRepository.streamSummary(state.subject, trimmedInput).collect { chunk ->
                                accumulated.append(chunk)
                                _uiState.update {
                                    it.copy(resultState = AiResultState.StreamingText(accumulated.toString()))
                                }
                            }
                        }
                        val finalText = accumulated.toString().trim()
                        _uiState.update {
                            it.copy(
                                resultState = if (finalText.isNotBlank()) {
                                    AiResultState.TextSuccess(finalText)
                                } else {
                                    AiResultState.Error("Model returned an empty summary.")
                                }
                            )
                        }
                    }

                    AiStudyMode.EXPLAIN -> {
                        val accumulated = StringBuilder()
                        withTimeout(60_000L) {
                            aiRepository.streamConceptExplanation(state.subject, trimmedInput).collect { chunk ->
                                accumulated.append(chunk)
                                _uiState.update {
                                    it.copy(resultState = AiResultState.StreamingText(accumulated.toString()))
                                }
                            }
                        }
                        val finalText = accumulated.toString().trim()
                        _uiState.update {
                            it.copy(
                                resultState = if (finalText.isNotBlank()) {
                                    AiResultState.TextSuccess(finalText)
                                } else {
                                    AiResultState.Error("Model returned an empty explanation.")
                                }
                            )
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI request timed out", e)
                _uiState.update {
                    it.copy(resultState = AiResultState.Error("AI request timed out after 60 seconds. Please try again."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                _uiState.update {
                    it.copy(
                        resultState = AiResultState.Error(
                            e.localizedMessage ?: "Failed to connect to Gemini AI. Please check your connection and try again."
                        )
                    )
                }
            }
        }
    }

    fun toggleQuizQuestionSelection(index: Int) {
        val current = _uiState.value.resultState as? AiResultState.QuizDraftReady ?: return
        val updated = current.questions.mapIndexed { i, q ->
            if (i == index) q.copy(isSelected = !q.isSelected) else q
        }
        _uiState.update { it.copy(resultState = current.copy(questions = updated)) }
    }

    fun updateDraftQuizTitle(title: String) {
        val current = _uiState.value.resultState as? AiResultState.QuizDraftReady ?: return
        _uiState.update { it.copy(resultState = current.copy(title = title)) }
    }

    fun saveReviewedQuizToLibrary() {
        val current = _uiState.value.resultState as? AiResultState.QuizDraftReady ?: return
        val selected = current.questions.filter { it.isSelected }
        if (selected.isEmpty()) {
            _uiState.update { it.copy(savedNotice = "Select at least one question to save.") }
            return
        }
        viewModelScope.launch {
            val quizId = studyRepository.insertQuiz(
                QuizEntity(
                    title = current.title.ifBlank { "AI Study Quiz" },
                    subject = current.subject
                )
            )
            selected.forEach { q ->
                studyRepository.insertQuestion(
                    QuizQuestionEntity(
                        quizId = quizId,
                        questionText = q.questionText,
                        questionType = if (q.isTrueFalse) QuestionType.TRUE_FALSE.name else QuestionType.MULTIPLE_CHOICE.name,
                        optionsJson = StudyRepository.encodeStringList(q.options),
                        correctIndex = q.correctIndex,
                        explanation = q.explanation
                    )
                )
            }
            _uiState.update {
                it.copy(
                    resultState = current.copy(savedQuizId = quizId),
                    savedNotice = "Saved ${selected.size} questions to \"${current.title}\"!"
                )
            }
        }
    }

    fun toggleFlashcardSelection(index: Int) {
        val current = _uiState.value.resultState as? AiResultState.FlashcardDraftReady ?: return
        val updated = current.cards.mapIndexed { i, c ->
            if (i == index) c.copy(isSelected = !c.isSelected) else c
        }
        _uiState.update { it.copy(resultState = current.copy(cards = updated)) }
    }

    fun updateDraftDeckTitle(title: String) {
        val current = _uiState.value.resultState as? AiResultState.FlashcardDraftReady ?: return
        _uiState.update { it.copy(resultState = current.copy(title = title)) }
    }

    fun saveReviewedFlashcardsToLibrary() {
        val current = _uiState.value.resultState as? AiResultState.FlashcardDraftReady ?: return
        val selected = current.cards.filter { it.isSelected }
        if (selected.isEmpty()) {
            _uiState.update { it.copy(savedNotice = "Select at least one flashcard to save.") }
            return
        }
        viewModelScope.launch {
            val deckId = studyRepository.insertDeck(
                FlashcardDeckEntity(
                    title = current.title.ifBlank { "AI Flashcard Deck" },
                    subject = current.subject
                )
            )
            selected.forEach { c ->
                studyRepository.insertCard(
                    FlashcardEntity(
                        deckId = deckId,
                        front = c.front,
                        back = c.back
                    )
                )
            }
            _uiState.update {
                it.copy(
                    resultState = current.copy(savedDeckId = deckId),
                    savedNotice = "Saved ${selected.size} flashcards to \"${current.title}\"!"
                )
            }
        }
    }

    fun saveTextResultAsNote() {
        val state = _uiState.value
        val textResult = (state.resultState as? AiResultState.TextSuccess)?.text ?: return
        val noteTitle = "${state.selectedMode.title}: ${state.subject}"
        viewModelScope.launch {
            studyRepository.insertNote(
                NoteEntity(
                    title = noteTitle,
                    content = textResult,
                    subject = state.subject
                )
            )
            _uiState.update { it.copy(savedNotice = "Saved to My Notes under ${state.subject}!") }
        }
    }

    class Factory(
        private val aiRepository: AiStudyRepository,
        private val studyRepository: StudyRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AiAssistantViewModel(aiRepository, studyRepository) as T
        }
    }
}
