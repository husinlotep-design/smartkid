package com.smartkids.launcher.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit,
    onOpenJarvis: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .grayscaleDetox(state.isGrayscaleActive) // Use the new name here
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            ScreenTimeHeader(
                elapsed = state.screenTimeElapsed,
                limit = state.screenTimeLimit,
                isLimitReached = state.isGrayscaleActive
            )

            AppGrid(
                apps = state.approvedApps,
                modifier = Modifier.weight(1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Jarvis button
                FloatingActionButton(
                    onClick = onOpenJarvis,
                    containerColor = Color(0xFF00C9A7),
                    modifier = Modifier.size(56.dp)
                ) {
                    Text("J", color = Color(0xFF0D1B2A), fontSize = 22.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }

                // Settings button
                SmallFloatingActionButton(onClick = onOpenSettings) {
                    Text("⚙")
                }
            }
        }

        AnimatedVisibility(
            visible = state.isGrayscaleActive,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            DetoxWarningBanner()
        }
    }
}

@Composable
fun ScreenTimeHeader(elapsed: Int, limit: Int, isLimitReached: Boolean) {
    val safe = limit.toFloat().coerceAtLeast(1f)
    val progress = (elapsed.toFloat() / safe).coerceIn(0f, 1f)
    val barColor = when {
        isLimitReached   -> Color(0xFFE53935)
        progress > 0.75f -> Color(0xFFFFA726)
        else             -> Color(0xFF66BB6A)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Screen Time",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f))
            Text("$elapsed / $limit min",
                style = MaterialTheme.typography.labelMedium,
                color = barColor)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun DetoxWarningBanner() {
    Surface(
        color = Color(0xFFE53935).copy(alpha = 0.92f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "⏰ Screen time limit reached! Time for a break.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            modifier = Modifier.padding(12.dp)
        )
    }
}