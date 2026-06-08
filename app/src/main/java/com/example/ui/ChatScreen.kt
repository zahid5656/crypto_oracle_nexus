package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CryptoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: CryptoViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isBengali by viewModel.isBengali.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isBengali) "ক্রিপ্টো ওরাকল চ্যাট" else "Crypto Oracle Chat") 
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.Home) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    TextButton(onClick = { viewModel.toggleLanguage() }) {
                        Text(if (isBengali) "ENG" else "বাংলা", color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.testTag("clear_chat_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Chat")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { 
                            Text(if (isBengali) "বাজার সম্পর্কে প্রশ্ন করুন..." else "Ask about the market...") 
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendChatMessage(inputText)
                                    inputText = ""
                                }
                            }
                        ),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendChatMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp).testTag("send_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message"
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (chatMessages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send, 
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isBengali) "কিভাবে সাহায্য করতে পারি?" else "How can I help you analyze the market?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chatMessages) { message ->
                    ChatBubble(message = message)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: CryptoViewModel.ChatMessage) {
    val arrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val textColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    AnimatedVisibility(
        visible = true, // We could add a state if we wanted entry animations
        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = arrangement
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .background(bubbleColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (message.isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = textColor,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analyzing...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = textColor
                    )
                }
            }
        }
    }
}
