package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.QuestionType
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.QuizEntity
import com.example.data.local.QuizQuestionEntity
import com.example.data.repository.StudyRepository
import com.example.ui.components.DefaultSubjects
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PastelOutlinedButton
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.SubjectBadge
import com.example.ui.components.formatDateTimeShort
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftCoralContainer
import com.example.ui.theme.SoftCoralUrgent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftMintContainer
import com.example.ui.theme.SoftMintSuccess
import com.example.ui.theme.SoftSkyTint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizListScreen(
    quizzes: List<QuizEntity>,
    allQuestions: List<QuizQuestionEntity>,
    attempts: List<QuizAttemptEntity>,
    onBack: () -> Unit,
    onCreateQuiz: suspend (title: String, subject: String) -> Long,
    onOpenQuizEditor: (Long) -> Unit,
    onStartQuiz: (Long) -> Unit,
    onReviewAttempt: (Long) -> Unit,
    onDeleteQuiz: (QuizEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showHistoryTab by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = { Text("Quizzes", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyHeading)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIceBackground)
            )
        },
        floatingActionButton = {
            if (!showHistoryTab) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PastelBluePrimary,
                    contentColor = PureWhiteSurface,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("create_quiz_fab")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create Quiz")
                }
            }
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = !showHistoryTab,
                        onClick = { showHistoryTab = false },
                        label = { Text("My Quizzes (${quizzes.size})") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelBluePrimary,
                            selectedLabelColor = PureWhiteSurface,
                            containerColor = PureWhiteSurface
                        )
                    )
                    FilterChip(
                        selected = showHistoryTab,
                        onClick = { showHistoryTab = true },
                        leadingIcon = { Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Past Results (${attempts.size})") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelBluePrimary,
                            selectedLabelColor = PureWhiteSurface,
                            containerColor = PureWhiteSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!showHistoryTab) {
                    if (quizzes.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.Quiz,
                            title = "No quizzes yet",
                            subtitle = "Build custom Multiple-Choice and True/False quizzes with explanations.",
                            actionLabel = "Create First Quiz",
                            onAction = { showCreateDialog = true }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(quizzes, key = { it.id }) { quiz ->
                                val qCount = allQuestions.count { it.quizId == quiz.id }
                                val latestAttempt = attempts.firstOrNull { it.quizId == quiz.id }
                                StudyCard(contentPadding = PaddingValues(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(quiz.title, style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                SubjectBadge(quiz.subject)
                                                Text(
                                                    "$qCount questions",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MutedBlueGray
                                                )
                                            }
                                            if (latestAttempt != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Last score: ${latestAttempt.score}/${latestAttempt.totalQuestions} (${latestAttempt.percentage}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SkyBlueAccent
                                                )
                                            }
                                        }
                                        IconButton(onClick = { onOpenQuizEditor(quiz.id) }) {
                                            Icon(Icons.Rounded.Edit, contentDescription = "Edit quiz", tint = SkyBlueAccent)
                                        }
                                        IconButton(onClick = { onDeleteQuiz(quiz) }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete quiz", tint = MutedBlueGray)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        PastelOutlinedButton(
                                            text = "Edit Questions",
                                            icon = Icons.Rounded.Edit,
                                            onClick = { onOpenQuizEditor(quiz.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        PastelPrimaryButton(
                                            text = "Take Quiz",
                                            icon = Icons.Rounded.PlayArrow,
                                            enabled = qCount > 0,
                                            onClick = { onStartQuiz(quiz.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (attempts.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.History,
                            title = "No quiz attempts yet",
                            subtitle = "Complete a quiz to track your scores and review explanations here."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(attempts, key = { it.id }) { attempt ->
                                StudyCard(
                                    onClick = { onReviewAttempt(attempt.id) },
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(attempt.quizTitle, style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                SubjectBadge(attempt.subject)
                                                Text(
                                                    formatDateTimeShort(attempt.completedAtMillis),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MutedBlueGray
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (attempt.percentage >= 70) SoftMintContainer else SoftSkyTint
                                        ) {
                                            Text(
                                                text = "${attempt.percentage}%",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (attempt.percentage >= 70) SoftMintSuccess else SkyBlueAccent,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        QuizMetaDialog(
            initialTitle = "",
            initialSubject = DefaultSubjects.first(),
            onDismiss = { showCreateDialog = false },
            onSave = { title, subject ->
                showCreateDialog = false
                scope.launch {
                    val newId = onCreateQuiz(title, subject)
                    onOpenQuizEditor(newId)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizEditorScreen(
    quizId: Long,
    loadQuiz: suspend (Long) -> QuizEntity?,
    observeQuestions: (Long) -> Flow<List<QuizQuestionEntity>>,
    onUpdateQuizMeta: suspend (Long, String, String) -> Long,
    onSaveQuestion: (QuizQuestionEntity) -> Unit,
    onDeleteQuestion: (QuizQuestionEntity) -> Unit,
    onStartQuiz: (Long) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var quiz by remember { mutableStateOf<QuizEntity?>(null) }
    val questions by observeQuestions(quizId).collectAsStateWithLifecycle(initialValue = emptyList())

    var showEditMeta by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<QuizQuestionEntity?>(null) }
    var showQuestionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(quizId) {
        quiz = loadQuiz(quizId)
    }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(quiz?.title ?: "Quiz Maker", style = MaterialTheme.typography.titleLarge, color = NavyHeading)
                        Text(quiz?.subject ?: "", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyHeading)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditMeta = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit quiz info", tint = SkyBlueAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIceBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingQuestion = null
                    showQuestionDialog = true
                },
                containerColor = PastelBluePrimary,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_question_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Question")
            }
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (questions.isNotEmpty()) {
                    PastelPrimaryButton(
                        text = "Take Quiz (${questions.size} Questions)",
                        icon = Icons.Rounded.PlayArrow,
                        onClick = { onStartQuiz(quizId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (questions.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Rounded.Quiz,
                        title = "No questions added yet",
                        subtitle = "Tap + to add Multiple-Choice or True/False questions with explanations.",
                        actionLabel = "Add Question",
                        onAction = {
                            editingQuestion = null
                            showQuestionDialog = true
                        }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(questions.indices.toList(), key = { questions[it].id }) { idx ->
                            val q = questions[idx]
                            val options = StudyRepository.decodeStringList(q.optionsJson)
                            StudyCard(contentPadding = PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Q${idx + 1}. ${q.questionText}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = NavyHeading
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        options.forEachIndexed { oIdx, opt ->
                                            val isCorrect = oIdx == q.correctIndex
                                            Text(
                                                text = if (isCorrect) "✓ $opt" else "• $opt",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isCorrect) SoftMintSuccess else MutedBlueGray,
                                                fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                        if (q.explanation.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Explanation: ${q.explanation}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SkyBlueAccent
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        editingQuestion = q
                                        showQuestionDialog = true
                                    }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit question", tint = SkyBlueAccent)
                                    }
                                    IconButton(onClick = { onDeleteQuestion(q) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete question", tint = MutedBlueGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditMeta && quiz != null) {
        QuizMetaDialog(
            initialTitle = quiz!!.title,
            initialSubject = quiz!!.subject,
            onDismiss = { showEditMeta = false },
            onSave = { title, subject ->
                showEditMeta = false
                scope.launch {
                    onUpdateQuizMeta(quizId, title, subject)
                    quiz = loadQuiz(quizId)
                }
            }
        )
    }

    if (showQuestionDialog) {
        QuestionEditorDialog(
            quizId = quizId,
            initial = editingQuestion,
            onDismiss = { showQuestionDialog = false },
            onSave = { saved ->
                onSaveQuestion(saved)
                showQuestionDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeQuizScreen(
    quizId: Long,
    loadQuiz: suspend (Long) -> QuizEntity?,
    loadQuestions: suspend (Long) -> List<QuizQuestionEntity>,
    onSubmitQuiz: suspend (QuizEntity, List<QuizQuestionEntity>, List<Int>) -> Long,
    onQuizCompleted: (Long) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var quiz by remember { mutableStateOf<QuizEntity?>(null) }
    var questions by remember { mutableStateOf<List<QuizQuestionEntity>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateListOf<Int>() }

    LaunchedEffect(quizId) {
        quiz = loadQuiz(quizId)
        val loaded = loadQuestions(quizId)
        questions = loaded
        selectedAnswers.clear()
        repeat(loaded.size) { selectedAnswers.add(-1) }
    }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = { Text(quiz?.title ?: "Take Quiz", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Exit quiz", tint = NavyHeading)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIceBackground)
            )
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (questions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Rounded.Quiz,
                    title = "Loading questions...",
                    subtitle = "If this quiz has no questions, go back and add some first.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val q = questions[currentIndex]
                val options = remember(q.id, q.optionsJson) {
                    StudyRepository.decodeStringList(q.optionsJson)
                }
                val selectedOption = selectedAnswers.getOrElse(currentIndex) { -1 }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${currentIndex + 1} of ${questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = SkyBlueAccent
                        )
                        SubjectBadge(quiz?.subject ?: "General")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PastelBluePrimary,
                        trackColor = BabyBlueContainer
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    StudyCard(contentPadding = PaddingValues(18.dp)) {
                        Text(
                            text = q.questionText,
                            style = MaterialTheme.typography.titleLarge,
                            color = NavyHeading
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        options.forEachIndexed { idx, opt ->
                            val isSelected = selectedOption == idx
                            Surface(
                                onClick = { selectedAnswers[currentIndex] = idx },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_option_$idx"),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) BabyBlueContainer else PureWhiteSurface,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PastelBluePrimary else CardBorderBlue
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedAnswers[currentIndex] = idx }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = opt,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = NavyHeading,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PastelOutlinedButton(
                            text = "Previous",
                            enabled = currentIndex > 0,
                            onClick = { if (currentIndex > 0) currentIndex-- },
                            modifier = Modifier.weight(1f)
                        )
                        if (currentIndex < questions.lastIndex) {
                            PastelPrimaryButton(
                                text = "Next",
                                onClick = { currentIndex++ },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            PastelPrimaryButton(
                                text = "Submit Quiz",
                                onClick = {
                                    val targetQuiz = quiz ?: return@PastelPrimaryButton
                                    scope.launch {
                                        val attemptId = onSubmitQuiz(targetQuiz, questions, selectedAnswers.toList())
                                        onQuizCompleted(attemptId)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("submit_quiz_button")
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizResultsScreen(
    attemptId: Long,
    loadAttempt: suspend (Long) -> QuizAttemptEntity?,
    loadQuestions: suspend (Long) -> List<QuizQuestionEntity>,
    onRetakeQuiz: (Long) -> Unit,
    onBack: () -> Unit
) {
    var attempt by remember { mutableStateOf<QuizAttemptEntity?>(null) }
    var questions by remember { mutableStateOf<List<QuizQuestionEntity>>(emptyList()) }

    LaunchedEffect(attemptId) {
        val loadedAttempt = loadAttempt(attemptId)
        attempt = loadedAttempt
        if (loadedAttempt != null) {
            questions = loadQuestions(loadedAttempt.quizId)
        }
    }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = { Text("Quiz Results", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyHeading)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIceBackground)
            )
        }
    ) { innerPadding ->
        ResponsiveScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val currentAttempt = attempt
            if (currentAttempt == null) {
                EmptyStateView(
                    icon = Icons.Rounded.Quiz,
                    title = "Loading results...",
                    subtitle = "Fetching your score and answer breakdown.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val chosenIndices = remember(currentAttempt.answersJson) {
                    StudyRepository.decodeIntList(currentAttempt.answersJson)
                }
                val incorrect = (currentAttempt.totalQuestions - currentAttempt.score).coerceAtLeast(0)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StudyCard(contentPadding = PaddingValues(20.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentAttempt.quizTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NavyHeading
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${currentAttempt.percentage}%",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = SkyBlueAccent
                                )
                                Text(
                                    text = "Score: ${currentAttempt.score} / ${currentAttempt.totalQuestions}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NavyHeading
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = SoftMintContainer
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("${currentAttempt.score}", style = MaterialTheme.typography.titleLarge, color = SoftMintSuccess)
                                            Text("Correct", style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = SoftCoralContainer
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("$incorrect", style = MaterialTheme.typography.titleLarge, color = SoftCoralUrgent)
                                            Text("Incorrect", style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                PastelPrimaryButton(
                                    text = "Retake Quiz",
                                    icon = Icons.Rounded.Refresh,
                                    onClick = { onRetakeQuiz(currentAttempt.quizId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("retake_quiz_button")
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Answer Breakdown & Explanations",
                            style = MaterialTheme.typography.titleLarge,
                            color = NavyHeading
                        )
                    }

                    items(questions.indices.toList()) { idx ->
                        val q = questions[idx]
                        val options = StudyRepository.decodeStringList(q.optionsJson)
                        val chosen = chosenIndices.getOrElse(idx) { -1 }
                        val isCorrect = chosen == q.correctIndex

                        StudyCard(contentPadding = PaddingValues(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    contentDescription = if (isCorrect) "Correct" else "Incorrect",
                                    tint = if (isCorrect) SoftMintSuccess else SoftCoralUrgent
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Question ${idx + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NavyHeading
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(q.questionText, style = MaterialTheme.typography.bodyLarge, color = NavyHeading)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your answer: ${options.getOrNull(chosen) ?: "Not answered"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) SoftMintSuccess else SoftCoralUrgent
                            )
                            Text(
                                text = "Correct answer: ${options.getOrNull(q.correctIndex) ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SoftMintSuccess
                            )
                            if (q.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SoftSkyTint
                                ) {
                                    Text(
                                        text = "Explanation: ${q.explanation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NavyHeading,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizMetaDialog(
    initialTitle: String,
    initialSubject: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var subject by remember { mutableStateOf(initialSubject) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteSurface,
        title = {
            Text(
                if (initialTitle.isBlank()) "Create Quiz" else "Edit Quiz Details",
                style = MaterialTheme.typography.titleLarge,
                color = NavyHeading
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMsg = null
                    },
                    label = { Text("Quiz Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultSubjects.forEach { preset ->
                        FilterChip(
                            selected = subject.equals(preset, ignoreCase = true),
                            onClick = { subject = preset },
                            label = { Text(preset) }
                        )
                    }
                }
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = SoftCoralUrgent)
                }
            }
        },
        confirmButton = {
            PastelPrimaryButton(
                text = "Save",
                onClick = {
                    if (title.isBlank()) {
                        errorMsg = "Please enter a quiz title."
                        return@PastelPrimaryButton
                    }
                    onSave(title, subject.ifBlank { "General" })
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedBlueGray) }
        }
    )
}

@Composable
private fun QuestionEditorDialog(
    quizId: Long,
    initial: QuizQuestionEntity?,
    onDismiss: () -> Unit,
    onSave: (QuizQuestionEntity) -> Unit
) {
    val initialOptions = remember(initial) {
        if (initial != null) StudyRepository.decodeStringList(initial.optionsJson)
        else listOf("", "", "", "")
    }
    var questionText by remember { mutableStateOf(initial?.questionText ?: "") }
    var isTrueFalse by remember {
        mutableStateOf(initial?.questionType == QuestionType.TRUE_FALSE.name)
    }
    var opt0 by remember { mutableStateOf(initialOptions.getOrElse(0) { "" }) }
    var opt1 by remember { mutableStateOf(initialOptions.getOrElse(1) { "" }) }
    var opt2 by remember { mutableStateOf(initialOptions.getOrElse(2) { "" }) }
    var opt3 by remember { mutableStateOf(initialOptions.getOrElse(3) { "" }) }
    var correctIndex by remember { mutableIntStateOf(initial?.correctIndex ?: 0) }
    var explanation by remember { mutableStateOf(initial?.explanation ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteSurface,
        title = {
            Text(
                if (initial == null) "Add Question" else "Edit Question",
                style = MaterialTheme.typography.titleLarge,
                color = NavyHeading
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isTrueFalse,
                        onClick = {
                            isTrueFalse = false
                            correctIndex = correctIndex.coerceIn(0, 3)
                        },
                        label = { Text("Multiple Choice") }
                    )
                    FilterChip(
                        selected = isTrueFalse,
                        onClick = {
                            isTrueFalse = true
                            correctIndex = correctIndex.coerceIn(0, 1)
                        },
                        label = { Text("True / False") }
                    )
                }

                OutlinedTextField(
                    value = questionText,
                    onValueChange = {
                        questionText = it
                        errorMsg = null
                    },
                    label = { Text("Question *") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isTrueFalse) {
                    Text("Select Correct Answer", style = MaterialTheme.typography.labelLarge, color = NavyHeading)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("True", "False").forEachIndexed { idx, label ->
                            FilterChip(
                                selected = correctIndex == idx,
                                onClick = { correctIndex = idx },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PastelBluePrimary,
                                    selectedLabelColor = PureWhiteSurface
                                )
                            )
                        }
                    }
                } else {
                    Text("Answer Choices (tap circle for correct)", style = MaterialTheme.typography.labelLarge, color = NavyHeading)
                    val choices = listOf(opt0, opt1, opt2, opt3)
                    val setters = listOf<(String) -> Unit>(
                        { opt0 = it },
                        { opt1 = it },
                        { opt2 = it },
                        { opt3 = it }
                    )
                    choices.forEachIndexed { idx, value ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = correctIndex == idx,
                                onClick = { correctIndex = idx }
                            )
                            OutlinedTextField(
                                value = value,
                                onValueChange = setters[idx],
                                label = { Text("Choice ${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Explanation (shown after submission)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = SoftCoralUrgent)
                }
            }
        },
        confirmButton = {
            PastelPrimaryButton(
                text = "Save Question",
                onClick = {
                    if (questionText.isBlank()) {
                        errorMsg = "Please enter the question text."
                        return@PastelPrimaryButton
                    }
                    val finalOptions = if (isTrueFalse) {
                        listOf("True", "False")
                    } else {
                        listOf(opt0, opt1, opt2, opt3).map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    if (finalOptions.size < 2) {
                        errorMsg = "Please provide at least 2 answer choices."
                        return@PastelPrimaryButton
                    }
                    val safeCorrect = correctIndex.coerceIn(0, finalOptions.lastIndex)
                    onSave(
                        QuizQuestionEntity(
                            id = initial?.id ?: 0L,
                            quizId = quizId,
                            questionText = questionText.trim(),
                            questionType = if (isTrueFalse) QuestionType.TRUE_FALSE.name else QuestionType.MULTIPLE_CHOICE.name,
                            optionsJson = StudyRepository.encodeStringList(finalOptions),
                            correctIndex = safeCorrect,
                            explanation = explanation.trim()
                        )
                    )
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedBlueGray) }
        }
    )
}
