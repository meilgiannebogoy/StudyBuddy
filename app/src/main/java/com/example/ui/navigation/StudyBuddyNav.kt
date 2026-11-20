package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.AppContainer
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.DeckDetailScreen
import com.example.ui.screens.FlashcardDecksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreHubScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NotesListScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ProgressTrackerScreen
import com.example.ui.screens.QuizEditorScreen
import com.example.ui.screens.QuizListScreen
import com.example.ui.screens.QuizResultsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.StudyFlashcardsScreen
import com.example.ui.screens.TakeQuizScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.LearningToolsViewModel
import com.example.ui.viewmodel.StudyPlannerViewModel
import com.example.ui.viewmodel.StudyTimerViewModel

object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val SCHEDULE = "schedule"
    const val TIMER = "timer"
    const val MORE = "more"

    const val NOTES = "notes"
    const val NOTE_EDITOR = "note_editor/{noteId}"
    const val QUIZZES = "quizzes"
    const val QUIZ_EDITOR = "quiz_editor/{quizId}"
    const val QUIZ_TAKE = "quiz_take/{quizId}"
    const val QUIZ_RESULTS = "quiz_results/{attemptId}"
    const val FLASHCARDS = "flashcards"
    const val DECK_DETAIL = "deck_detail/{deckId}"
    const val FLASHCARD_STUDY = "flashcard_study/{deckId}/{difficultOnly}"
    const val AI_ASSISTANT = "ai_assistant"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"

    fun noteEditor(noteId: Long) = "note_editor/$noteId"
    fun quizEditor(quizId: Long) = "quiz_editor/$quizId"
    fun quizTake(quizId: Long) = "quiz_take/$quizId"
    fun quizResults(attemptId: Long) = "quiz_results/$attemptId"
    fun deckDetail(deckId: Long) = "deck_detail/$deckId"
    fun flashcardStudy(deckId: Long, difficultOnly: Boolean) = "flashcard_study/$deckId/$difficultOnly"
}

private data class BottomTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelTabs = listOf(
    BottomTabItem(Routes.HOME, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    BottomTabItem(Routes.TASKS, "Tasks", Icons.AutoMirrored.Rounded.Assignment, Icons.AutoMirrored.Rounded.Assignment, "nav_tasks"),
    BottomTabItem(Routes.SCHEDULE, "Schedule", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_schedule"),
    BottomTabItem(Routes.TIMER, "Timer", Icons.Rounded.Timer, Icons.Outlined.Timer, "nav_timer"),
    BottomTabItem(Routes.MORE, "More", Icons.Rounded.GridView, Icons.Outlined.GridView, "nav_more")
)

@Composable
fun StudyBuddyApp(container: AppContainer) {
    val navController = rememberNavController()

    val plannerVm: StudyPlannerViewModel = viewModel(
        factory = StudyPlannerViewModel.Factory(
            container.studyRepository,
            container.userPreferencesRepository,
            container.notificationHelper
        )
    )
    val timerVm: StudyTimerViewModel = viewModel(
        factory = StudyTimerViewModel.Factory(
            container.studyRepository,
            container.userPreferencesRepository,
            container.notificationHelper
        )
    )
    val learningVm: LearningToolsViewModel = viewModel(
        factory = LearningToolsViewModel.Factory(container.studyRepository)
    )
    val aiVm: AiAssistantViewModel = viewModel(
        factory = AiAssistantViewModel.Factory(container.aiStudyRepository, container.studyRepository)
    )

    val prefs by plannerVm.userPreferences.collectAsStateWithLifecycle()
    val tasks by plannerVm.tasks.collectAsStateWithLifecycle()
    val classes by plannerVm.classes.collectAsStateWithLifecycle()
    val notes by plannerVm.notes.collectAsStateWithLifecycle()
    val sessions by plannerVm.sessions.collectAsStateWithLifecycle()
    val timerState by timerVm.uiState.collectAsStateWithLifecycle()
    val quizzes by learningVm.quizzes.collectAsStateWithLifecycle()
    val allQuestions by learningVm.allQuizQuestions.collectAsStateWithLifecycle()
    val quizAttempts by learningVm.quizAttempts.collectAsStateWithLifecycle()
    val decks by learningVm.decks.collectAsStateWithLifecycle()
    val allFlashcards by learningVm.allFlashcards.collectAsStateWithLifecycle()
    val aiState by aiVm.uiState.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val topLevelRoutes = remember { TopLevelTabs.map { it.route }.toSet() }
    val showTopLevelNav = currentDestination?.route in topLevelRoutes

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftIceBackground)
    ) {
        val useNavRail = maxWidth >= 720.dp

        Scaffold(
            containerColor = SoftIceBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showTopLevelNav && !useNavRail) {
                    NavigationBar(
                        containerColor = PureWhiteSurface,
                        tonalElevation = 4.dp
                    ) {
                        TopLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SkyBlueAccent,
                                    selectedTextColor = NavyHeading,
                                    indicatorColor = BabyBlueContainer,
                                    unselectedIconColor = MutedBlueGray,
                                    unselectedTextColor = MutedBlueGray
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelNav && useNavRail) {
                    NavigationRail(
                        containerColor = PureWhiteSurface
                    ) {
                        TopLevelTabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = SkyBlueAccent,
                                    selectedTextColor = NavyHeading,
                                    indicatorColor = BabyBlueContainer,
                                    unselectedIconColor = MutedBlueGray,
                                    unselectedTextColor = MutedBlueGray
                                ),
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            preferences = prefs,
                            tasks = tasks,
                            sessions = sessions,
                            onToggleTask = plannerVm::toggleTaskCompletion,
                            onNavigateToTasks = { navigateTopLevel(Routes.TASKS) },
                            onNavigateToSchedule = { navigateTopLevel(Routes.SCHEDULE) },
                            onNavigateToTimer = { navigateTopLevel(Routes.TIMER) },
                            onNavigateToNotes = { navController.navigate(Routes.NOTES) },
                            onNavigateToQuizzes = { navController.navigate(Routes.QUIZZES) },
                            onNavigateToFlashcards = { navController.navigate(Routes.FLASHCARDS) },
                            onNavigateToProgress = { navController.navigate(Routes.PROGRESS) },
                            onNavigateToAiAssistant = { navController.navigate(Routes.AI_ASSISTANT) }
                        )
                    }

                    composable(Routes.TASKS) {
                        TasksScreen(
                            tasks = tasks,
                            onSaveTask = plannerVm::saveTask,
                            onToggleTask = plannerVm::toggleTaskCompletion,
                            onDeleteTask = plannerVm::deleteTask
                        )
                    }

                    composable(Routes.SCHEDULE) {
                        ScheduleScreen(
                            classes = classes,
                            onSaveClasses = plannerVm::saveClassesForDays,
                            onDeleteClass = plannerVm::deleteClass
                        )
                    }

                    composable(Routes.TIMER) {
                        TimerScreen(
                            uiState = timerState,
                            onStart = timerVm::startTimer,
                            onPause = timerVm::pauseTimer,
                            onReset = timerVm::resetTimer,
                            onSwitchMode = timerVm::switchMode,
                            onSelectSubject = timerVm::selectSubject,
                            onUpdateDurations = timerVm::updateDurations,
                            onDismissBanner = timerVm::dismissBanner
                        )
                    }

                    composable(Routes.MORE) {
                        MoreHubScreen(
                            notesCount = notes.size,
                            quizzesCount = quizzes.size,
                            decksCount = decks.size,
                            onOpenNotes = { navController.navigate(Routes.NOTES) },
                            onOpenQuizzes = { navController.navigate(Routes.QUIZZES) },
                            onOpenFlashcards = { navController.navigate(Routes.FLASHCARDS) },
                            onOpenAiAssistant = { navController.navigate(Routes.AI_ASSISTANT) },
                            onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                            onOpenProfile = { navController.navigate(Routes.PROFILE) }
                        )
                    }

                    composable(Routes.NOTES) {
                        NotesListScreen(
                            notes = notes,
                            onBack = { navController.popBackStack() },
                            onOpenNote = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                            onDeleteNote = plannerVm::deleteNote
                        )
                    }

                    composable(
                        route = Routes.NOTE_EDITOR,
                        arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                        NoteEditorScreen(
                            noteId = noteId,
                            loadNote = plannerVm::getNoteById,
                            saveNote = plannerVm::saveNote,
                            onDeleteNote = plannerVm::deleteNote,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.QUIZZES) {
                        QuizListScreen(
                            quizzes = quizzes,
                            allQuestions = allQuestions,
                            attempts = quizAttempts,
                            onBack = { navController.popBackStack() },
                            onCreateQuiz = { title, subject ->
                                learningVm.createOrUpdateQuiz(0L, title, subject)
                            },
                            onOpenQuizEditor = { quizId -> navController.navigate(Routes.quizEditor(quizId)) },
                            onStartQuiz = { quizId -> navController.navigate(Routes.quizTake(quizId)) },
                            onReviewAttempt = { attemptId -> navController.navigate(Routes.quizResults(attemptId)) },
                            onDeleteQuiz = learningVm::deleteQuiz
                        )
                    }

                    composable(
                        route = Routes.QUIZ_EDITOR,
                        arguments = listOf(navArgument("quizId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val quizId = backStackEntry.arguments?.getLong("quizId") ?: 0L
                        QuizEditorScreen(
                            quizId = quizId,
                            loadQuiz = learningVm::getQuizById,
                            observeQuestions = learningVm::observeQuestionsForQuiz,
                            onUpdateQuizMeta = learningVm::createOrUpdateQuiz,
                            onSaveQuestion = learningVm::saveQuestion,
                            onDeleteQuestion = learningVm::deleteQuestion,
                            onStartQuiz = { id -> navController.navigate(Routes.quizTake(id)) },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Routes.QUIZ_TAKE,
                        arguments = listOf(navArgument("quizId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val quizId = backStackEntry.arguments?.getLong("quizId") ?: 0L
                        TakeQuizScreen(
                            quizId = quizId,
                            loadQuiz = learningVm::getQuizById,
                            loadQuestions = learningVm::getQuestionsForQuiz,
                            onSubmitQuiz = learningVm::submitQuizAttempt,
                            onQuizCompleted = { attemptId ->
                                navController.navigate(Routes.quizResults(attemptId)) {
                                    popUpTo(Routes.quizTake(quizId)) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Routes.QUIZ_RESULTS,
                        arguments = listOf(navArgument("attemptId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val attemptId = backStackEntry.arguments?.getLong("attemptId") ?: 0L
                        QuizResultsScreen(
                            attemptId = attemptId,
                            loadAttempt = learningVm::getAttemptById,
                            loadQuestions = learningVm::getQuestionsForQuiz,
                            onRetakeQuiz = { qId ->
                                navController.navigate(Routes.quizTake(qId)) {
                                    popUpTo(Routes.quizResults(attemptId)) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.FLASHCARDS) {
                        FlashcardDecksScreen(
                            decks = decks,
                            allCards = allFlashcards,
                            onBack = { navController.popBackStack() },
                            onCreateDeck = { title, subject ->
                                learningVm.createOrUpdateDeck(0L, title, subject)
                            },
                            onOpenDeckEditor = { deckId -> navController.navigate(Routes.deckDetail(deckId)) },
                            onStudyDeck = { deckId, difficultOnly ->
                                navController.navigate(Routes.flashcardStudy(deckId, difficultOnly))
                            },
                            onDeleteDeck = learningVm::deleteDeck
                        )
                    }

                    composable(
                        route = Routes.DECK_DETAIL,
                        arguments = listOf(navArgument("deckId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
                        DeckDetailScreen(
                            deckId = deckId,
                            loadDeck = learningVm::getDeckById,
                            observeCards = learningVm::observeCardsForDeck,
                            onUpdateDeckMeta = learningVm::createOrUpdateDeck,
                            onSaveCard = learningVm::saveFlashcard,
                            onDeleteCard = learningVm::deleteFlashcard,
                            onResetProgress = learningVm::resetDeckProgress,
                            onStudyDeck = { id, difficultOnly ->
                                navController.navigate(Routes.flashcardStudy(id, difficultOnly))
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Routes.FLASHCARD_STUDY,
                        arguments = listOf(
                            navArgument("deckId") { type = NavType.LongType },
                            navArgument("difficultOnly") { type = NavType.BoolType }
                        )
                    ) { backStackEntry ->
                        val deckId = backStackEntry.arguments?.getLong("deckId") ?: 0L
                        val difficultOnly = backStackEntry.arguments?.getBoolean("difficultOnly") ?: false
                        StudyFlashcardsScreen(
                            deckId = deckId,
                            difficultOnly = difficultOnly,
                            loadDeck = learningVm::getDeckById,
                            observeCards = learningVm::observeCardsForDeck,
                            onMarkCardStatus = learningVm::markCardStatus,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.AI_ASSISTANT) {
                        AiAssistantScreen(
                            uiState = aiState,
                            savedNotes = notes,
                            onBack = { navController.popBackStack() },
                            onSelectMode = aiVm::selectMode,
                            onUpdateSubject = aiVm::updateSubject,
                            onUpdateInput = aiVm::updateInputText,
                            onRunAi = aiVm::runAiTask,
                            onToggleQuestion = aiVm::toggleQuizQuestionSelection,
                            onUpdateQuizTitle = aiVm::updateDraftQuizTitle,
                            onSaveQuiz = aiVm::saveReviewedQuizToLibrary,
                            onOpenSavedQuiz = { quizId -> navController.navigate(Routes.quizTake(quizId)) },
                            onToggleFlashcard = aiVm::toggleFlashcardSelection,
                            onUpdateDeckTitle = aiVm::updateDraftDeckTitle,
                            onSaveFlashcards = aiVm::saveReviewedFlashcardsToLibrary,
                            onOpenSavedDeck = { deckId -> navController.navigate(Routes.flashcardStudy(deckId, false)) },
                            onSaveTextAsNote = aiVm::saveTextResultAsNote
                        )
                    }

                    composable(Routes.PROGRESS) {
                        ProgressTrackerScreen(
                            preferences = prefs,
                            tasks = tasks,
                            attempts = quizAttempts,
                            flashcards = allFlashcards,
                            sessions = sessions,
                            onBack = { navController.popBackStack() },
                            onOpenAttemptReview = { attemptId ->
                                navController.navigate(Routes.quizResults(attemptId))
                            }
                        )
                    }

                    composable(Routes.PROFILE) {
                        ProfileSettingsScreen(
                            preferences = prefs,
                            hasNotificationPermission = container.notificationHelper::hasNotificationPermission,
                            onBack = { navController.popBackStack() },
                            onSaveProfile = plannerVm::updateProfile,
                            onSaveNotifications = plannerVm::updateNotificationSettings,
                            onSendTestNotification = plannerVm::sendTestNotification
                        )
                    }
                }
            }
        }
    }
}
