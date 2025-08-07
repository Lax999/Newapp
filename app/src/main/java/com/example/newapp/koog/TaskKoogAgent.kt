package com.example.newapp.koog // Declares the package name for this file

// Import necessary libraries
import android.util.Log // For logging messages to the Android logcat
import kotlinx.coroutines.Dispatchers // For specifying the thread context for coroutines
import kotlinx.coroutines.launch // For launching coroutines
import kotlinx.coroutines.withContext // For switching coroutine context

/**
 * A specialized KoogAgent that handles map-related tasks.
 * This agent is responsible for generating instructions for map navigation.
 */
class TaskKoogAgent private constructor( // Private constructor to enforce use of Builder pattern
    ai: KoogAI, // The AI implementation to use for generating responses
    systemPrompt: String // The system prompt that defines the agent's behavior
) : KoogAgent(ai, systemPrompt) { // Inherits from KoogAgent base class

    private val TAG = "TaskKoogAgent" // Tag for logging messages from this class

    /**
     * Process user input to generate map task instructions
     *
     * @param input The user input to process
     * @param callback Callback function that receives the task instructions
     */
    override fun processInput(input: String, callback: (String) -> Unit) { // Override the base class method
        // Use the parent class implementation to get the task instructions
        super.processInput(input) { response -> // Call the parent method with a callback
            Log.d(TAG, "Task instructions: $response") // Log the response for debugging
            callback(response) // Pass the response to the original callback
        }
    }

    /**
     * Process a map task with a specific destination
     *
     * @param destination The destination for the map task
     * @param callback Callback function that receives the task instructions
     */
    fun processMapTask(destination: String, callback: (String) -> Unit) { // Method to process a map task with a specific destination
        scope.launch { // Launch a coroutine in the IO scope
            try {
                // Generate response using AI with the agent's system prompt and the destination
                val input = "I need directions to $destination" // Create input text with the destination
                val response = ai.generateResponse(input, systemPrompt) // Call the AI to generate a response

                Log.d(TAG, "Map task instructions for $destination: $response") // Log the response for debugging

                withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                    callback(response) // Invoke the callback with the response
                }
            } catch (e: Exception) { // Catch any exceptions that might occur
                Log.e(TAG, "Error processing map task", e) // Log the error
                withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                    callback("Sorry, there was an error processing your map request: ${e.message}") // Invoke the callback with an error message
                }
            }
        }
    }

    /**
     * Builder for creating TaskKoogAgent instances
     */
    class Builder : KoogAgent.Builder() { // Extends the base Builder class from KoogAgent
        init { // Initialization block that runs when the Builder is created
            // Set the default system prompt for task execution
            systemPrompt = """ // Multi-line string for the specialized system prompt
                You are a navigation assistant. Your job is to provide clear instructions for map navigation. // Define the assistant's role

                When the user asks for directions to a location, provide a helpful response that includes: // Instructions for responses
                1. Confirmation that you're opening the maps app for them // Response element 1
                2. The destination they're going to // Response element 2
                3. A brief, friendly message // Response element 3

                Examples: // Provide examples to guide the AI's responses
                User: "I need directions to Central Park" // Example 1
                Response: "I'll open the maps app for you with directions to Central Park. Enjoy your visit to this beautiful urban oasis!" // Expected response for Example 1

                User: "I need directions to the nearest grocery store" // Example 2
                Response: "I'll open the maps app for you with directions to the nearest grocery store. Happy shopping!" // Expected response for Example 2

                User: "I need directions to 123 Main Street" // Example 3
                Response: "I'll open the maps app for you with directions to 123 Main Street. Have a safe journey!" // Expected response for Example 3
            """.trimIndent() // Remove leading/trailing whitespace from the multi-line string
        }

        override fun build(): TaskKoogAgent { // Override the build method to return a TaskKoogAgent
            requireNotNull(ai) { "AI must be set" } // Ensure the AI is set before building
            return TaskKoogAgent(ai!!, systemPrompt) // Create and return a new TaskKoogAgent with the AI and system prompt
        }
    } // End of Builder class
} // End of TaskKoogAgent class
