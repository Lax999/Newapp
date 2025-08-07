package com.example.newapp.ui.components // Declares the package name for this file

// Import necessary Compose libraries
import androidx.compose.foundation.background // For setting background colors
import androidx.compose.foundation.layout.Box // For creating a container with absolute positioning
import androidx.compose.foundation.layout.Column // For vertical arrangement of components
import androidx.compose.foundation.layout.fillMaxWidth // For making a component fill the maximum width
import androidx.compose.foundation.layout.padding // For adding padding around components
import androidx.compose.foundation.layout.widthIn // For constraining the width of a component
import androidx.compose.foundation.shape.RoundedCornerShape // For creating rounded corners
import androidx.compose.material3.MaterialTheme // For accessing the Material Design theme
import androidx.compose.material3.Text // For displaying text
import androidx.compose.runtime.Composable // For marking a function as a Composable
import androidx.compose.ui.Alignment // For aligning components
import androidx.compose.ui.Modifier // For modifying the appearance and behavior of components
import androidx.compose.ui.draw.clip // For clipping a component to a shape
import androidx.compose.ui.unit.dp // For specifying dimensions in density-independent pixels
import com.example.newapp.data.ChatMessage // Import the ChatMessage data class

/**
 * Composable for displaying a single chat message.
 * This component renders a chat bubble with different styles for user and AI messages.
 * 
 * @param message The chat message to display
 * @param modifier Optional modifier for customizing the layout
 */
@Composable // Marks this function as a Composable that can be used in the UI
fun ChatMessageItem(
    message: ChatMessage, // The message to display
    modifier: Modifier = Modifier // Optional modifier with a default value
) {
    Column( // Vertical arrangement for the message
        modifier = modifier // Apply the provided modifier
            .fillMaxWidth() // Make the column fill the maximum width
            .padding(horizontal = 8.dp, vertical = 4.dp), // Add padding around the message
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start // Align user messages to the right, AI messages to the left
    ) {
        Box( // Container for the message bubble
            modifier = Modifier
                .widthIn(max = 300.dp) // Limit the maximum width of the message bubble
                .clip( // Clip the box to a rounded corner shape
                    RoundedCornerShape( // Create a rounded corner shape for the message bubble
                        topStart = 16.dp, // Round the top-left corner
                        topEnd = 16.dp, // Round the top-right corner
                        bottomStart = if (message.isFromUser) 16.dp else 0.dp, // Round the bottom-left corner for user messages only
                        bottomEnd = if (message.isFromUser) 0.dp else 16.dp // Round the bottom-right corner for AI messages only
                    )
                )
                .background( // Set the background color of the message bubble
                    if (message.isFromUser) // Different colors for user and AI messages
                        MaterialTheme.colorScheme.primary // User messages use the primary color
                    else 
                        MaterialTheme.colorScheme.secondaryContainer // AI messages use the secondary container color
                )
                .padding(12.dp) // Add padding inside the message bubble
        ) {
            Text( // Display the message text
                text = message.content, // The content of the message
                color = if (message.isFromUser) // Different text colors for user and AI messages
                    MaterialTheme.colorScheme.onPrimary // User message text color (on primary background)
                else 
                    MaterialTheme.colorScheme.onSecondaryContainer, // AI message text color (on secondary container background)
                style = MaterialTheme.typography.bodyLarge // Use the body large text style from the theme
            )
        }
    }
} // End of ChatMessageItem composable
