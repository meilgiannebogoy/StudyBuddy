package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.NoteEntity
import com.example.ui.components.DefaultSubjects
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.SubjectBadge
import com.example.ui.components.formatDateTimeShort
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftMintContainer
import com.example.ui.theme.SoftMintSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    notes: List<NoteEntity>,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("All") }

    val availableSubjects = remember(notes) {
        listOf("All") + (DefaultSubjects + notes.map { it.subject }).distinct()
    }

    val filteredNotes = remember(notes, searchQuery, selectedSubject) {
        notes.filter { note ->
            val subjMatch = selectedSubject == "All" || note.subject.equals(selectedSubject, ignoreCase = true)
            val q = searchQuery.trim()
            val queryMatch = q.isEmpty() ||
                note.title.contains(q, ignoreCase = true) ||
                note.content.contains(q, ignoreCase = true) ||
                note.subject.contains(q, ignoreCase = true)
            subjMatch && queryMatch
        }
    }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text("My Notes", style = MaterialTheme.typography.titleLarge, color = NavyHeading)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyHeading)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIceBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenNote(0L) },
                containerColor = PastelBluePrimary,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Note")
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
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search saved notes...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = SkyBlueAccent) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search", tint = MutedBlueGray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhiteSurface,
                        unfocusedContainerColor = PureWhiteSurface,
                        focusedBorderColor = PastelBluePrimary,
                        unfocusedBorderColor = CardBorderBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notes_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableSubjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface,
                                containerColor = PureWhiteSurface,
                                labelColor = NavyHeading
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredNotes.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Rounded.Description,
                        title = if (notes.isEmpty()) "No study notes yet" else "No matching notes",
                        subtitle = if (notes.isEmpty()) {
                            "Create notes for your classes. Every edit is automatically saved."
                        } else {
                            "Try another keyword or clear your subject filter."
                        },
                        actionLabel = if (notes.isEmpty()) "Write First Note" else null,
                        onAction = if (notes.isEmpty()) { { onOpenNote(0L) } } else null,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(filteredNotes, key = { it.id }) { note ->
                            StudyCard(
                                onClick = { onOpenNote(note.id) },
                                contentPadding = PaddingValues(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = note.title.ifBlank { "Untitled Note" },
                                            style = MaterialTheme.typography.titleMedium,
                                            color = NavyHeading,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (note.content.isNotBlank()) {
                                            Text(
                                                text = note.content,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedBlueGray,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SubjectBadge(note.subject)
                                            Text(
                                                text = "Edited ${formatDateTimeShort(note.updatedAtMillis)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedBlueGray
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onDeleteNote(note) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete note", tint = MutedBlueGray)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long,
    loadNote: suspend (Long) -> NoteEntity?,
    saveNote: suspend (NoteEntity) -> Long,
    onDeleteNote: (NoteEntity) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentNoteId by remember { mutableLongStateOf(noteId) }
    var createdAt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf(DefaultSubjects.first()) }
    var isLoaded by remember { mutableStateOf(noteId == 0L) }
    var saveStatus by remember { mutableStateOf("Ready") }

    LaunchedEffect(noteId) {
        if (noteId != 0L) {
            loadNote(noteId)?.let { existing ->
                currentNoteId = existing.id
                createdAt = existing.createdAtMillis
                title = existing.title
                content = existing.content
                subject = existing.subject
                saveStatus = "Saved"
            }
            isLoaded = true
        }
    }

    // Debounced auto-save whenever title, content, or subject changes
    LaunchedEffect(title, content, subject, isLoaded) {
        if (!isLoaded) return@LaunchedEffect
        if (title.isBlank() && content.isBlank()) return@LaunchedEffect
        saveStatus = "Saving..."
        delay(550L)
        val savedId = saveNote(
            NoteEntity(
                id = currentNoteId,
                title = title.trim().ifBlank { "Untitled Note" },
                content = content,
                subject = subject.trim().ifBlank { "General" },
                createdAtMillis = createdAt
            )
        )
        currentNoteId = savedId
        saveStatus = "Auto-saved"
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isLoaded && (title.isNotBlank() || content.isNotBlank())) {
                scope.launch {
                    saveNote(
                        NoteEntity(
                            id = currentNoteId,
                            title = title.trim().ifBlank { "Untitled Note" },
                            content = content,
                            subject = subject.trim().ifBlank { "General" },
                            createdAtMillis = createdAt
                        )
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentNoteId == 0L) "New Note" else "Edit Note",
                            style = MaterialTheme.typography.titleLarge,
                            color = NavyHeading
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SoftMintContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.DoneAll, contentDescription = null, tint = SoftMintSuccess, modifier = Modifier.padding(end = 3.dp))
                                    Text(saveStatus, style = MaterialTheme.typography.labelSmall, color = SoftMintSuccess)
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                saveNote(
                                    NoteEntity(
                                        id = currentNoteId,
                                        title = title.trim().ifBlank { "Untitled Note" },
                                        content = content,
                                        subject = subject.trim().ifBlank { "General" },
                                        createdAtMillis = createdAt
                                    )
                                )
                            }
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Save and go back", tint = NavyHeading)
                    }
                },
                actions = {
                    if (currentNoteId != 0L) {
                        IconButton(onClick = {
                            onDeleteNote(
                                NoteEntity(
                                    id = currentNoteId,
                                    title = title,
                                    content = content,
                                    subject = subject
                                )
                            )
                            onBack()
                        }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete note", tint = MutedBlueGray)
                        }
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
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DefaultSubjects.forEach { subj ->
                        FilterChip(
                            selected = subject.equals(subj, ignoreCase = true),
                            onClick = { subject = subj },
                            label = { Text(subj) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface,
                                containerColor = PureWhiteSurface,
                                labelColor = NavyHeading
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhiteSurface,
                        unfocusedContainerColor = PureWhiteSurface,
                        focusedBorderColor = PastelBluePrimary,
                        unfocusedBorderColor = CardBorderBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Write your study notes here...") },
                    minLines = 12,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = PureWhiteSurface,
                        unfocusedContainerColor = PureWhiteSurface,
                        focusedBorderColor = PastelBluePrimary,
                        unfocusedBorderColor = CardBorderBlue
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
