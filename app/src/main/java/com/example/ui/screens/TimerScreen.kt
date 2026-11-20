package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DefaultSubjects
import com.example.ui.components.PastelOutlinedButton
import com.example.ui.components.PastelPrimaryButton
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StudyCard
import com.example.ui.theme.BabyBlueContainer
import com.example.ui.theme.BabyBlueSecondary
import com.example.ui.theme.CardBorderBlue
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.MutedBlueGray
import com.example.ui.theme.NavyHeading
import com.example.ui.theme.PastelBluePrimary
import com.example.ui.theme.PureWhiteSurface
import com.example.ui.theme.SkyBlueAccent
import com.example.ui.theme.SoftIceBackground
import com.example.ui.theme.SoftMintContainer
import com.example.ui.theme.SoftMintSuccess
import com.example.ui.theme.SoftSkyTint
import com.example.ui.viewmodel.TimerUiState
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TimerScreen(
    uiState: TimerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSwitchMode: (Boolean) -> Unit,
    onSelectSubject: (String) -> Unit,
    onUpdateDurations: (focusMin: Int, breakMin: Int) -> Unit,
    onDismissBanner: () -> Unit
) {
    var showSettingsDialog by remember { mutableStateOf(false) }

    val progress = if (uiState.totalSeconds > 0) {
        uiState.remainingSeconds.toFloat() / uiState.totalSeconds.toFloat()
    } else {
        0f
    }
    val animatedSweep by animateFloatAsState(
        targetValue = 360f * progress,
        animationSpec = tween(durationMillis = 300),
        label = "timer_arc"
    )

    val mins = uiState.remainingSeconds / 60
    val secs = uiState.remainingSeconds % 60
    val formattedClock = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftIceBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        ResponsiveScreenContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Study Timer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NavyHeading
                    )
                    Text(
                        text = "${uiState.completedSessionsCount} focus sessions completed today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedBlueGray
                    )
                }
                Surface(
                    onClick = { showSettingsDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhiteSurface,
                    border = BorderStroke(1.dp, CardBorderBlue),
                    modifier = Modifier.testTag("timer_customize_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Tune, contentDescription = "Customize timer", tint = SkyBlueAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Durations", style = MaterialTheme.typography.labelLarge, color = SkyBlueAccent)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = uiState.lastCompletedMessage != null) {
                StudyCard(
                    containerColor = SoftMintContainer,
                    contentPadding = PaddingValues(14.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = SoftMintSuccess)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.lastCompletedMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NavyHeading,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onDismissBanner) {
                            Text("OK", color = SoftMintSuccess)
                        }
                    }
                }
            }

            // Mode Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModePill(
                    title = "Focus (${uiState.focusDurationMinutes}m)",
                    icon = Icons.Rounded.MenuBook,
                    selected = uiState.isFocusSession,
                    onClick = { onSwitchMode(true) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("timer_focus_mode_pill")
                )
                ModePill(
                    title = "Break (${uiState.breakDurationMinutes}m)",
                    icon = Icons.Rounded.Coffee,
                    selected = !uiState.isFocusSession,
                    onClick = { onSwitchMode(false) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("timer_break_mode_pill")
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Countdown Card
            StudyCard(
                contentPadding = PaddingValues(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (uiState.isFocusSession) SoftSkyTint else SoftMintContainer
                    ) {
                        Text(
                            text = if (uiState.isFocusSession) {
                                "FOCUS SESSION • ${uiState.selectedSubject.uppercase()}"
                            } else {
                                "RECHARGE BREAK"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.isFocusSession) SkyBlueAccent else SoftMintSuccess,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Box(
                        modifier = Modifier.size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(220.dp)) {
                            val stroke = 14.dp.toPx()
                            drawArc(
                                color = BabyBlueContainer,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = if (uiState.isFocusSession) PastelBluePrimary else SoftMintSuccess,
                                startAngle = -90f,
                                sweepAngle = animatedSweep,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formattedClock,
                                fontFamily = MonoFontFamily,
                                fontSize = 44.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyHeading,
                                modifier = Modifier.testTag("timer_countdown_text")
                            )
                            Text(
                                text = if (uiState.isRunning) "In progress" else "Paused / Ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedBlueGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.isRunning) {
                            PastelPrimaryButton(
                                text = "Pause",
                                icon = Icons.Rounded.Pause,
                                onClick = onPause,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_pause_button")
                            )
                        } else {
                            PastelPrimaryButton(
                                text = "Start",
                                icon = Icons.Rounded.PlayArrow,
                                onClick = onStart,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("timer_start_button")
                            )
                        }
                        PastelOutlinedButton(
                            text = "Reset",
                            icon = Icons.Rounded.Refresh,
                            onClick = onReset,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("timer_reset_button")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subject Selector for Session Logging
            StudyCard(contentPadding = PaddingValues(16.dp)) {
                Text(
                    text = "Studying Subject",
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyHeading
                )
                Text(
                    text = "Completed focus sessions are logged to your Progress Tracker",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedBlueGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DefaultSubjects.forEach { subj ->
                        FilterChip(
                            selected = uiState.selectedSubject == subj,
                            onClick = { onSelectSubject(subj) },
                            label = { Text(subj) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PastelBluePrimary,
                                selectedLabelColor = PureWhiteSurface,
                                containerColor = SoftSkyTint,
                                labelColor = NavyHeading
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showSettingsDialog) {
        TimerDurationsDialog(
            initialFocus = uiState.focusDurationMinutes,
            initialBreak = uiState.breakDurationMinutes,
            onDismiss = { showSettingsDialog = false },
            onSave = { focusMin, breakMin ->
                onUpdateDurations(focusMin, breakMin)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
private fun ModePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PastelBluePrimary else PureWhiteSurface,
        border = BorderStroke(1.dp, if (selected) PastelBluePrimary else CardBorderBlue)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) PureWhiteSurface else SkyBlueAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) PureWhiteSurface else NavyHeading
            )
        }
    }
}

@Composable
private fun TimerDurationsDialog(
    initialFocus: Int,
    initialBreak: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var focusSlider by remember { mutableFloatStateOf(initialFocus.toFloat()) }
    var breakSlider by remember { mutableFloatStateOf(initialBreak.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PureWhiteSurface,
        title = {
            Text(
                text = "Customize Pomodoro",
                style = MaterialTheme.typography.titleLarge,
                color = NavyHeading
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Focus Duration: ${focusSlider.roundToInt()} minutes",
                    style = MaterialTheme.typography.titleSmall,
                    color = NavyHeading
                )
                Slider(
                    value = focusSlider,
                    onValueChange = { focusSlider = it },
                    valueRange = 5f..90f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = PastelBluePrimary,
                        activeTrackColor = PastelBluePrimary,
                        inactiveTrackColor = BabyBlueContainer
                    )
                )

                Text(
                    text = "Break Duration: ${breakSlider.roundToInt()} minutes",
                    style = MaterialTheme.typography.titleSmall,
                    color = NavyHeading
                )
                Slider(
                    value = breakSlider,
                    onValueChange = { breakSlider = it },
                    valueRange = 1f..30f,
                    steps = 28,
                    colors = SliderDefaults.colors(
                        thumbColor = SkyBlueAccent,
                        activeTrackColor = BabyBlueSecondary,
                        inactiveTrackColor = BabyBlueContainer
                    )
                )
            }
        },
        confirmButton = {
            PastelPrimaryButton(
                text = "Apply",
                onClick = { onSave(focusSlider.roundToInt(), breakSlider.roundToInt()) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedBlueGray)
            }
        }
    )
}
