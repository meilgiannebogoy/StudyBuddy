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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.ClassScheduleEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.dayOfWeekName
import com.example.ui.components.dayOfWeekShort
import com.example.ui.components.formatTimeOfDay
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.ScheduleTagColors
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftCoralUrgent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftSkyTint
import java.util.Calendar

@Composable
fun ScheduleScreen(
    classes: List<ClassScheduleEntity>,
    onSaveClasses: (
        existingId: Long,
        subjectName: String,
        instructor: String,
        room: String,
        days: List<Int>,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        colorTag: Int,
        reminderEnabled: Boolean
    ) -> Unit,
    onDeleteClass: (ClassScheduleEntity) -> Unit
) {
    val todayIndex = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }
    var selectedDay by remember { mutableIntStateOf(todayIndex) }
    var showFullWeek by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<ClassScheduleEntity?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val dayClasses = remember(classes, selectedDay) {
        classes.filter { it.dayOfWeek == selectedDay }
            .sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }

    Scaffold(
        containerColor = SoftIceBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingClass = null
                    showDialog = true
                },
                containerColor = PastelBluePrimary,
                contentColor = PureWhiteSurface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_class_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Class")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Class Schedule",
                            style = MaterialTheme.typography.headlineMedium,
                            color = NavyHeading
                        )
                        Text(
                            text = "${classes.size} weekly classes scheduled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedBlueGray
                        )
                    }
                    FilterChip(
                        selected = showFullWeek,
                        onClick = { showFullWeek = !showFullWeek },
                        label = { Text(if (showFullWeek) "Day View" else "Full Week") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PastelBluePrimary,
                            selectedLabelColor = PureWhiteSurface,
                            containerColor = PureWhiteSurface,
                            labelColor = SkyBlueAccent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!showFullWeek) {
                    // 7-Day Selector Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..7).forEach { day ->
                            val isSelected = selectedDay == day
                            val hasClasses = classes.any { it.dayOfWeek == day }
                            Surface(
                                onClick = { selectedDay = day },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .testTag("schedule_day_$day"),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PastelBluePrimary else PureWhiteSurface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) PastelBluePrimary else CardBorderBlue
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = dayOfWeekShort(day),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) PureWhiteSurface else NavyHeading
                                    )
                                    if (hasClasses) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) PureWhiteSurface else PastelBluePrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = dayOfWeekName(selectedDay),
                        style = MaterialTheme.typography.titleLarge,
                        color = NavyHeading
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (dayClasses.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.CalendarMonth,
                            title = "No classes on ${dayOfWeekName(selectedDay)}",
                            subtitle = "Tap + to add your subjects, class times, and room numbers.",
                            actionLabel = "Add Class",
                            onAction = {
                                editingClass = null
                                showDialog = true
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(dayClasses, key = { it.id }) { item ->
                                ClassItemCard(
                                    item = item,
                                    onEdit = {
                                        editingClass = item
                                        showDialog = true
                                    },
                                    onDelete = { onDeleteClass(item) }
                                )
                            }
                        }
                    }
                } else {
                    // Full Weekly Timetable Overview
                    if (classes.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Rounded.CalendarMonth,
                            title = "Your weekly timetable is empty",
                            subtitle = "Add your classes once and organize your entire academic week.",
                            actionLabel = "Add First Class",
                            onAction = {
                                editingClass = null
                                showDialog = true
                            }
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items((1..7).toList()) { day ->
                                val list = classes.filter { it.dayOfWeek == day }
                                    .sortedWith(compareBy({ it.startHour }, { it.startMinute }))
                                StudyCard(contentPadding = PaddingValues(14.dp)) {
                                    Text(
                                        text = dayOfWeekName(day),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NavyHeading
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (list.isEmpty()) {
                                        Text(
                                            text = "No scheduled classes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedBlueGray
                                        )
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            list.forEach { item ->
                                                val tagColor = ScheduleTagColors[item.colorTag % ScheduleTagColors.size]
                                                Surface(
                                                    onClick = {
                                                        editingClass = item
                                                        showDialog = true
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = SoftSkyTint,
                                                    border = BorderStroke(1.dp, CardBorderBlue)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(5.dp)
                                                                .height(36.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(tagColor)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = item.subjectName,
                                                                style = MaterialTheme.typography.titleSmall,
                                                                color = NavyHeading
                                                            )
                                                            Text(
                                                                text = "${formatTimeOfDay(item.startHour, item.startMinute)} - ${formatTimeOfDay(item.endHour, item.endMinute)}" +
                                                                    if (item.room.isNotBlank()) " • ${item.room}" else "",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MutedBlueGray
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
                }
            }
        }
    }

    if (showDialog) {
        ClassEditorDialog(
            initialItem = editingClass,
            defaultDay = selectedDay,
            onDismiss = { showDialog = false },
            onSave = { subject, instructor, room, days, sh, sm, eh, em, tag, reminder ->
                onSaveClasses(
                    editingClass?.id ?: 0L,
                    subject,
                    instructor,
                    room,
                    days,
                    sh,
                    sm,
                    eh,
                    em,
                    tag,
                    reminder
                )
                showDialog = false
            }
        )
    }
}

@Composable
private fun ClassItemCard(
    item: ClassScheduleEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val accent = ScheduleTagColors[item.colorTag % ScheduleTagColors.size]
    StudyCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(62.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyHeading
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = SkyBlueAccent, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${formatTimeOfDay(item.startHour, item.startMinute)} – ${formatTimeOfDay(item.endHour, item.endMinute)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SkyBlueAccent
                    )
                }
                if (item.room.isNotBlank() || item.instructor.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (item.room.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MutedBlueGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(item.room, style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                            }
                        }
                        if (item.instructor.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = MutedBlueGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(item.instructor, style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                            }
                        }
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, contentDescription = "Edit class", tint = SkyBlueAccent)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete class", tint = MutedBlueGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassEditorDialog(
    initialItem: ClassScheduleEntity?,
    defaultDay: Int,
    onDismiss: () -> Unit,
    onSave: (
        subjectName: String,
        instructor: String,
        room: String,
        days: List<Int>,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        colorTag: Int,
        reminderEnabled: Boolean
    ) -> Unit
) {
    var subjectName by remember { mutableStateOf(initialItem?.subjectName ?: "") }
    var instructor by remember { mutableStateOf(initialItem?.instructor ?: "") }
    var room by remember { mutableStateOf(initialItem?.room ?: "") }
    var selectedDays by remember {
        mutableStateOf(listOf(initialItem?.dayOfWeek ?: defaultDay))
    }
    var startHour by remember { mutableIntStateOf(initialItem?.startHour ?: 9) }
    var startMinute by remember { mutableIntStateOf(initialItem?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialItem?.endHour ?: 10) }
    var endMinute by remember { mutableIntStateOf(initialItem?.endMinute ?: 0) }
    var colorTag by remember { mutableIntStateOf(initialItem?.colorTag ?: 0) }
    var reminderEnabled by remember { mutableStateOf(initialItem?.reminderEnabled ?: true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var pickStartTime by remember { mutableStateOf(false) }
    var pickEndTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteSurface,
        title = {
            Text(
                text = if (initialItem == null) "Add Class" else "Edit Class",
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
                    value = subjectName,
                    onValueChange = {
                        subjectName = it
                        errorMsg = null
                    },
                    label = { Text("Subject Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Classroom / Room (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructor,
                    onValueChange = { instructor = it },
                    label = { Text("Instructor (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Days of Week", style = MaterialTheme.typography.labelLarge, color = NavyHeading)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..7).forEach { day ->
                        val active = day in selectedDays
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedDays = if (active && selectedDays.size > 1) {
                                    selectedDays - day
                                } else if (!active) {
                                    selectedDays + day
                                } else {
                                    selectedDays
                                }
                            },
                            label = { Text(dayOfWeekShort(day)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface
                            )
                        )
                    }
                }

                Text("Class Time", style = MaterialTheme.typography.labelLarge, color = NavyHeading)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { pickStartTime = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SoftSkyTint,
                        border = BorderStroke(1.dp, CardBorderBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Starts", style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
                            Text(formatTimeOfDay(startHour, startMinute), style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                        }
                    }
                    Surface(
                        onClick = { pickEndTime = true },
                        shape = RoundedCornerShape(12.dp),
                        color = SoftSkyTint,
                        border = BorderStroke(1.dp, CardBorderBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Ends", style = MaterialTheme.typography.labelSmall, color = MutedBlueGray)
                            Text(formatTimeOfDay(endHour, endMinute), style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                        }
                    }
                }

                Text("Blue Shade Tag", style = MaterialTheme.typography.labelLarge, color = NavyHeading)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScheduleTagColors.forEachIndexed { idx, color ->
                        Surface(
                            onClick = { colorTag = idx },
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = color,
                            border = if (colorTag == idx) BorderStroke(3.dp, NavyHeading) else null
                        ) {}
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Class reminder (15 min before)", style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, style = MaterialTheme.typography.bodySmall, color = SoftCoralUrgent)
                }
            }
        },
        confirmButton = {
            PastelPrimaryButton(
                text = "Save Class",
                onClick = {
                    if (subjectName.isBlank()) {
                        errorMsg = "Please enter a subject name."
                        return@PastelPrimaryButton
                    }
                    val startTotal = startHour * 60 + startMinute
                    val endTotal = endHour * 60 + endMinute
                    if (endTotal <= startTotal) {
                        errorMsg = "End time must be after start time."
                        return@PastelPrimaryButton
                    }
                    onSave(
                        subjectName,
                        instructor,
                        room,
                        selectedDays,
                        startHour,
                        startMinute,
                        endHour,
                        endMinute,
                        colorTag,
                        reminderEnabled
                    )
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedBlueGray) }
        }
    )

    if (pickStartTime) {
        val state = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
        AlertDialog(
            onDismissRequest = { pickStartTime = false },
            confirmButton = {
                TextButton(onClick = {
                    startHour = state.hour
                    startMinute = state.minute
                    pickStartTime = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pickStartTime = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) }
        )
    }

    if (pickEndTime) {
        val state = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute)
        AlertDialog(
            onDismissRequest = { pickEndTime = false },
            confirmButton = {
                TextButton(onClick = {
                    endHour = state.hour
                    endMinute = state.minute
                    pickEndTime = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pickEndTime = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) }
        )
    }
}
