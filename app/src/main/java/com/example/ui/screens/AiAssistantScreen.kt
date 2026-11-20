package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.NoteEntity
import com.example.ui.components.DefaultSubjects
import com.example.ui.components.PastelOutlinedButton
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
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
import com.example.ui.viewmodel.AiAssistantUiState
import com.example.ui.viewmodel.AiResultState
import com.example.ui.viewmodel.AiStudyMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    uiState: AiAssistantUiState,
    savedNotes: List<NoteEntity>,
    onBack: () -> Unit,
    onSelectMode: (AiStudyMode) -> Unit,
    onUpdateSubject: (String) -> Unit,
    onUpdateInput: (String) -> Unit,
    onRunAi: () -> Unit,
    onToggleQuestion: (Int) -> Unit,
    onUpdateQuizTitle: (String) -> Unit,
    onSaveQuiz: () -> Unit,
    onOpenSavedQuiz: (Long) -> Unit,
    onToggleFlashcard: (Int) -> Unit,
    onUpdateDeckTitle: (String) -> Unit,
    onSaveFlashcards: () -> Unit,
    onOpenSavedDeck: (Long) -> Unit,
    onSaveTextAsNote: () -> Unit
) {
    var showNotePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("AI Study Assistant", style = MaterialTheme.typography.titleLarge, color = NavyHeading)
                },
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header card
                StudyCard(
                    containerColor = SoftSkyTint,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_ai_companion),
                            contentDescription = "AI Study Companion",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = uiState.selectedMode.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyHeading
                            )
                            Text(
                                text = uiState.selectedMode.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedBlueGray
                            )
                        }
                    }
                }

                // Mode Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiStudyMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.selectedMode == mode,
                            onClick = { onSelectMode(mode) },
                            label = { Text(mode.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface,
                                containerColor = PureWhiteSurface
                            ),
                            modifier = Modifier.testTag("ai_mode_${mode.name.lowercase()}")
                        )
                    }
                }

                // Subject Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultSubjects.forEach { subj ->
                        FilterChip(
                            selected = uiState.subject == subj,
                            onClick = { onUpdateSubject(subj) },
                            label = { Text(subj) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SkyBlueAccent,
                                selectedLabelColor = PureWhiteSurface,
                                containerColor = PureWhiteSurface
                            )
                        )
                    }
                }

                // Study Material Input
                StudyCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (uiState.selectedMode) {
                                AiStudyMode.EXPLAIN -> "Concept to Explain"
                                AiStudyMode.PRACTICE -> "Topic for Practice Questions"
                                else -> "Lesson Text / Study Notes"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = NavyHeading
                        )
                        if (savedNotes.isNotEmpty()) {
                            TextButton(onClick = { showNotePicker = true }) {
                                Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load from Notes")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = onUpdateInput,
                        placeholder = {
                            Text(
                                when (uiState.selectedMode) {
                                    AiStudyMode.EXPLAIN -> "e.g. How does photosynthesis convert light into chemical energy?"
                                    AiStudyMode.PRACTICE -> "e.g. Quadratic equations and factoring"
                                    else -> "Paste your lesson text or lecture notes here. StudyBuddy bases questions and summaries strictly on your text."
                                }
                            )
                        },
                        minLines = 5,
                        maxLines = 10,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SoftIceBackground,
                            unfocusedContainerColor = SoftIceBackground,
                            focusedBorderColor = PastelBluePrimary,
                            unfocusedBorderColor = CardBorderBlue
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_input_field")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PastelPrimaryButton(
                        text = "Generate with AI",
                        icon = Icons.Rounded.AutoAwesome,
                        enabled = uiState.resultState !is AiResultState.Loading,
                        onClick = onRunAi,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_generate_button")
                    )
                }

                if (uiState.savedNotice != null) {
                    StudyCard(containerColor = SoftMintContainer, contentPadding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SoftMintSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(uiState.savedNotice, style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                        }
                    }
                }

                // Result Rendering
                when (val result = uiState.resultState) {
                    AiResultState.Idle -> {}
                    AiResultState.Loading -> {
                        StudyCard(contentPadding = PaddingValues(24.dp)) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = PastelBluePrimary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "StudyBuddy AI is crafting your study materials...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MutedBlueGray
                                )
                            }
                        }
                    }

                    is AiResultState.StreamingText -> {
                        StudyCard(contentPadding = PaddingValues(16.dp)) {
                            Text("Generating...", style = MaterialTheme.typography.labelLarge, color = SkyBlueAccent)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.text, style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                        }
                    }

                    is AiResultState.TextSuccess -> {
                        StudyCard(contentPadding = PaddingValues(16.dp)) {
                            Text(
                                text = uiState.selectedMode.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyHeading
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(result.text, style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                            Spacer(modifier = Modifier.height(14.dp))
                            PastelOutlinedButton(
                                text = "Save to My Notes",
                                icon = Icons.Rounded.Save,
                                onClick = onSaveTextAsNote,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    is AiResultState.QuizDraftReady -> {
                        StudyCard(contentPadding = PaddingValues(16.dp)) {
                            Text(
                                text = "Review Generated Questions",
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyHeading
                            )
                            Text(
                                text = "Uncheck any question you don't want, then save to Quiz Maker.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedBlueGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = result.title,
                                onValueChange = onUpdateQuizTitle,
                                label = { Text("Quiz Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            result.questions.forEachIndexed { idx, q ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = SoftSkyTint,
                                    border = BorderStroke(1.dp, CardBorderBlue)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = q.isSelected,
                                            onCheckedChange = { onToggleQuestion(idx) }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Q${idx + 1}. ${q.questionText}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = NavyHeading
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            q.options.forEachIndexed { oIdx, opt ->
                                                val isCorrect = oIdx == q.correctIndex
                                                Text(
                                                    text = if (isCorrect) "✓ $opt" else "• $opt",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isCorrect) SoftMintSuccess else MutedBlueGray,
                                                    fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                            if (q.explanation.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Why: ${q.explanation}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SkyBlueAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            if (result.savedQuizId == null) {
                                PastelPrimaryButton(
                                    text = "Save Selected to Quizzes",
                                    icon = Icons.Rounded.Save,
                                    onClick = onSaveQuiz,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                PastelPrimaryButton(
                                    text = "Take Saved Quiz Now",
                                    icon = Icons.Rounded.PlayArrow,
                                    onClick = { onOpenSavedQuiz(result.savedQuizId) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    is AiResultState.FlashcardDraftReady -> {
                        StudyCard(contentPadding = PaddingValues(16.dp)) {
                            Text(
                                text = "Review Generated Flashcards",
                                style = MaterialTheme.typography.titleMedium,
                                color = NavyHeading
                            )
                            Text(
                                text = "Select the flashcards you want to add to your study deck.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedBlueGray
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = result.title,
                                onValueChange = onUpdateDeckTitle,
                                label = { Text("Deck Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            result.cards.forEachIndexed { idx, card ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = SoftSkyTint,
                                    border = BorderStroke(1.dp, CardBorderBlue)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = card.isSelected,
                                            onCheckedChange = { onToggleFlashcard(idx) }
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Front: ${card.front}", style = MaterialTheme.typography.titleSmall, color = NavyHeading)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text("Back: ${card.back}", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            if (result.savedDeckId == null) {
                                PastelPrimaryButton(
                                    text = "Save Selected to Flashcards",
                                    icon = Icons.Rounded.Save,
                                    onClick = onSaveFlashcards,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                PastelPrimaryButton(
                                    text = "Study Saved Deck Now",
                                    icon = Icons.Rounded.PlayArrow,
                                    onClick = { onOpenSavedDeck(result.savedDeckId) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    is AiResultState.Error -> {
                        StudyCard(containerColor = SoftCoralContainer, contentPadding = PaddingValues(14.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = SoftCoralUrgent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(result.message, style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showNotePicker) {
        AlertDialog(
            onDismissRequest = { showNotePicker = false },
            containerColor = PureWhiteSurface,
            title = { Text("Load Note Content", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedNotes.forEach { note ->
                        StudyCard(
                            onClick = {
                                onUpdateSubject(note.subject)
                                onUpdateInput(note.content.ifBlank { note.title })
                                showNotePicker = false
                            },
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Text(note.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleSmall, color = NavyHeading)
                            Text(note.subject, style = MaterialTheme.typography.labelSmall, color = SkyBlueAccent)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotePicker = false }) { Text("Close") }
            }
        )
    }
}
