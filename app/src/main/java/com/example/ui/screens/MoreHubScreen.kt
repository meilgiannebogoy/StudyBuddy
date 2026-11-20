package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftIceBackground

@Composable
fun MoreHubScreen(
    notesCount: Int,
    quizzesCount: Int,
    decksCount: Int,
    onOpenNotes: () -> Unit,
    onOpenQuizzes: () -> Unit,
    onOpenFlashcards: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftIceBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        ResponsiveScreenContainer {
            Text(
                text = "Study Tools & More",
                style = MaterialTheme.typography.headlineMedium,
                color = NavyHeading
            )
            Text(
                text = "Notes, quizzes, flashcards, AI tutor, and analytics",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedBlueGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Featured AI Study Assistant Card
            StudyCard(
                onClick = onOpenAiAssistant,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.testTag("more_ai_assistant_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFD9ECFF), Color(0xFFF3F9FF))
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = SkyBlueAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI STUDY ASSISTANT",
                                style = MaterialTheme.typography.labelSmall,
                                color = SkyBlueAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generate Quizzes, Flashcards & Summaries",
                            style = MaterialTheme.typography.titleMedium,
                            color = NavyHeading
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Paste your lesson text and review AI-drafted study materials.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedBlueGray
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter = painterResource(id = R.drawable.img_ai_companion),
                        contentDescription = "AI Study Companion illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MoreOptionCard(
                    title = "My Notes",
                    subtitle = "$notesCount saved notes • Auto-save & search",
                    icon = Icons.Rounded.Description,
                    tag = "more_notes_card",
                    onClick = onOpenNotes
                )
                MoreOptionCard(
                    title = "Quiz Maker & Take a Quiz",
                    subtitle = "$quizzesCount quizzes • Multiple choice, True/False & attempt history",
                    icon = Icons.Rounded.Quiz,
                    tag = "more_quizzes_card",
                    onClick = onOpenQuizzes
                )
                MoreOptionCard(
                    title = "My Flashcards",
                    subtitle = "$decksCount decks • Flip, shuffle & track mastery",
                    icon = Icons.Rounded.Style,
                    tag = "more_flashcards_card",
                    onClick = onOpenFlashcards
                )
                MoreOptionCard(
                    title = "Progress Tracker",
                    subtitle = "Tasks, quiz scores, flashcards & weekly study charts",
                    icon = Icons.Rounded.BarChart,
                    tag = "more_progress_card",
                    onClick = onOpenProgress
                )
                MoreOptionCard(
                    title = "Profile & Settings",
                    subtitle = "Display name, daily goal, reminders & notifications",
                    icon = Icons.Rounded.Person,
                    tag = "more_profile_card",
                    onClick = onOpenProfile
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MoreOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    StudyCard(
        onClick = onClick,
        modifier = Modifier.testTag(tag),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BabyBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = SkyBlueAccent, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Open $title",
                tint = MutedBlueGray
            )
        }
    }
}
