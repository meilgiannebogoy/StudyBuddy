package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.TaskEntity
import com.example.data.local.TaskPriority
import com.example.ui.components.DefaultSubjects
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.PriorityBadge
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.SubjectBadge
import com.example.ui.components.formatDateShort
import com.example.ui.components.formatDateTimeShort
import com.example.ui.components.formatTimeOfDay
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
import com.example.ui.theme.SoftMintSuccess
import com.example.ui.theme.SoftSkyTint
import java.util.Calendar

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    onSaveTask: (TaskEntity) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit
) {
    var showCompletedTab by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf("All") }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }

    val availableSubjects = remember(tasks) {
        listOf("All") + (DefaultSubjects + tasks.map { it.subject }).distinct()
    }

    val now = System.currentTimeMillis()
    val filteredTasks = remember(tasks, showCompletedTab, selectedSubject) {
        tasks.filter { task ->
            val tabMatches = if (showCompletedTab) task.isCompleted else !task.isCompleted
            val subjMatches = selectedSubject == "All" || task.subject.equals(selectedSubject, ignoreCase = true)
            tabMatches && subjMatches
        }.sortedBy { it.dueDateMillis }
    }

    val pendingCount = remember(tasks) { tasks.count { !it.isCompleted } }
    val completedCount = remember(tasks) { tasks.count { it.isCompleted } }
    val overdueCount = remember(tasks, now) { tasks.count { !it.isCompleted && it.dueDateMillis < now } }

    Scaffold(
        containerColor = SoftIceBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showTaskDialog = true
                },
                containerColor = PastelBluePrimary,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Task")
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
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "My Tasks",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NavyHeading
                )
                Text(
                    text = if (overdueCount > 0) {
                        "$pendingCount pending • $overdueCount overdue"
                    } else {
                        "$pendingCount pending • $completedCount completed"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (overdueCount > 0) SoftCoralUrgent else MutedBlueGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Pending / Completed Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TabPillButton(
                        label = "Pending ($pendingCount)",
                        selected = !showCompletedTab,
                        onClick = { showCompletedTab = false },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tasks_pending_tab")
                    )
                    TabPillButton(
                        label = "Completed ($completedCount)",
                        selected = showCompletedTab,
                        onClick = { showCompletedTab = true },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tasks_completed_tab")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Subject Filter Strip
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

                if (filteredTasks.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.AutoMirrored.Rounded.Assignment,
                        title = if (showCompletedTab) "No completed tasks yet" else "No pending tasks",
                        subtitle = if (showCompletedTab) {
                            "Check off assignments as you finish them to build momentum."
                        } else {
                            "Tap + to add an assignment, homework deadline, or exam reminder."
                        },
                        actionLabel = if (showCompletedTab) null else "Create Task",
                        onAction = if (showCompletedTab) null else {
                            {
                                editingTask = null
                                showTaskDialog = true
                            }
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskItemCard(
                                task = task,
                                isOverdue = !task.isCompleted && task.dueDateMillis < now,
                                onToggle = { onToggleTask(task) },
                                onEdit = {
                                    editingTask = task
                                    showTaskDialog = true
                                },
                                onDelete = { onDeleteTask(task) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskEditorDialog(
            initialTask = editingTask,
            onDismiss = { showTaskDialog = false },
            onSave = { saved ->
                onSaveTask(saved)
                showTaskDialog = false
            }
        )
    }
}

@Composable
private fun TabPillButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) PastelBluePrimary else PureWhiteSurface,
        border = BorderStroke(1.dp, if (selected) PastelBluePrimary else CardBorderBlue)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) PureWhiteSurface else NavyHeading
            )
        }
    }
}

@Composable
private fun TaskItemCard(
    task: TaskEntity,
    isOverdue: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    StudyCard(
        containerColor = if (isOverdue) Color(0xFFFFF8F9) else PureWhiteSurface,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.testTag("toggle_task_${task.id}")
            ) {
                Icon(
                    imageVector = if (task.isCompleted) {
                        Icons.Rounded.CheckCircle
                    } else {
                        Icons.Rounded.RadioButtonUnchecked
                    },
                    contentDescription = if (task.isCompleted) "Mark pending" else "Mark completed",
                    tint = if (task.isCompleted) SoftMintSuccess else PastelBluePrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.isCompleted) MutedBlueGray else NavyHeading,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedBlueGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectBadge(task.subject)
                    PriorityBadge(task.priority)
                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SoftCoralContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.WarningAmber,
                                    contentDescription = null,
                                    tint = SoftCoralUrgent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Overdue",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftCoralUrgent
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Due: ${formatDateTimeShort(task.dueDateMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOverdue) SoftCoralUrgent else SkyBlueAccent
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_task_${task.id}")) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit task",
                        tint = SkyBlueAccent
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_task_${task.id}")) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = MutedBlueGray
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorDialog(
    initialTask: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    val defaultDue = remember(initialTask) {
        initialTask?.dueDateMillis ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
    }
    val initialCal = remember(defaultDue) {
        Calendar.getInstance().apply { timeInMillis = defaultDue }
    }

    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var subject by remember { mutableStateOf(initialTask?.subject ?: DefaultSubjects.first()) }
    var priority by remember {
        mutableStateOf(
            runCatching { TaskPriority.valueOf(initialTask?.priority ?: "MEDIUM") }
                .getOrDefault(TaskPriority.MEDIUM)
        )
    }
    var selectedDateMillis by remember { mutableLongStateOf(defaultDue) }
    var selectedHour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }
    var reminderEnabled by remember { mutableStateOf(initialTask?.reminderEnabled ?: true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteSurface,
        title = {
            Text(
                text = if (initialTask == null) "New Assignment / Task" else "Edit Task",
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
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("Task Title *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    maxLines = 3,
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

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelLarge,
                    color = NavyHeading
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPriority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface
                            )
                        )
                    }
                }

                Text(
                    text = "Deadline",
                    style = MaterialTheme.typography.labelLarge,
                    color = NavyHeading
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SoftSkyTint,
                        border = BorderStroke(1.dp, CardBorderBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(formatDateShort(selectedDateMillis), style = MaterialTheme.typography.bodySmall, color = NavyHeading)
                        }
                    }
                    Surface(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SoftSkyTint,
                        border = BorderStroke(1.dp, CardBorderBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(formatTimeOfDay(selectedHour, selectedMinute), style = MaterialTheme.typography.bodySmall, color = NavyHeading)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Deadline reminder notification",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NavyHeading
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftCoralUrgent
                    )
                }
            }
        },
        confirmButton = {
            PastelPrimaryButton(
                text = "Save Task",
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a task title."
                        return@PastelPrimaryButton
                    }
                    val combinedCal = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMillis
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                    }
                    onSave(
                        TaskEntity(
                            id = initialTask?.id ?: 0L,
                            title = title.trim(),
                            description = description.trim(),
                            subject = subject.trim().ifBlank { "General" },
                            dueDateMillis = combinedCal.timeInMillis,
                            priority = priority.name,
                            isCompleted = initialTask?.isCompleted ?: false,
                            completedAtMillis = initialTask?.completedAtMillis,
                            reminderEnabled = reminderEnabled,
                            createdAtMillis = initialTask?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.testTag("save_task_button")
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedBlueGray)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = utcMillis
                        }
                        val localCal = Calendar.getInstance().apply {
                            set(
                                utcCal.get(Calendar.YEAR),
                                utcCal.get(Calendar.MONTH),
                                utcCal.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                        selectedDateMillis = localCal.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedHour = timeState.hour
                    selectedMinute = timeState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}
