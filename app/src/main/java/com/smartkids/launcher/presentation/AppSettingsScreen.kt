package com.smartkids.launcher.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    viewModel: AppSettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val apps by viewModel.allApps.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parental Controls") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                },
                actions = {

                    IconButton(onClick = { showPinDialog = true }) {
                        Text("🔑")
                    }
                }
            )
        }
    ) { padding ->
        // Logic for handling the PIN change popup
        if (showPinDialog) {
            PinChangeDialog(
                onDismiss = { showPinDialog = false },
                onConfirm = { old, new ->
                    val success = viewModel.updateParentalPin(old, new)
                    if (success) {
                        showPinDialog = false
                    } else {

                    }
                }
            )
        }

        LazyColumn(contentPadding = padding) {

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security Configuration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showPinDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Parental PIN")
                    }
                }
                HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // APP LIST SECTION
            items(apps, key = { it.packageName }) { app ->
                AppControlRow(
                    app = app,
                    onToggle = {
                        viewModel.toggleApproval(app.packageName, app.isApproved)
                    },
                    onTimeLimitChange = { min ->
                        viewModel.updateTimeLimit(app.packageName, min)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun PinChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Security PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { if (it.length <= 4) oldPin = it },
                    label = { Text("Current PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it },
                    label = { Text("New 4-Digit PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(oldPin, newPin) }) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AppControlRow(
    app: com.smartkids.launcher.domain.model.AppModel,
    onToggle: () -> Unit,
    onTimeLimitChange: (Int) -> Unit
) {
    var showLimitSlider by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(app.appName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (app.dailyLimitMinutes == 0) "No limit"
                    else "Limit: ${app.dailyLimitMinutes} min/day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { showLimitSlider = !showLimitSlider }) {
                    Text("⏱")
                }
                Switch(
                    checked = app.isApproved,
                    onCheckedChange = { onToggle() }
                )
            }
        }

        if (showLimitSlider) {
            var sliderValue by remember { mutableFloatStateOf(app.dailyLimitMinutes.toFloat()) }
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    "Daily limit: ${sliderValue.toInt()} min",
                    style = MaterialTheme.typography.labelSmall
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = { onTimeLimitChange(sliderValue.toInt()) },
                    valueRange = 0f..120f,
                    steps = 23
                )
            }
        }
    }
}