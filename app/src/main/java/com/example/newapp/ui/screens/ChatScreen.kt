package com.example.newapp.ui.screens // Declares the package name for this file

// Import necessary Compose libraries
import androidx.compose.foundation.layout.Column // For vertical arrangement of components
import androidx.compose.foundation.layout.Row // For horizontal arrangement of components
import androidx.compose.foundation.layout.fillMaxSize // For making a component fill the maximum size
import androidx.compose.foundation.layout.fillMaxWidth // For making a component fill the maximum width
import androidx.compose.foundation.layout.imePadding // For adding padding for the soft keyboard
import androidx.compose.foundation.layout.navigationBarsPadding // For adding padding for the navigation bar
import androidx.compose.foundation.layout.padding // For adding padding around components
import androidx.compose.foundation.lazy.LazyColumn // For creating a scrollable column with lazy loading
import androidx.compose.foundation.lazy.items // For iterating over items in a LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState // For remembering the scroll state of a LazyColumn
import androidx.compose.material.icons.Icons // For accessing Material icons
import androidx.compose.material.icons.filled.Send // For the send icon
import androidx.compose.material3.ExperimentalMaterial3Api // For marking experimental Material 3 APIs
import androidx.compose.material3.Icon // For displaying icons
import androidx.compose.material3.IconButton // For creating a button with an icon
import androidx.compose.material3.MaterialTheme // For accessing the Material Design theme
import androidx.compose.material3.OutlinedTextField // For creating a text input field with an outline
import androidx.compose.material3.Scaffold // For creating a basic Material Design layout structure
import androidx.compose.material3.Text // For displaying text
import androidx.compose.material3.TopAppBar // For creating a top app bar
import androidx.compose.material3.TopAppBarDefaults // For accessing default values for the top app bar
import androidx.compose.runtime.Composable // For marking a function as a Composable
import androidx.compose.runtime.LaunchedEffect // For running side effects when the composition is first launched
import androidx.compose.runtime.getValue // For property delegation in Compose
import androidx.compose.runtime.mutableStateOf // For creating a mutable state in Compose
import androidx.compose.runtime.remember // For remembering a value across recompositions
import androidx.compose.runtime.setValue // For property delegation in Compose
import androidx.compose.ui.Alignment // For aligning components
import androidx.compose.ui.Modifier // For modifying the appearance and behavior of components
import androidx.compose.ui.unit.dp // For specifying dimensions in density-independent pixels
import androidx.lifecycle.viewmodel.compose.viewModel // For creating a ViewModel in a Composable
import com.example.newapp.ui.components.ChatMessageItem // Import the ChatMessageItem composable
import com.example.newapp.viewmodel.ChatViewModel // Import the ChatViewModel
import com.example.newapp.viewmodel.ChatViewModelFactory // Import the ChatViewModelFactory
import android.app.Application // For accessing the Application context
import androidx.compose.ui.platform.LocalContext // For accessing the current context in a Composable

/**
 * Main chat screen composable that displays the chat interface.
 * This screen shows a list of messages and an input field for sending new messages.
 */
@OptIn(ExperimentalMaterial3Api::class) // Opt-in for experimental Material 3 APIs (needed for TopAppBar)
@Composable // Marks this function as a Composable that can be used in the UI
fun ChatScreen(
    modifier: Modifier = Modifier, // Optional modifier with a default value
    viewModel: ChatViewModel = viewModel(factory = ChatViewModelFactory(LocalContext.current.applicationContext as Application)) // Get or create the ChatViewModel with the application context
) {
    val listState = rememberLazyListState() // Remember the scroll state of the message list
    var inputText by remember { mutableStateOf("") } // Remember the text in the input field across recompositions

    // Scroll to bottom when new messages are added
    LaunchedEffect(viewModel.messages.size) { // Run this effect when the number of messages changes
        if (viewModel.messages.isNotEmpty()) { // Only scroll if there are messages
            listState.animateScrollToItem(viewModel.messages.size - 1) // Animate scrolling to the last message
        }
    }

    Scaffold( // Create a basic Material Design layout structure
        modifier = modifier.fillMaxSize(), // Make the scaffold fill the maximum size
        topBar = { // Define the top app bar
            TopAppBar( // Create a top app bar
                title = { Text("Koog AI Chat") }, // Set the title of the app bar
                colors = TopAppBarDefaults.topAppBarColors( // Set the colors of the app bar
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // Set the background color
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer // Set the title text color
                )
            )
        }
    ) { padding -> // Padding values provided by the Scaffold
        Column( // Vertical arrangement for the main content
            modifier = Modifier
                .fillMaxSize() // Make the column fill the maximum size
                .padding(padding) // Apply the padding from the Scaffold
        ) {
            // Messages list
            LazyColumn( // Create a scrollable column with lazy loading for the messages
                modifier = Modifier
                    .weight(1f) // Take up all available space
                    .fillMaxWidth(), // Make the column fill the maximum width
                state = listState // Use the remembered scroll state
            ) {
                items(viewModel.messages) { message -> // For each message in the list
                    ChatMessageItem(message = message) // Display a chat message item
                }
            }

            // Input field
            Row( // Horizontal arrangement for the input field and send button
                modifier = Modifier
                    .fillMaxWidth() // Make the row fill the maximum width
                    .padding(8.dp) // Add padding around the row
                    .navigationBarsPadding() // Add padding for the navigation bar
                    .imePadding(), // Add padding for the soft keyboard
                verticalAlignment = Alignment.CenterVertically // Center the items vertically
            ) {
                OutlinedTextField( // Create a text input field with an outline
                    value = inputText, // The current text value
                    onValueChange = { inputText = it }, // Update the text value when it changes
                    modifier = Modifier
                        .weight(1f) // Take up all available space
                        .padding(end = 8.dp), // Add padding to the right
                    placeholder = { Text("Type a message") }, // Placeholder text when the field is empty
                    maxLines = 3 // Limit the number of lines to 3
                )

                IconButton( // Create a button with an icon
                    onClick = { // What happens when the button is clicked
                        if (inputText.isNotBlank()) { // Only send if the input is not blank
                            viewModel.sendMessage(inputText) // Send the message to the ViewModel
                            inputText = "" // Clear the input field
                        }
                    }
                ) {
                    Icon( // Display an icon
                        imageVector = Icons.Default.Send, // Use the send icon
                        contentDescription = "Send", // Accessibility description
                        tint = MaterialTheme.colorScheme.primary // Set the icon color
                    )
                }
            }
        }
    }
} // End of ChatScreen composable
