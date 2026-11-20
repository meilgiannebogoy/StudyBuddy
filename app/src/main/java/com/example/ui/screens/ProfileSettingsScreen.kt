package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UserPreferences
import com.example.ui.components.PastelOutlinedButton
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.components.formatTimeOfDay
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftMintContainer
import com.example.ui.theme.SoftMintSuccess
import com.example.ui.theme.SoftSkyTint
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    preferences: UserPreferences,
    hasNotificationPermission: () -> Boolean,
    onBack: () -> Unit,
    onSaveProfile: (name: String, subtitle: String, dailyGoalMinutes: Int) -> Unit,
    onSaveNotifications: (
        notificationsEnabled: Boolean,
        taskReminders: Boolean,
        classReminders: Boolean,
        timerNotifications: Boolean,
        dailyReminder: Boolean,
        dailyHour: Int,
        dailyMinute: Int
    ) -> Unit,
    onSendTestNotification: () -> Unit
) {
    var name by remember(preferences.studentName) { mutableStateOf(preferences.studentName) }
    var subtitle by remember(preferences.studentSubtitle) { mutableStateOf(preferences.studentSubtitle) }
    var goalMinutes by remember(preferences.dailyStudyGoalMinutes) {
        mutableFloatStateOf(preferences.dailyStudyGoalMinutes.toFloat())
    }
    var savedBanner by remember { mutableStateOf<String?>(null) }

    var notificationsEnabled by remember(preferences.notificationsEnabled) {
        mutableStateOf(preferences.notificationsEnabled)
    }
    var taskReminders by remember(preferences.taskRemindersEnabled) {
        mutableStateOf(preferences.taskRemindersEnabled)
    }
    var classReminders by remember(preferences.classRemindersEnabled) {
        mutableStateOf(preferences.classRemindersEnabled)
    }
    var timerNotifications by remember(preferences.timerNotificationsEnabled) {
        mutableStateOf(preferences.timerNotificationsEnabled)
    }
    var dailyReminder by remember(preferences.dailyReminderEnabled) {
        mutableStateOf(preferences.dailyReminderEnabled)
    }
    var dailyHour by remember(preferences.dailyReminderHour) {
        mutableIntStateOf(preferences.dailyReminderHour)
    }
    var dailyMinute by remember(preferences.dailyReminderMinute) {
        mutableIntStateOf(preferences.dailyReminderMinute)
    }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(hasNotificationPermission()) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) {
            onSendTestNotification()
        }
    }

    LaunchedEffect(Unit) {
        permissionGranted = hasNotificationPermission()
    }

    fun applyNotificationPrefs() {
        onSaveNotifications(
            notificationsEnabled,
            taskReminders,
            classReminders,
            timerNotifications,
            dailyReminder,
            dailyHour,
            dailyMinute
        )
    }

    Scaffold(
        containerColor = SoftIceBackground,
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", style = MaterialTheme.typography.titleLarge, color = NavyHeading) },
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
                if (savedBanner != null) {
                    StudyCard(containerColor = SoftMintContainer, contentPadding = PaddingValues(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SoftMintSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(savedBanner!!, style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                        }
                    }
                }

                // Student Profile Card
                StudyCard(contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(BabyBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Person, contentDescription = "Profile", tint = SkyBlueAccent, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Student Profile", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                            Text("No account required • Saved locally", style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = subtitle,
                        onValueChange = { subtitle = it },
                        label = { Text("Major / Year / Study Bio") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Daily Focus Goal: ${goalMinutes.roundToInt()} minutes",
                        style = MaterialTheme.typography.titleSmall,
                        color = NavyHeading
                    )
                    Slider(
                        value = goalMinutes,
                        onValueChange = { goalMinutes = it },
                        valueRange = 15f..360f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = PastelBluePrimary,
                            activeTrackColor = PastelBluePrimary,
                            inactiveTrackColor = BabyBlueContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    PastelPrimaryButton(
                        text = "Save Profile",
                        onClick = {
                            onSaveProfile(name, subtitle, goalMinutes.roundToInt())
                            savedBanner = "Profile updated!"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_button")
                    )
                }

                // Notification Preferences
                StudyCard(contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = SkyBlueAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reminders & Notifications", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "Enable Notifications",
                        subtitle = "Master switch for all StudyBuddy reminders",
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            applyNotificationPrefs()
                        }
                    )

                    SettingSwitchRow(
                        title = "Assignment & Task Deadlines",
                        subtitle = "Notify 1 hour before due deadlines",
                        checked = taskReminders,
                        enabled = notificationsEnabled,
                        onCheckedChange = {
                            taskReminders = it
                            applyNotificationPrefs()
                        }
                    )

                    SettingSwitchRow(
                        title = "Scheduled Classes",
                        subtitle = "Remind 15 minutes before class starts",
                        checked = classReminders,
                        enabled = notificationsEnabled,
                        onCheckedChange = {
                            classReminders = it
                            applyNotificationPrefs()
                        }
                    )

                    SettingSwitchRow(
                        title = "Pomodoro Timer Completion",
                        subtitle = "Alert when focus or break sessions finish",
                        checked = timerNotifications,
                        enabled = notificationsEnabled,
                        onCheckedChange = {
                            timerNotifications = it
                            applyNotificationPrefs()
                        }
                    )

                    SettingSwitchRow(
                        title = "Daily Study Reminder",
                        subtitle = "Daily check-in at ${formatTimeOfDay(dailyHour, dailyMinute)}",
                        checked = dailyReminder,
                        enabled = notificationsEnabled,
                        onCheckedChange = {
                            dailyReminder = it
                            applyNotificationPrefs()
                        }
                    )

                    if (dailyReminder && notificationsEnabled) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            onClick = { showReminderTimePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftSkyTint,
                            border = BorderStroke(1.dp, CardBorderBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = SkyBlueAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Daily Reminder Time", style = MaterialTheme.typography.bodyMedium, color = NavyHeading)
                                }
                                Text(formatTimeOfDay(dailyHour, dailyMinute), style = MaterialTheme.typography.labelLarge, color = SkyBlueAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    PastelOutlinedButton(
                        text = if (permissionGranted) "Send Test Notification" else "Allow & Test Notifications",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission()) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onSendTestNotification()
                                savedBanner = "Test notification sent!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // About StudyBuddy
                StudyCard(containerColor = SoftSkyTint, contentPadding = PaddingValues(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = SkyBlueAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About StudyBuddy", style = MaterialTheme.typography.titleMedium, color = NavyHeading)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "StudyBuddy – Your Personal Study Companion brings together assignment tracking, weekly class scheduling, a Pomodoro timer, auto-saving notes, quizzes, flashcards, an AI study assistant, and progress analytics in one calm workspace.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedBlueGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0 • Local Room Storage + Gemini AI Study Assistant",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueAccent
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showReminderTimePicker) {
        val state = rememberTimePickerState(initialHour = dailyHour, initialMinute = dailyMinute)
        AlertDialog(
            onDismissRequest = { showReminderTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dailyHour = state.hour
                    dailyMinute = state.minute
                    showReminderTimePicker = false
                    applyNotificationPrefs()
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showReminderTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = state) }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = NavyHeading)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MutedBlueGray)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}
