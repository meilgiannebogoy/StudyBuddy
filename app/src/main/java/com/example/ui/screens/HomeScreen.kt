package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.data.preferences.UserPreferences
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PriorityBadge
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.SectionHeader
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class QuickAccessItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tag: String,
    val onClick: () -> Unit
)

private val StudyTips = listOf(
    "Break big assignments into 25-minute Pomodoro sprints and celebrate every checkmark.",
    "Active recall beats passive rereading—turn your notes into flashcards or a quick 5-question quiz.",
    "Review your class notes within 24 hours of lecture to lock key concepts into long-term memory.",
    "Tackle your highest-priority task first while your focus energy is fresh.",
    "Use spaced repetition on cards marked 'Need More Practice' before moving to new chapters."
)

@Composable
fun HomeScreen(
    preferences: UserPreferences,
    tasks: List<TaskEntity>,
    sessions: List<StudySessionEntity>,
    onToggleTask: (TaskEntity) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToQuizzes: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToAiAssistant: () -> Unit
) {
    val now = System.currentTimeMillis()
    val pendingTasks = remember(tasks) {
        tasks.filter { !it.isCompleted }.sortedBy { it.dueDateMillis }
    }
    val completedTasksCount = remember(tasks) { tasks.count { it.isCompleted } }
    val totalTasksCount = tasks.size
    val completionRatio = if (totalTasksCount == 0) 0f else completedTasksCount.toFloat() / totalTasksCount
    val animatedProgress by animateFloatAsState(
        targetValue = completionRatio,
        animationSpec = tween(durationMillis = 600),
        label = "daily_progress"
    )

    val startOfDay = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayFocusMinutes = remember(sessions) {
        sessions.filter { it.sessionType == "FOCUS" && it.completedAtMillis >= startOfDay }
            .sumOf { it.durationMinutes }
    }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
    val formattedToday = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }
    var tipIndex by remember { mutableIntStateOf(0) }

    val quickAccessItems = remember(
        onNavigateToTasks,
        onNavigateToSchedule,
        onNavigateToTimer,
        onNavigateToNotes,
        onNavigateToQuizzes,
        onNavigateToFlashcards,
        onNavigateToProgress,
        onNavigateToAiAssistant
    ) {
        listOf(
            QuickAccessItem("My Tasks", "${pendingTasks.size} pending", Icons.AutoMirrored.Rounded.Assignment, "qa_tasks", onNavigateToTasks),
            QuickAccessItem("Class Schedule", "Weekly timetable", Icons.Rounded.CalendarMonth, "qa_schedule", onNavigateToSchedule),
            QuickAccessItem("Study Timer", "Pomodoro focus", Icons.Rounded.Timer, "qa_timer", onNavigateToTimer),
            QuickAccessItem("My Notes", "Auto-saved", Icons.Rounded.Description, "qa_notes", onNavigateToNotes),
            QuickAccessItem("Quiz Maker", "Create & test", Icons.Rounded.Quiz, "qa_quizzes", onNavigateToQuizzes),
            QuickAccessItem("Flashcards", "Flip & master", Icons.Rounded.Style, "qa_flashcards", onNavigateToFlashcards),
            QuickAccessItem("Progress Tracker", "Charts & stats", Icons.Rounded.BarChart, "qa_progress", onNavigateToProgress),
            QuickAccessItem("AI Assistant", "Smart study help", Icons.Rounded.AutoAwesome, "qa_ai", onNavigateToAiAssistant)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftIceBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        ResponsiveScreenContainer {
            // Welcome Hero Card
            StudyCard(
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFDDEEFF), Color(0xFFF5FAFF))
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.25f)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PureWhiteSurface.copy(alpha = 0.9f),
                                border = BorderStroke(1.dp, CardBorderBlue)
                            ) {
                                Text(
                                    text = formattedToday,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SkyBlueAccent,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "$greeting, ${preferences.studentName}!",
                                style = MaterialTheme.typography.headlineMedium,
                                color = NavyHeading
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ready for a calm, productive study session?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedBlueGray
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Image(
                            painter = painterResource(id = R.drawable.img_study_hero),
                            contentDescription = "StudyBuddy workspace illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(0.95f)
                                .height(108.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Task Completion & Stats Card
            StudyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(86.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(86.dp)) {
                            val strokeWidth = 9.dp.toPx()
                            drawArc(
                                color = BabyBlueContainer,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = PastelBluePrimary,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "${(completionRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NavyHeading
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Task Progress",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyHeading
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatPill(
                                label = "Pending",
                                value = "${pendingTasks.size}",
                                container = SoftSkyTint,
                                valueColor = SkyBlueAccent,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "Done",
                                value = "$completedTasksCount",
                                container = SoftMintContainer,
                                valueColor = SoftMintSuccess,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "Focus",
                                value = "${todayFocusMinutes}m",
                                container = BabyBlueContainer,
                                valueColor = NavyHeading,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Upcoming Deadlines
            SectionHeader(
                title = "Upcoming Deadlines",
                subtitle = if (pendingTasks.isEmpty()) "All caught up!" else "Next due assignments",
                actionText = "See All",
                onActionClick = onNavigateToTasks
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (pendingTasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Rounded.CheckCircle,
                    title = "No pending assignments",
                    subtitle = "Add your upcoming homework, projects, or exams in My Tasks to stay ahead.",
                    actionLabel = "Add a Task",
                    onAction = onNavigateToTasks
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingTasks.take(3).forEach { task ->
                        val isOverdue = task.dueDateMillis < now
                        StudyCard(
                            onClick = onNavigateToTasks,
                            contentPadding = PaddingValues(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onToggleTask(task) },
                                    modifier = Modifier.testTag("home_toggle_task_${task.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.RadioButtonUnchecked,
                                        contentDescription = "Mark task completed",
                                        tint = PastelBluePrimary
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = NavyHeading,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SubjectBadge(task.subject)
                                        PriorityBadge(task.priority)
                                        if (isOverdue) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = SoftCoralContainer
                                            ) {
                                                Text(
                                                    text = "Overdue",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SoftCoralUrgent,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Due ${formatDateTimeShort(task.dueDateMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOverdue) SoftCoralUrgent else MutedBlueGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Quick Access Grid
            SectionHeader(
                title = "Quick Access",
                subtitle = "Jump straight into your study tools"
            )
            Spacer(modifier = Modifier.height(10.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth >= 560.dp) 4 else 2
                val rows = quickAccessItems.chunked(columns)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                StudyCard(
                                    onClick = item.onClick,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag(item.tag),
                                    contentPadding = PaddingValues(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BabyBlueContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = SkyBlueAccent,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = NavyHeading,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedBlueGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Study Tip Banner
            StudyCard(
                containerColor = SoftSkyTint
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PureWhiteSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = "Study tip",
                            tint = PastelBluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Study Tip",
                            style = MaterialTheme.typography.titleSmall,
                            color = NavyHeading
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = StudyTips[tipIndex],
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedBlueGray
                        )
                    }
                    IconButton(
                        onClick = { tipIndex = (tipIndex + 1) % StudyTips.size },
                        modifier = Modifier.testTag("next_study_tip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Next study tip",
                            tint = SkyBlueAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    container: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = container
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedBlueGray
            )
        }
    }
}
