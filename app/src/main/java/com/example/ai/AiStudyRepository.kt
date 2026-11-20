package com.example.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class GeneratedQuizDraft(
    val suggestedTitle: String,
    val questions: List<GeneratedQuestionItem>
)

data class GeneratedQuestionItem(
    val questionText: String,
    val isTrueFalse: Boolean,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val isSelected: Boolean = true
)

data class GeneratedFlashcardDraft(
    val suggestedTitle: String,
    val cards: List<GeneratedFlashcardItem>
)

data class GeneratedFlashcardItem(
    val front: String,
    val back: String,
    val isSelected: Boolean = true
)

class AiStudyRepository {

    private val textModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val quizSchema = Schema.obj(
        properties = mapOf(
            "suggestedTitle" to Schema.string(description = "A concise title for this quiz"),
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "questionText" to Schema.string(description = "The quiz question"),
                        "questionType" to Schema.enumeration(
                            values = listOf("MULTIPLE_CHOICE", "TRUE_FALSE"),
                            description = "Question format"
                        ),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Answer choices (4 choices for MULTIPLE_CHOICE, or ['True', 'False'] for TRUE_FALSE)"
                        ),
                        "correctIndex" to Schema.integer(description = "0-based index of the correct option"),
                        "explanation" to Schema.string(description = "Short explanation grounded in the provided text")
                    )
                ),
                description = "List of generated questions"
            )
        )
    )

    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizSchema
            }
        )
    }

    private val flashcardSchema = Schema.obj(
        properties = mapOf(
            "suggestedTitle" to Schema.string(description = "A concise title for the flashcard deck"),
            "cards" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "front" to Schema.string(description = "Term, question, or concept"),
                        "back" to Schema.string(description = "Clear definition or answer grounded in the source text")
                    )
                ),
                description = "List of study flashcards"
            )
        )
    )

    private val flashcardModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = flashcardSchema
            }
        )
    }

    suspend fun generateQuizFromNotes(
        subject: String,
        sourceText: String,
        questionCount: Int = 5
    ): GeneratedQuizDraft {
        val prompt = """
            You are StudyBuddy, a strict academic study assistant.
            Generate $questionCount quiz questions (mix of MULTIPLE_CHOICE with 4 options and TRUE_FALSE with ["True", "False"])
            for the subject "$subject".
            CRITICAL RULE: Base every question, option, correct answer, and explanation strictly on the student's provided study material below. Do not invent or fabricate external facts not supported by the material.
            
            Study Material:
            $sourceText
        """.trimIndent()

        val response = withTimeout(60_000L) {
            quizModel.generateContent(prompt)
        }
        val jsonText = response.text ?: throw IllegalStateException("AI returned an empty response.")
        return parseQuizDraft(jsonText)
    }

    suspend fun generatePracticeQuestions(
        subject: String,
        topic: String,
        questionCount: Int = 5
    ): GeneratedQuizDraft {
        val prompt = """
            You are StudyBuddy, an academic tutor.
            Create $questionCount practice quiz questions for the subject "$subject" focusing on "$topic".
            Include clear explanations for each correct answer.
            Use a mix of MULTIPLE_CHOICE (4 choices) and TRUE_FALSE (["True", "False"]).
        """.trimIndent()

        val response = withTimeout(60_000L) {
            quizModel.generateContent(prompt)
        }
        val jsonText = response.text ?: throw IllegalStateException("AI returned an empty response.")
        return parseQuizDraft(jsonText)
    }

    suspend fun generateFlashcardsFromNotes(
        subject: String,
        sourceText: String,
        cardCount: Int = 6
    ): GeneratedFlashcardDraft {
        val prompt = """
            You are StudyBuddy, a strict academic study assistant.
            Create $cardCount clear, concise flashcards for the subject "$subject".
            CRITICAL RULE: Base every term and definition strictly on the provided study material below. Do not fabricate facts outside the provided notes.
            
            Study Material:
            $sourceText
        """.trimIndent()

        val response = withTimeout(60_000L) {
            flashcardModel.generateContent(prompt)
        }
        val jsonText = response.text ?: throw IllegalStateException("AI returned an empty response.")
        return parseFlashcardDraft(jsonText)
    }

    fun streamSummary(subject: String, sourceText: String): Flow<String> {
        val prompt = """
            You are StudyBuddy, a helpful academic study companion.
            Summarize the following "$subject" study material into well-structured, easy-to-review bullet points and key takeaways.
            CRITICAL RULE: Only summarize information present in the provided text. Do not fabricate extra facts.
            
            Study Material:
            $sourceText
        """.trimIndent()

        return textModel.generateContentStream(prompt).map { chunk -> chunk.text.orEmpty() }
    }

    fun streamConceptExplanation(subject: String, conceptText: String): Flow<String> {
        val prompt = """
            You are StudyBuddy, a patient and encouraging tutor.
            Explain the following "$subject" concept in simple, crystal-clear language that a student can quickly grasp.
            Include a short intuitive analogy if helpful, and break it down step by step.
            
            Concept or excerpt from student:
            $conceptText
        """.trimIndent()

        return textModel.generateContentStream(prompt).map { chunk -> chunk.text.orEmpty() }
    }

    private fun parseQuizDraft(jsonText: String): GeneratedQuizDraft {
        val root = JSONObject(jsonText)
        val title = root.optString("suggestedTitle", "AI Study Quiz")
        val qArray = root.getJSONArray("questions")
        val questions = buildList {
            for (i in 0 until qArray.length()) {
                val obj = qArray.getJSONObject(i)
                val qType = obj.optString("questionType", "MULTIPLE_CHOICE")
                val isTf = qType == "TRUE_FALSE"
                val optsJson = obj.getJSONArray("options")
                val rawOptions = (0 until optsJson.length()).map { idx -> optsJson.getString(idx) }
                val options = if (isTf) {
                    listOf("True", "False")
                } else {
                    rawOptions.ifEmpty { listOf("Option A", "Option B", "Option C", "Option D") }
                }
                val rawCorrect = obj.optInt("correctIndex", 0)
                val safeCorrect = rawCorrect.coerceIn(0, (options.size - 1).coerceAtLeast(0))
                add(
                    GeneratedQuestionItem(
                        questionText = obj.getString("questionText"),
                        isTrueFalse = isTf,
                        options = options,
                        correctIndex = safeCorrect,
                        explanation = obj.optString("explanation", "")
                    )
                )
            }
        }
        return GeneratedQuizDraft(suggestedTitle = title, questions = questions)
    }

    private fun parseFlashcardDraft(jsonText: String): GeneratedFlashcardDraft {
        val root = JSONObject(jsonText)
        val title = root.optString("suggestedTitle", "AI Flashcard Deck")
        val cArray = root.getJSONArray("cards")
        val cards = buildList {
            for (i in 0 until cArray.length()) {
                val obj = cArray.getJSONObject(i)
                add(
                    GeneratedFlashcardItem(
                        front = obj.getString("front"),
                        back = obj.getString("back")
                    )
                )
            }
        }
        return GeneratedFlashcardDraft(suggestedTitle = title, cards = cards)
    }
}
