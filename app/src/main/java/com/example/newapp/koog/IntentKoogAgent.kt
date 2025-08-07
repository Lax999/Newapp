// this is an itent agent tailored to the map-related intent detection.


package com.example.newapp.koog // Declares the package name for this file

// Import necessary libraries
import android.util.Log // For logging messages to the Android logcat
import kotlinx.coroutines.Dispatchers // For specifying the thread context for coroutines
import kotlinx.coroutines.launch // For launching coroutines
import kotlinx.coroutines.withContext // For switching coroutine context

/**
 * A specialized KoogAgent that analyzes user input to determine intent.
 * This agent is responsible for identifying when the user wants to perform map-related tasks.
 */
class IntentKoogAgent private constructor( // Private constructor to enforce use of Builder pattern
    ai: KoogAI, // The AI implementation to use for generating responses
    systemPrompt: String // The system prompt that defines the agent's behavior
) : KoogAgent(ai, systemPrompt) { // Inherits from KoogAgent base class

    private val TAG = "IntentKoogAgent" // Tag for logging messages from this class

    /**
     * Process user input to determine intent and return the result via callback
     *
     * @param input The user input to analyze
     * @param callback Callback function that receives the intent analysis result
     */
    override fun processInput(input: String, callback: (String) -> Unit) { // Override the base class method
        // Use the parent class implementation to get the intent analysis
        super.processInput(input) { response -> // Call the parent method with a callback
            Log.d(TAG, "Intent analysis: $response") // Log the response for debugging
            callback(response) // Pass the response to the original callback
        }
    }

    /**
     * Process user input to determine if it's a map-related task
     *
     * @param input The user input to analyze
     * @param onMapTask Callback function that receives the map task details if detected
     * @param onNotMapTask Callback function that's called if no map task is detected
     */
    fun analyzeIntent( // Method to analyze user intent with separate callbacks for map tasks and non-map tasks
        input: String, // The user input to analyze
        onMapTask: (destination: String) -> Unit, // Callback for when a map task is detected
        onNotMapTask: () -> Unit // Callback for when no map task is detected
    ) {
        scope.launch { // Launch a coroutine in the IO scope
            try {
                // Generate response using AI with the agent's system prompt
                val response = ai.generateResponse(input, systemPrompt) // Call the AI to generate a response

                Log.d(TAG, "Intent analysis: $response") // Log the response for debugging

                // Parse the response to determine if it's a map task
                if (response.contains("MAP_TASK:", ignoreCase = true)) { // Check if the response contains the map task marker
                    // Extract the destination from the response
                    val destination = extractDestination(response) // Extract the destination using a helper method
                    if (destination.isNotEmpty()) { // Check if a valid destination was found
                        withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                            onMapTask(destination) // Call the map task callback with the destination
                        }
                        return@launch // Exit the coroutine early
                    }
                }

                // If we get here, it's not a map task
                withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                    onNotMapTask() // Call the non-map task callback
                }
            } catch (e: Exception) { // Catch any exceptions that might occur
                Log.e(TAG, "Error analyzing intent", e) // Log the error
                withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                    onNotMapTask() // Call the non-map task callback as a fallback
                }
            }
        }
    }

    /**
     * Extract the destination from the intent analysis response
     */
    private fun extractDestination(response: String): String { // Helper method to extract the destination from the response
        // Look for the destination in the response
        val regex = "MAP_TASK:\\s*(.+)".toRegex(RegexOption.IGNORE_CASE) // Create a regex to match the MAP_TASK pattern
        val matchResult = regex.find(response) // Find the pattern in the response
        return matchResult?.groupValues?.get(1)?.trim() ?: "" // Return the captured group (destination) or empty string if not found
    }

    /**
     * Builder for creating IntentKoogAgent instances
     */
    class Builder : KoogAgent.Builder() { // Extends the base Builder class from KoogAgent
        init { // Initialization block that runs when the Builder is created
            // Set the default system prompt for intent analysis
            systemPrompt = """ // Multi-line string for the specialized system prompt
                You are an intent analysis assistant. Your job is to determine if the user's message is asking for directions or location information. // Define the assistant's role

                If the user is asking for directions, respond with "MAP_TASK: [destination]" where [destination] is the location they want to go to. // Instructions for map tasks
                For example, if the user says "How do I get to Central Park?", respond with "MAP_TASK: Central Park". // Example of map task detection

                If the user is not asking for directions or location information, respond with a brief analysis of what they are asking for. // Instructions for non-map tasks

                Examples: // Provide examples to guide the AI's responses
                User: "How do I get to the nearest grocery store?" // Example 1: Map task
                Response: "MAP_TASK: nearest grocery store" // Expected response for Example 1

                User: "Can you show me directions to 123 Main Street?" // Example 2: Map task
                Response: "MAP_TASK: 123 Main Street" // Expected response for Example 2

                User: "What's the weather like today?" // Example 3: Non-map task
                Response: "The user is asking about weather information, not directions." // Expected response for Example 3

                User: "Tell me a joke" // Example 4: Non-map task
                Response: "The user is asking for entertainment, not directions." // Expected response for Example 4
            """.trimIndent() // Remove leading/trailing whitespace from the multi-line string
        }

        override fun build(): IntentKoogAgent { // Override the build method to return an IntentKoogAgent
            requireNotNull(ai) { "AI must be set" } // Ensure the AI is set before building
            return IntentKoogAgent(ai!!, systemPrompt) // Create and return a new IntentKoogAgent with the AI and system prompt
        }
    } // End of Builder class
}
