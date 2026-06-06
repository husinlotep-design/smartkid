package com.smartkids.launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkids.launcher.domain.model.AppModel
import com.smartkids.launcher.domain.repository.AppRepository
import com.smartkids.launcher.domain.usecase.GetApprovedAppsUseCase
import com.smartkids.launcher.service.ScreenTimeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LauncherUiState(
    val approvedApps: List<AppModel> = emptyList(),
    val isGrayscaleActive: Boolean = false,
    val screenTimeElapsed: Int = 0,
    val screenTimeLimit: Int = ScreenTimeManager.DEFAULT_DAILY_LIMIT_MINUTES,
    val isLoading: Boolean = true
)

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val getApprovedApps: GetApprovedAppsUseCase,
    private val repository: AppRepository,
    private val screenTimeManager: ScreenTimeManager
) : ViewModel() {

    val uiState: StateFlow<LauncherUiState> = combine(
        getApprovedApps(),
        screenTimeManager.uiState
    ) { apps, timeState ->
        LauncherUiState(
            approvedApps = apps,
            isGrayscaleActive = timeState.isLimitReached,
            screenTimeElapsed = timeState.elapsedMinutes,
            screenTimeLimit = timeState.limitMinutes,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState()
    )

    init {
        viewModelScope.launch { repository.syncInstalledApps() }
    }
}