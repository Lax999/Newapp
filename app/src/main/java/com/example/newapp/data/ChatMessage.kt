package com.example.newapp.data // Declares the package name for this file

/**
 * Data class representing a chat message in the application.
 * This class is used to store and display messages in the chat UI.
 * 
 * @property id Unique identifier for the message
 * @property content The text content of the message
 * @property isFromUser Whether the message is from the user (true) or AI (false)
 * @property timestamp The time when the message was created
 */
data class ChatMessage( // Kotlin data class for representing a chat message
    val id: String = System.currentTimeMillis().toString(), // Generate a unique ID based on current time (default parameter)
    val content: String, // The actual text content of the message (required parameter)
    val isFromUser: Boolean, // Flag indicating if the message is from the user (true) or AI (false) (required parameter)
    val timestamp: Long = System.currentTimeMillis() // Timestamp when the message was created, defaults to current time
) // This class is used by the ChatViewModel and displayed by the ChatMessageItem composable
