package com.example.ui.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.StudyRepository
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class TimerUiState(
    val isFocusSession: Boolean = true,
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val focusDurationMinutes: Int = 25,
    val breakDurationMinutes: Int = 5,
    val selectedSubject: String = "General",
    val completedSessionsCount: Int = 0,
    val lastCompletedMessage: String? = null
)

class StudyTimerViewModel(
    private val studyRepository: StudyRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val prefsFlow: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    private var tickerJob: Job? = null
    private var endElapsedRealtimeMs: Long = 0L

    init {
        viewModelScope.launch {
            preferencesRepository.userPreferences.collect { prefs ->
                _uiState.update { current ->
                    if (!current.isRunning) {
                        val isAtStart = current.remainingSeconds == current.totalSeconds
                        val newTotal = if (current.isFocusSession) {
                            prefs.pomodoroFocusMinutes * 60
                        } else {
                            prefs.pomodoroBreakMinutes * 60
                        }
                        current.copy(
                            focusDurationMinutes = prefs.pomodoroFocusMinutes,
                            breakDurationMinutes = prefs.pomodoroBreakMinutes,
                            totalSeconds = newTotal,
                            remainingSeconds = if (isAtStart) newTotal else current.remainingSeconds
                        )
                    } else {
                        current.copy(
                            focusDurationMinutes = prefs.pomodoroFocusMinutes,
                            breakDurationMinutes = prefs.pomodoroBreakMinutes
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            studyRepository.allSessions.collect { sessions ->
                val startOfDay = startOfTodayMillis()
                val todayFocusCount = sessions.count {
                    it.sessionType == "FOCUS" && it.completedAtMillis >= startOfDay
                }
                _uiState.update { it.copy(completedSessionsCount = todayFocusCount) }
            }
        }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun startTimer() {
        if (_uiState.value.isRunning) return
        val remainingMs = _uiState.value.remainingSeconds * 1000L
        if (remainingMs <= 0L) return

        endElapsedRealtimeMs = SystemClock.elapsedRealtime() + remainingMs
        _uiState.update { it.copy(isRunning = true, lastCompletedMessage = null) }

        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val millisLeft = endElapsedRealtimeMs - SystemClock.elapsedRealtime()
                if (millisLeft <= 0L) {
                    onSessionFinished()
                    break
                }
                val secLeft = ((millisLeft + 999L) / 1000L).toInt()
                _uiState.update { it.copy(remainingSeconds = secLeft) }
                delay(250L)
            }
        }
    }

    fun pauseTimer() {
        tickerJob?.cancel()
        tickerJob = null
        val millisLeft = (endElapsedRealtimeMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        val secLeft = if (_uiState.value.isRunning) {
            ((millisLeft + 999L) / 1000L).toInt()
        } else {
            _uiState.value.remainingSeconds
        }
        _uiState.update {
            it.copy(
                isRunning = false,
                remainingSeconds = secLeft.coerceAtLeast(1)
            )
        }
    }

    fun resetTimer() {
        tickerJob?.cancel()
        tickerJob = null
        _uiState.update { state ->
            val targetSeconds = if (state.isFocusSession) {
                state.focusDurationMinutes * 60
            } else {
                state.breakDurationMinutes * 60
            }
            state.copy(
                isRunning = false,
                totalSeconds = targetSeconds,
                remainingSeconds = targetSeconds,
                lastCompletedMessage = null
            )
        }
    }

    fun switchMode(focusMode: Boolean) {
        tickerJob?.cancel()
        tickerJob = null
        _uiState.update { state ->
            val targetSeconds = if (focusMode) {
                state.focusDurationMinutes * 60
            } else {
                state.breakDurationMinutes * 60
            }
            state.copy(
                isFocusSession = focusMode,
                isRunning = false,
                totalSeconds = targetSeconds,
                remainingSeconds = targetSeconds,
                lastCompletedMessage = null
            )
        }
    }

    fun updateDurations(focusMinutes: Int, breakMinutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updateTimerDurations(focusMinutes, breakMinutes)
            if (!_uiState.value.isRunning) {
                val newTotal = if (_uiState.value.isFocusSession) focusMinutes * 60 else breakMinutes * 60
                _uiState.update {
                    it.copy(
                        focusDurationMinutes = focusMinutes,
                        breakDurationMinutes = breakMinutes,
                        totalSeconds = newTotal,
                        remainingSeconds = newTotal
                    )
                }
            }
        }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(lastCompletedMessage = null) }
    }

    private suspend fun onSessionFinished() {
        tickerJob = null
        val current = _uiState.value
        val wasFocus = current.isFocusSession
        val prefs = prefsFlow.first()

        if (wasFocus) {
            studyRepository.logStudySession(
                subject = current.selectedSubject,
                durationMinutes = current.focusDurationMinutes,
                sessionType = "FOCUS"
            )
        }

        if (prefs.notificationsEnabled && prefs.timerNotificationsEnabled) {
            notificationHelper.showTimerFinishedNotification(wasFocus, current.selectedSubject)
        }

        val nextIsFocus = !wasFocus
        val nextDurationSec = if (nextIsFocus) {
            current.focusDurationMinutes * 60
        } else {
            current.breakDurationMinutes * 60
        }

        _uiState.update {
            it.copy(
                isFocusSession = nextIsFocus,
                isRunning = false,
                totalSeconds = nextDurationSec,
                remainingSeconds = nextDurationSec,
                lastCompletedMessage = if (wasFocus) {
                    "Focus session complete! Switched to ${current.breakDurationMinutes}m break."
                } else {
                    "Break complete! Ready for a ${current.focusDurationMinutes}m focus session."
                }
            )
        }
    }

    private fun startOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    class Factory(
        private val studyRepository: StudyRepository,
        private val preferencesRepository: UserPreferencesRepository,
        private val notificationHelper: NotificationHelper
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudyTimerViewModel(studyRepository, preferencesRepository, notificationHelper) as T
        }
    }
}
