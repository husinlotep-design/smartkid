package com.smartkids.launcher.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val messages      by viewModel.messages.collectAsState()
    val isThinking    by viewModel.isThinking.collectAsState()
    val isListening   by viewModel.isListening.collectAsState()
    val listState     = rememberLazyListState()

    var textInput     by remember { mutableStateOf("") }


    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text("←", color = Color.White, fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Jarvis avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C9A7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "J",
                    color = Color(0xFF0D1B2A),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text(
                    "Jarvis",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    when {
                        isListening -> "listening..."
                        isThinking  -> "thinking..."
                        else        -> "AI Assistant"
                    },
                    color = Color(0xFF00C9A7),
                    fontSize = 11.sp
                )
            }
        }

        // ── Chat messages ──────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }

            // Typing indicator dots shown while Jarvis is thinking
            if (isThinking) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C9A7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "J",
                                color = Color(0xFF0D1B2A),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                                .background(Color(0xFF1A2D40))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("• • •", color = Color(0xFF00C9A7), fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // ── Input bar ──────────────────────────────────────
        Surface(
            color = Color(0xFF111F2E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone button — turns teal when listening
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Color(0xFF00C9A7)
                            else Color(0xFF1A2D40)
                        )
                        .clickable {
                            if (isListening) viewModel.stopListening()
                            else viewModel.startListening()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (isListening) "⏹" else "🎙️",
                        fontSize = 18.sp
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Text input field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            "Ask Jarvis anything...",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00C9A7),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = Color(0xFF00C9A7)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput.trim())
                                textInput = ""
                            }
                        }
                    )
                )

                Spacer(Modifier.width(8.dp))

                // Send button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (textInput.isNotBlank()) Color(0xFF00C9A7)
                            else Color(0xFF1A2D40)
                        )
                        .clickable {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput.trim())
                                textInput = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", color = Color(0xFF0D1B2A), fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromJarvis)
            Arrangement.Start else Arrangement.End
    ) {
        if (message.isFromJarvis) {
            // Jarvis avatar beside each message
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00C9A7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "J",
                    color = Color(0xFF0D1B2A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .clip(
                    if (message.isFromJarvis)
                        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
                    else
                        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                )
                .background(
                    if (message.isFromJarvis) Color(0xFF1A2D40)
                    else Color(0xFF00C9A7)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isFromJarvis) Color.White
                else Color(0xFF0D1B2A),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}