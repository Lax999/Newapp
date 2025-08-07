package com.example.newapp.viewmodel // Declares the package name for this file

// Import necessary libraries and classes
import android.app.Application // For accessing application context
import android.util.Log // For logging messages to the Android logcat
import androidx.compose.runtime.mutableStateListOf // For creating observable state lists in Compose
import androidx.lifecycle.AndroidViewModel // Base class for ViewModels that need application context
import androidx.lifecycle.viewModelScope // Coroutine scope tied to the ViewModel lifecycle
import com.example.newapp.data.ChatMessage // Data class for chat messages
import com.example.newapp.koog.IntentKoogAgent // Agent for analyzing user intent
import com.example.newapp.koog.KoogAI // Core AI functionality
import com.example.newapp.koog.KoogAgent // Base agent implementation
import com.example.newapp.koog.TaskKoogAgent // Agent for handling map-related tasks
import com.example.newapp.map.MapIntentHandler // Handler for map-related intents
import kotlinx.coroutines.delay // For adding delays in coroutines
import kotlinx.coroutines.launch // For launching coroutines

/**
 * ViewModel for managing chat state and interactions with the Koog AI agents.
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) { // ViewModel that has access to the Application context

    // List of chat messages
    private val _messages = mutableStateListOf<ChatMessage>() // Mutable state list that triggers recomposition when modified
    val messages: List<ChatMessage> = _messages // Public immutable view of the messages list

    // Create the AI instance to be shared by all agents
    private val koogAI = KoogAI.create() // Create a single instance of KoogAI to be shared by all agents

    // Intent agent for analyzing user intent
    private val intentAgent = IntentKoogAgent.Builder() // Create a builder for the intent agent
        .setAI(koogAI) // Set the AI implementation
        .build() // Build the intent agent

    // Task agent for handling map-related tasks
    private val taskAgent = TaskKoogAgent.Builder() // Create a builder for the task agent
        .setAI(koogAI) // Set the AI implementation
        .build() // Build the task agent

    // General purpose agent for other queries
    private val generalAgent = KoogAgent.Builder() // Create a builder for the general agent
        .setAI(koogAI) // Set the AI implementation
        .build() // Build the general agent

    // Map intent handler for opening maps
    private val mapIntentHandler = MapIntentHandler(application) // Create a handler for map-related intents

    init { // Initialization block that runs when the ViewModel is created
        // Add a welcome message
        addMessage( // Add a welcome message to the chat
            ChatMessage( // Create a new chat message
                content = "Hello! I'm your AI assistant. How can I help you today?", // Welcome message content
                isFromUser = false // Message is from the AI, not the user
            )
        )

        // Initialize the connection with a test message
        viewModelScope.launch { // Launch a coroutine in the ViewModel scope
            try {
                // Wait for the app to initialize
                delay(2000) // Wait 2 seconds to ensure the app is fully initialized

                // Send a test message to warm up the connection
                Log.d("ChatViewModel", "Initializing connection to Ollama...") // Log initialization
                generalAgent.processInput("test") { response -> // Send a test message to the general agent
                    Log.d("ChatViewModel", "Connection test response: $response") // Log the response
                }
            } catch (e: Exception) { // Catch any exceptions that might occur
                Log.e("ChatViewModel", "Error initializing connection", e) // Log the error
            }
        }
    } // End of init block

    /**
     * Add a new message to the chat
     */
    fun addMessage(message: ChatMessage) { // Method to add a new message to the chat
        _messages.add(message) // Add the message to the mutable state list, which will trigger UI updates
    }

    /**
     * Send a user message and get AI response
     */
    fun sendMessage(content: String) { // Method to send a user message and get an AI response
        // Add user message
        val userMessage = ChatMessage( // Create a new chat message for the user's input
            content = content, // Set the content to the user's input
            isFromUser = true // Mark the message as from the user
        )
        addMessage(userMessage) // Add the user message to the chat

        // First, use the intent agent to determine if this is a map-related task
        intentAgent.processInput(content) { intentResponse -> // Send the user's input to the intent agent for analysis
            Log.d("ChatViewModel", "Intent analysis: $intentResponse") // Log the intent analysis response

            // Check if the response indicates a map task
            if (intentResponse.contains("MAP_TASK:", ignoreCase = true)) { // Check if the response contains the map task marker
                // Extract the destination
                val regex = "MAP_TASK:\\s*(.+)".toRegex(RegexOption.IGNORE_CASE) // Create a regex to match the MAP_TASK pattern
                val matchResult = regex.find(intentResponse) // Find the pattern in the response
                val destination = matchResult?.groupValues?.get(1)?.trim() ?: "" // Extract the destination or empty string if not found

                if (destination.isNotEmpty()) { // Check if a valid destination was found
                    // It's a map-related task with a valid destination
                    Log.d("ChatViewModel", "Detected map task with destination: $destination") // Log the detected destination

                    // Use the task agent to generate a response
                    taskAgent.processInput("I need directions to $destination") { taskResponse -> // Send a request to the task agent
                        // Add the task agent's response to the chat
                        val aiMessage = ChatMessage( // Create a new chat message for the AI's response
                            content = taskResponse, // Set the content to the task agent's response
                            isFromUser = false // Mark the message as from the AI
                        )
                        addMessage(aiMessage) // Add the AI message to the chat

                        // Open the maps app with the destination
                        val success = mapIntentHandler.openMapsWithDirections(destination) // Try to open the maps app
                        if (!success) { // If opening the maps app failed
                            // If opening the maps app failed, add an error message
                            val errorMessage = ChatMessage( // Create a new chat message for the error
                                content = "Sorry, I couldn't open the maps app. Please make sure you have a maps app installed.", // Error message
                                isFromUser = false // Mark the message as from the AI
                            )
                            addMessage(errorMessage) // Add the error message to the chat
                        }
                    }
                } else { // If no valid destination was found
                    // It's a map-related task but we couldn't extract the destination
                    val errorMessage = ChatMessage( // Create a new chat message for the error
                        content = "I understand you want directions, but I couldn't determine the destination. Could you please specify where you want to go?", // Error message
                        isFromUser = false // Mark the message as from the AI
                    )
                    addMessage(errorMessage) // Add the error message to the chat
                }
            } else { // If it's not a map-related task
                // It's not a map-related task, so use the general agent to generate a response
                generalAgent.processInput(content) { response -> // Send the user's input to the general agent
                    val aiMessage = ChatMessage( // Create a new chat message for the AI's response
                        content = response, // Set the content to the general agent's response
                        isFromUser = false // Mark the message as from the AI
                    )
                    addMessage(aiMessage) // Add the AI message to the chat
                }
            }
        }
    } // End of sendMessage method

    override fun onCleared() { // Called when the ViewModel is being destroyed
        super.onCleared() // Call the parent class implementation
        // Clean up resources
        intentAgent.shutdown() // Shutdown the intent agent
        taskAgent.shutdown() // Shutdown the task agent
        generalAgent.shutdown() // Shutdown the general agent
    } // End of onCleared method
}
