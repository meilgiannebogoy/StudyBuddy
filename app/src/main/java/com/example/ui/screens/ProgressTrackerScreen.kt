package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.CardStatus
import com.example.data.local.FlashcardEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.data.preferences.UserPreferences
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.SubjectBadge
import com.example.ui.components.formatDateTimeShort
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftAmberContainer
import com.example.ui.theme.SoftAmberWarning
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftMintContainer
import com.example.ui.theme.SoftMintSuccess
import com.example.ui.theme.SoftSkyTint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class DailyStudyBucket(
    val dayLabel: String,
    val minutes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackerScreen(
    preferences: UserPreferences,
    tasks: List<TaskEntity>,
    attempts: List<QuizAttemptEntity>,
    flashcards: List<FlashcardEntity>,
    sessions: List<StudySessionEntity>,
    onBack: () -> Unit,
    onOpenAttemptReview: (Long) -> Unit
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val taskPct = if (totalTasks == 0) 0 else ((completedTasks.toFloat() / totalTasks) * 100f).toInt()

    val avgQuizScore = remember(attempts) {
        if (attempts.isEmpty()) 0 else attempts.map { it.percentage }.average().toInt()
    }

    val totalCards = flashcards.size
    val reviewedCards = flashcards.count { it.status != CardStatus.NEW.name }
    val masteredCards = flashcards.count { it.status == CardStatus.MASTERED.name }
    val difficultCards = flashcards.count { it.status == CardStatus.NEEDS_PRACTICE.name }

    val focusSessions = remember(sessions) { sessions.filter { it.sessionType == "FOCUS" } }
    val totalFocusMinutes = remember(focusSessions) { focusSessions.sumOf { it.durationMinutes } }

    val weeklyBuckets = remember(focusSessions) {
        val labelFmt = SimpleDateFormat("EEE", Locale.getDefault())
        (6 downTo 0).map { daysAgo ->
            val startCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startMs = startCal.timeInMillis
            val endMs = startMs + 24 * 60 * 60 * 1000L
            val mins = focusSessions
                .filter { it.completedAtMillis in startMs until endMs }
                .sumOf { it.durationMinutes }
            DailyStudyBucket(
                dayLabel = labelFmt.format(Date(startMs)),
                minutes = mins
            )
        }
    }

    val todayMinutes = weeklyBuckets.lastOrNull()?.minutes ?: 0
    val goalProgress = (todayMinutes.toFloat() / preferences.dailyStudyGoalMinutes.coerceAtLeast(1)).coerceIn(0f, 1f)

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = { Text("Progress Tracker", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Task Completion Statistics
                StudyCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tasks Overview", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                        Text("$taskPct% complete", style = MaterialTheme.typography.titleSmall, color = SkyBlueAccent)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { taskPct / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = PastelBluePrimary,
                        trackColor = BabyBlueContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricTile("Total", "$totalTasks", SoftSkyTint, NavyHeading, Modifier.weight(1f))
                        MetricTile("Completed", "$completedTasks", SoftMintContainer, SoftMintSuccess, Modifier.weight(1f))
                        MetricTile("Pending", "$pendingTasks", BabyBlueContainer, SkyBlueAccent, Modifier.weight(1f))
                    }
                }

                // Daily & Weekly Study Time Chart
                StudyCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Weekly Study Focus", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                            Text(
                                "Today: ${todayMinutes}m / ${preferences.dailyStudyGoalMinutes}m goal • ${focusSessions.size} sessions (${totalFocusMinutes}m total)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedBlueGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SkyBlueAccent,
                        trackColor = BabyBlueContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 7-Day Bar Chart
                    val maxMins = (weeklyBuckets.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(30)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val barCount = weeklyBuckets.size
                        val slotWidth = size.width / barCount
                        val barWidth = (slotWidth * 0.52f).coerceAtMost(36.dp.toPx())
                        weeklyBuckets.forEachIndexed { idx, bucket ->
                            val ratio = (bucket.minutes.toFloat() / maxMins).coerceIn(0.05f, 1f)
                            val barHeight = if (bucket.minutes == 0) 6.dp.toPx() else size.height * ratio
                            val left = idx * slotWidth + (slotWidth - barWidth) / 2f
                            val top = size.height - barHeight
                            drawRoundRect(
                                color = if (bucket.minutes > 0) PastelBluePrimary else BabyBlueContainer,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weeklyBuckets.forEach { bucket ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(bucket.dayLabel, style = MaterialTheme.typography.labelSmall, color = MutedBlueGray, textAlign = TextAlign.Center)
                                Text("${bucket.minutes}m", style = MaterialTheme.typography.labelSmall, color = SkyBlueAccent)
                            }
                        }
                    }
                }

                // Flashcards Mastery
                StudyCard(contentPadding = PaddingValues(16.dp)) {
                    Text("Flashcard Progress", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                    Spacer(modifier = Modifier.height(10.dp))
                    val masteryRatio = if (totalCards == 0) 0f else masteredCards.toFloat() / totalCards
                    LinearProgressIndicator(
                        progress = { masteryRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = SoftMintSuccess,
                        trackColor = BabyBlueContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricTile("Reviewed", "$reviewedCards/$totalCards", SoftSkyTint, SkyBlueAccent, Modifier.weight(1f))
                        MetricTile("Mastered", "$masteredCards", SoftMintContainer, SoftMintSuccess, Modifier.weight(1f))
                        MetricTile("Practice", "$difficultCards", SoftAmberContainer, SoftAmberWarning, Modifier.weight(1f))
                    }
                }

                // Quiz Performance History
                StudyCard(contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quiz Performance History", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                        if (attempts.isNotEmpty()) {
                            Text("Avg: $avgQuizScore%", style = MaterialTheme.typography.titleSmall, color = SkyBlueAccent)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (attempts.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.BarChart,
                            title = "No quiz scores yet",
                            subtitle = "Take a quiz to see your score history and improvement over time."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            attempts.take(10).forEach { attempt ->
                                Surface(
                                    onClick = { onOpenAttemptReview(attempt.id) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = SoftSkyTint
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(attempt.quizTitle, style = MaterialTheme.typography.titleSmall, color = NavyHeading)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                SubjectBadge(attempt.subject)
                                                Text(formatDateTimeShort(attempt.completedAtMillis), style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${attempt.score}/${attempt.totalQuestions} (${attempt.percentage}%)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (attempt.percentage >= 70) SoftMintSuccess else SkyBlueAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = bg) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = fg)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
        }
    }
}
