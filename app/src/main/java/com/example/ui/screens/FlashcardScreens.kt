package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Style
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CardStatus
import com.example.data.local.FlashcardDeckEntity
import com.example.data.local.FlashcardEntity
import com.example.ui.components.DefaultSubjects
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PastelOutlinedButton
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftAmberContainer
import com.example.ui.theme.SoftAmberWarning
import com.example.ui.theme.SoftCoralUrgent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftMintContainer
import com.example.ui.theme.SoftMintSuccess
import com.example.ui.theme.SoftSkyTint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardDecksScreen(
    decks: List<FlashcardDeckEntity>,
    allCards: List<FlashcardEntity>,
    onBack: () -> Unit,
    onCreateDeck: suspend (String, String) -> Long,
    onOpenDeckEditor: (Long) -> Unit,
    onStudyDeck: (deckId: Long, difficultOnly: Boolean) -> Unit,
    onDeleteDeck: (FlashcardDeckEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedSubject by remember { mutableStateOf("All") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val subjects = remember(decks) {
        listOf("All") + (DefaultSubjects + decks.map { it.subject }).distinct()
    }
    val filteredDecks = remember(decks, selectedSubject) {
        decks.filter { selectedSubject == "All" || it.subject.equals(selectedSubject, ignoreCase = true) }
    }

    val totalCards = allCards.size
    val reviewedCards = allCards.count { it.status != CardStatus.NEW.name }
    val masteredCards = allCards.count { it.status == CardStatus.MASTERED.name }
    val needPracticeCards = allCards.count { it.status == CardStatus.NEEDS_PRACTICE.name }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = { Text("My Flashcards", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
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
                onClick = { showCreateDialog = true },
                containerColor = PastelBluePrimary,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("create_deck_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Flashcard Deck")
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
                // Global Flashcard Progress Summary
                StudyCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FlashcardStatBadge("Total", "$totalCards", SoftSkyTint, SkyBlueAccent, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        FlashcardStatBadge("Reviewed", "$reviewedCards", BabyBlueContainer, NavyHeading, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        FlashcardStatBadge("Mastered", "$masteredCards", SoftMintContainer, SoftMintSuccess, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        FlashcardStatBadge("Practice", "$needPracticeCards", SoftAmberContainer, SoftAmberWarning, Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    subjects.forEach { subj ->
                        FilterChip(
                            selected = selectedSubject == subj,
                            onClick = { selectedSubject = subj },
                            label = { Text(subj) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface,
                                containerColor = PureWhiteSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredDecks.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Rounded.Style,
                        title = "No flashcard decks yet",
                        subtitle = "Create a deck for definitions, formulas, vocabulary, or exam concepts.",
                        actionLabel = "Create First Deck",
                        onAction = { showCreateDialog = true }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(filteredDecks, key = { it.id }) { deck ->
                            val cards = allCards.filter { it.deckId == deck.id }
                            val mastered = cards.count { it.status == CardStatus.MASTERED.name }
                            val difficult = cards.count { it.status == CardStatus.NEEDS_PRACTICE.name }

                            StudyCard(contentPadding = PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(deck.title, style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            SubjectBadge(deck.subject)
                                            Text(
                                                "${cards.size} cards • $mastered mastered",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MutedBlueGray
                                            )
                                        }
                                    }
                                    IconButton(onClick = { onOpenDeckEditor(deck.id) }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Manage cards", tint = SkyBlueAccent)
                                    }
                                    IconButton(onClick = { onDeleteDeck(deck) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete deck", tint = MutedBlueGray)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PastelOutlinedButton(
                                        text = if (difficult > 0) "Practice ($difficult)" else "Edit Cards",
                                        onClick = {
                                            if (difficult > 0) onStudyDeck(deck.id, true)
                                            else onOpenDeckEditor(deck.id)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    PastelPrimaryButton(
                                        text = "Study Deck",
                                        icon = Icons.Rounded.PlayArrow,
                                        enabled = cards.isNotEmpty(),
                                        onClick = { onStudyDeck(deck.id, false) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        DeckMetaDialog(
            initialTitle = "",
            initialSubject = DefaultSubjects.first(),
            onDismiss = { showCreateDialog = false },
            onSave = { title, subject ->
                showCreateDialog = false
                scope.launch {
                    val id = onCreateDeck(title, subject)
                    onOpenDeckEditor(id)
                }
            }
        )
    }
}

@Composable
private fun FlashcardStatBadge(
    label: String,
    value: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = fg)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Long,
    loadDeck: suspend (Long) -> FlashcardDeckEntity?,
    observeCards: (Long) -> Flow<List<FlashcardEntity>>,
    onUpdateDeckMeta: suspend (Long, String, String) -> Long,
    onSaveCard: (FlashcardEntity) -> Unit,
    onDeleteCard: (FlashcardEntity) -> Unit,
    onResetProgress: (Long) -> Unit,
    onStudyDeck: (deckId: Long, difficultOnly: Boolean) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var deck by remember { mutableStateOf<FlashcardDeckEntity?>(null) }
    val cards by observeCards(deckId).collectAsStateWithLifecycle(initialValue = emptyList())
    var editingCard by remember { mutableStateOf<FlashcardEntity?>(null) }
    var showCardDialog by remember { mutableStateOf(false) }
    var showMetaDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) {
        deck = loadDeck(deckId)
    }

    val difficultCount = cards.count { it.status == CardStatus.NEEDS_PRACTICE.name }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(deck?.title ?: "Flashcard Deck", style = MaterialTheme.typography.titleLarge, color = NavyHeading)
                        Text(deck?.subject ?: "", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyHeading)
                    }
                },
                actions = {
                    IconButton(onClick = { showMetaDialog = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit deck info", tint = SkyBlueAccent)
                    }
                    IconButton(onClick = { onResetProgress(deckId) }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Reset mastery progress", tint = MutedBlueGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftIceBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCard = null
                    showCardDialog = true
                },
                containerColor = PastelBluePrimary,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_flashcard_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Flashcard")
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
                if (cards.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PastelPrimaryButton(
                            text = "Study All (${cards.size})",
                            icon = Icons.Rounded.PlayArrow,
                            onClick = { onStudyDeck(deckId, false) },
                            modifier = Modifier.weight(1f)
                        )
                        if (difficultCount > 0) {
                            PastelOutlinedButton(
                                text = "Review Hard ($difficultCount)",
                                onClick = { onStudyDeck(deckId, true) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (cards.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Rounded.Style,
                        title = "No cards in this deck",
                        subtitle = "Tap + to add terms on the front and definitions or answers on the back.",
                        actionLabel = "Add First Card",
                        onAction = {
                            editingCard = null
                            showCardDialog = true
                        }
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(cards, key = { it.id }) { card ->
                            StudyCard(contentPadding = PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Front: ${card.front}", style = MaterialTheme.typography.titleSmall, color = NavyHeading)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Back: ${card.back}", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        val (statusText, statusBg, statusFg) = when (card.status) {
                                            CardStatus.MASTERED.name -> Triple("Mastered", SoftMintContainer, SoftMintSuccess)
                                            CardStatus.NEEDS_PRACTICE.name -> Triple("Needs Practice", SoftAmberContainer, SoftAmberWarning)
                                            else -> Triple("Not Reviewed", SoftSkyTint, SkyBlueAccent)
                                        }
                                        Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                                            Text(
                                                statusText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = statusFg,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = {
                                        editingCard = card
                                        showCardDialog = true
                                    }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Edit card", tint = SkyBlueAccent)
                                    }
                                    IconButton(onClick = { onDeleteCard(card) }) {
                                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete card", tint = MutedBlueGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMetaDialog && deck != null) {
        DeckMetaDialog(
            initialTitle = deck!!.title,
            initialSubject = deck!!.subject,
            onDismiss = { showMetaDialog = false },
            onSave = { title, subject ->
                showMetaDialog = false
                scope.launch {
                    onUpdateDeckMeta(deckId, title, subject)
                    deck = loadDeck(deckId)
                }
            }
        )
    }

    if (showCardDialog) {
        FlashcardEditorDialog(
            deckId = deckId,
            initial = editingCard,
            onDismiss = { showCardDialog = false },
            onSave = { saved ->
                onSaveCard(saved)
                showCardDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyFlashcardsScreen(
    deckId: Long,
    difficultOnly: Boolean,
    loadDeck: suspend (Long) -> FlashcardDeckEntity?,
    observeCards: (Long) -> Flow<List<FlashcardEntity>>,
    onMarkCardStatus: (FlashcardEntity, CardStatus) -> Unit,
    onBack: () -> Unit
) {
    var deck by remember { mutableStateOf<FlashcardDeckEntity?>(null) }
    val allCards by observeCards(deckId).collectAsStateWithLifecycle(initialValue = emptyList())
    var deckOrder by remember { mutableStateOf<List<Long>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }

    LaunchedEffect(deckId) {
        deck = loadDeck(deckId)
    }

    LaunchedEffect(allCards, difficultOnly) {
        if (deckOrder.isEmpty() && allCards.isNotEmpty()) {
            val pool = if (difficultOnly) {
                allCards.filter { it.status == CardStatus.NEEDS_PRACTICE.name }.ifEmpty { allCards }
            } else {
                allCards
            }
            deckOrder = pool.map { it.id }
        }
    }

    val studyCards = remember(allCards, deckOrder) {
        val byId = allCards.associateBy { it.id }
        deckOrder.mapNotNull { byId[it] }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "flashcard_flip"
    )

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(deck?.title ?: "Study Flashcards", style = MaterialTheme.typography.titleLarge, color = NavyHeading)
                        if (difficultOnly) {
                            Text("Reviewing cards needing practice", style = MaterialTheme.typography.bodySmall, color = SoftAmberWarning)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = NavyHeading)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (studyCards.isNotEmpty()) {
                                deckOrder = deckOrder.shuffled()
                                currentIndex = 0
                                isFlipped = false
                            }
                        },
                        modifier = Modifier.testTag("shuffle_flashcards_button")
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle cards", tint = SkyBlueAccent)
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
            if (studyCards.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Rounded.Style,
                    title = "No flashcards ready",
                    subtitle = "Add cards to this deck first before starting a study session.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val safeIndex = currentIndex.coerceIn(0, studyCards.lastIndex)
                val card = studyCards[safeIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Card ${safeIndex + 1} of ${studyCards.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = SkyBlueAccent
                        )
                        val masteredCount = studyCards.count { it.status == CardStatus.MASTERED.name }
                        Text(
                            text = "$masteredCount mastered",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMintSuccess
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (safeIndex + 1).toFloat() / studyCards.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PastelBluePrimary,
                        trackColor = BabyBlueContainer
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // 3D Flipping Flashcard
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clip(RoundedCornerShape(26.dp))
                            .clickable { isFlipped = !isFlipped }
                            .testTag("flashcard_flip_surface")
                    ) {
                        if (rotation <= 90f) {
                            StudyCard(
                                modifier = Modifier.fillMaxSize(),
                                containerColor = PureWhiteSurface,
                                contentPadding = PaddingValues(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(shape = RoundedCornerShape(50), color = SoftSkyTint) {
                                        Text(
                                            "QUESTION / TERM",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkyBlueAccent,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                    Text(
                                        text = card.front,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = NavyHeading,
                                        textAlign = TextAlign.Center
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.Flip, contentDescription = null, tint = MutedBlueGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tap card to flip", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                                    }
                                }
                            }
                        } else {
                            StudyCard(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f },
                                containerColor = SoftSkyTint,
                                contentPadding = PaddingValues(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Surface(shape = RoundedCornerShape(50), color = PureWhiteSurface) {
                                        Text(
                                            "ANSWER / DEFINITION",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SoftMintSuccess,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                    Text(
                                        text = card.back,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = NavyHeading,
                                        textAlign = TextAlign.Center
                                    )
                                    Text("Tap card to flip back", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Mastery Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = {
                                onMarkCardStatus(card, CardStatus.NEEDS_PRACTICE)
                                isFlipped = false
                                if (safeIndex < studyCards.lastIndex) currentIndex++
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("card_needs_practice_button"),
                            shape = RoundedCornerShape(16.dp),
                            color = SoftAmberContainer,
                            border = BorderStroke(1.dp, SoftAmberWarning.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Need More Practice", style = MaterialTheme.typography.labelLarge, color = SoftAmberWarning)
                            }
                        }

                        Surface(
                            onClick = {
                                onMarkCardStatus(card, CardStatus.MASTERED)
                                isFlipped = false
                                if (safeIndex < studyCards.lastIndex) currentIndex++
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("card_mastered_button"),
                            shape = RoundedCornerShape(16.dp),
                            color = SoftMintContainer,
                            border = BorderStroke(1.dp, SoftMintSuccess.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SoftMintSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("I Know This", style = MaterialTheme.typography.labelLarge, color = SoftMintSuccess)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Previous / Next
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PastelOutlinedButton(
                            text = "Previous",
                            enabled = safeIndex > 0,
                            onClick = {
                                if (safeIndex > 0) {
                                    currentIndex = safeIndex - 1
                                    isFlipped = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        PastelPrimaryButton(
                            text = if (safeIndex < studyCards.lastIndex) "Next" else "Restart",
                            onClick = {
                                currentIndex = if (safeIndex < studyCards.lastIndex) safeIndex + 1 else 0
                                isFlipped = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckMetaDialog(
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
                if (initialTitle.isBlank()) "Create Flashcard Deck" else "Edit Deck",
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
                    label = { Text("Deck Title *") },
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
                        errorMsg = "Please enter a deck title."
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
private fun FlashcardEditorDialog(
    deckId: Long,
    initial: FlashcardEntity?,
    onDismiss: () -> Unit,
    onSave: (FlashcardEntity) -> Unit
) {
    var front by remember { mutableStateOf(initial?.front ?: "") }
    var back by remember { mutableStateOf(initial?.back ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteSurface,
        title = {
            Text(
                if (initial == null) "Add Flashcard" else "Edit Flashcard",
                style = MaterialTheme.typography.titleLarge,
                color = NavyHeading
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = front,
                    onValueChange = {
                        front = it
                        errorMsg = null
                    },
                    label = { Text("Front (Term / Question) *") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = back,
                    onValueChange = {
                        back = it
                        errorMsg = null
                    },
                    label = { Text("Back (Answer / Definition) *") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = SoftCoralUrgent)
                }
            }
        },
        confirmButton = {
            PastelPrimaryButton(
                text = "Save Card",
                onClick = {
                    if (front.isBlank() || back.isBlank()) {
                        errorMsg = "Both front and back are required."
                        return@PastelPrimaryButton
                    }
                    onSave(
                        FlashcardEntity(
                            id = initial?.id ?: 0L,
                            deckId = deckId,
                            front = front.trim(),
                            back = back.trim(),
                            status = initial?.status ?: CardStatus.NEW.name,
                            lastReviewedMillis = initial?.lastReviewedMillis
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
