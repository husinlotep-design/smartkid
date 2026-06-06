package com.smartkids.launcher.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt


@Composable
fun PinScreen(
    viewModel: PinViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {

    val state by viewModel.state.collectAsState()


    val shakeX by animateFloatAsState(
        targetValue = if (state.isError) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 400
            0f   at 0
            -14f at 60
            14f  at 120
            -10f at 180
            10f  at 240
            -6f  at 300
            6f   at 350
            0f   at 400
        },
        label = "shake"
    )


    LaunchedEffect(state.isUnlocked) {
        if (state.isUnlocked) onSuccess()
    }

    // When an error occurs, wait 700ms then clear it automatically
    LaunchedEffect(state.isError) {
        if (state.isError) {
            delay(700)
            viewModel.clearError()
        }
    }

    // Full screen dark background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {

            // Lock icon at the top
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.isError) Color(0xFF3D1515)
                        else Color(0xFF0F2A3A)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (state.isError) "✗" else "🔒",
                    fontSize = 28.sp
                )
            }

            // Title
            Text(
                text = "Parent Access",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Subtitle — tells parent what to do
            Text(
                text = if (!viewModel.isPinSet())
                    "Enter 1234 to set up your PIN"
                else
                    "Enter your 4-digit PIN",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.45f)
            )

            // Error message — only visible when isError is true
            Box(modifier = Modifier.height(20.dp)) {
                if (state.isError) {
                    Text(
                        text = "Wrong PIN. Please try again.",
                        fontSize = 13.sp,
                        color = Color(0xFFEF6C6C),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Four PIN dots — shake when wrong PIN is entered
            // offset moves the Row left and right during the shake animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.offset {
                    IntOffset(shakeX.roundToInt(), 0)
                }
            ) {
                // Draw 4 dots — filled when a digit has been entered
                repeat(4) { index ->
                    val filled = index < state.enteredPin.length

                    // animateColorAsState smoothly changes dot color
                    val dotColor by animateColorAsState(
                        targetValue = when {
                            state.isError -> Color(0xFFEF6C6C)  // red on error
                            filled        -> Color(0xFF00C9A7)  // teal when filled
                            else          -> Color.White.copy(alpha = 0.2f) // dim unfilled
                        },
                        label = "dot_color_$index"
                    )

                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Number pad — 3 columns, 4 rows
            // Keys layout: 1-9, then blank/0/delete
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("",  "0", "⌫")
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                keyRows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        row.forEach { key ->
                            PinKeyButton(
                                label = key,
                                onClick = {
                                    when (key) {
                                        ""  -> { /* empty cell — no action */ }
                                        "⌫" -> viewModel.deleteDigit()
                                        else -> viewModel.enterDigit(key)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Cancel button — goes back to launcher
            Text(
                text = "Cancel",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.35f),
                modifier = Modifier
                    .clickable { onCancel() }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            )
        }
    }
}

// Single key button on the number pad
// Empty label = invisible cell (used for layout spacing)
@Composable
private fun PinKeyButton(
    label: String,
    onClick: () -> Unit
) {
    val isEmpty = label.isEmpty()

    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .then(
                // Only add border, background, and click handler for real keys
                if (!isEmpty) {
                    Modifier
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable(onClick = onClick)
                } else {
                    Modifier  // empty cell — no styling
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isEmpty) {
            Text(
                text = label,
                // Delete key is slightly smaller than number keys
                fontSize = if (label == "⌫") 22.sp else 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
        }
    }
}