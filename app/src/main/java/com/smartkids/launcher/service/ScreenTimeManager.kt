package com.smartkids.launcher.service

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeManager @Inject constructor() {

    companion object {
        const val DEFAULT_DAILY_LIMIT_MINUTES = 60
    }

    data class ScreenTimeState(
        val elapsedMinutes: Int = 0,
        val limitMinutes: Int = DEFAULT_DAILY_LIMIT_MINUTES,
        val isLimitReached: Boolean = false,
        val isTracking: Boolean = false
    )

    private val _uiState = MutableStateFlow(ScreenTimeState())
    val uiState: StateFlow<ScreenTimeState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun startTracking(limitMinutes: Int = DEFAULT_DAILY_LIMIT_MINUTES) {
        if (trackingJob?.isActive == true) return

        _uiState.value = _uiState.value.copy(
            limitMinutes = limitMinutes,
            isTracking = true
        )

        trackingJob = scope.launch {
            while (isActive) {
                delay(60_000L)
                val current = _uiState.value
                val newElapsed = current.elapsedMinutes + 1
                _uiState.value = current.copy(
                    elapsedMinutes = newElapsed,
                    isLimitReached = newElapsed >= current.limitMinutes
                )
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        _uiState.value = _uiState.value.copy(isTracking = false)
    }

    fun resetForNewDay() {
        trackingJob?.cancel()
        _uiState.value = ScreenTimeState()
    }
}