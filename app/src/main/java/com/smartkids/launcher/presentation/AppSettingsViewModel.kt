package com.smartkids.launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkids.launcher.data.PinManager
import com.smartkids.launcher.domain.usecase.GetAllAppsUseCase
import com.smartkids.launcher.domain.usecase.SetAppTimeLimitUseCase
import com.smartkids.launcher.domain.usecase.UpdateApprovedAppUseCase
import com.smartkids.launcher.domain.model.AppModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val getAllApps: GetAllAppsUseCase,
    private val updateApproval: UpdateApprovedAppUseCase,
    private val setTimeLimit: SetAppTimeLimitUseCase,
    private val pinManager: PinManager
) : ViewModel() {

    val allApps: StateFlow<List<AppModel>> = getAllApps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleApproval(packageName: String, currentState: Boolean) {
        viewModelScope.launch { updateApproval(packageName, !currentState) }
    }

    fun updateTimeLimit(packageName: String, minutes: Int) {
        viewModelScope.launch { setTimeLimit(packageName, minutes) }
    }


    fun updateParentalPin(oldPin: String, newPin: String): Boolean {

        return pinManager.updatePin(oldPin, newPin)
    }
}