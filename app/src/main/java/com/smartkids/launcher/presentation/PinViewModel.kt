package com.smartkids.launcher.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartkids.launcher.data.PinManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PinState(
    val enteredPin: String  = "",
    val isError: Boolean    = false,
    val isUnlocked: Boolean = false
)

@HiltViewModel
class PinViewModel @Inject constructor(
    private val pinManager: PinManager
) : ViewModel() {

    private val _state = MutableStateFlow(PinState())
    val state: StateFlow<PinState> = _state.asStateFlow()

    fun isPinSet(): Boolean = pinManager.isPinSet()

    fun enterDigit(digit: String) {
        if (_state.value.isError) return
        val current = _state.value.enteredPin
        if (current.length >= 4) return
        val updated = current + digit
        _state.value = _state.value.copy(enteredPin = updated)
        if (updated.length == 4) {
            viewModelScope.launch { verify(updated) }
        }
    }

    fun deleteDigit() {
        if (_state.value.isError) return
        val current = _state.value.enteredPin
        if (current.isNotEmpty()) {
            _state.value = _state.value.copy(enteredPin = current.dropLast(1))
        }
    }

    fun clearError() {
        _state.value = PinState()
    }

    private fun verify(pin: String) {
        if (pinManager.verifyPin(pin)) {
            _state.value = _state.value.copy(isUnlocked = true)
        } else {
            _state.value = _state.value.copy(isError = true)
        }
    }
}