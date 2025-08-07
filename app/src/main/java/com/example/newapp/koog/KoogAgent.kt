package com.example.newapp.koog // Declares the package name for this file

// Import necessary libraries for coroutines
import kotlinx.coroutines.CoroutineScope // For creating a coroutine scope
import kotlinx.coroutines.Dispatchers // For specifying the thread context for coroutines
import kotlinx.coroutines.launch // For launching coroutines
import kotlinx.coroutines.withContext // For switching coroutine context

/**
 * Implementation of the Koog Agent framework.
 * This is the base class for all agent types.
 */
open class KoogAgent protected constructor( // Open class that can be inherited, with protected constructor to enforce use of Builder
    protected val ai: KoogAI, // The AI implementation to use for generating responses
    protected val systemPrompt: String = "You are a helpful assistant." // The system prompt that defines the agent's behavior
) {

    protected val scope = CoroutineScope(Dispatchers.IO) // Create a coroutine scope for async operations on IO thread

    /**
     * Process user input and return AI response via callback
     */
    open fun processInput(input: String, callback: (String) -> Unit) { // Open function that can be overridden by subclasses
        scope.launch { // Launch a coroutine in the IO scope
            try {
                // Generate response using AI with the agent's system prompt
                val response = ai.generateResponse(input, systemPrompt) // Call the AI to generate a response

                // Call back on main thread
                withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                    callback(response) // Invoke the callback with the response
                }
            } catch (e: Exception) { // Catch any exceptions that might occur
                // Handle any errors
                withContext(Dispatchers.Main) { // Switch to the main thread for UI updates
                    callback("Sorry, there was an error: ${e.message}") // Invoke the callback with an error message
                }
            }
        }
    }

    /**
     * Shutdown the agent and release resources
     */
    fun shutdown() { // Method to clean up resources when the agent is no longer needed
        // In a real implementation, this would clean up resources
        // Currently a placeholder for future implementation
    }

    /**
     * Builder for creating KoogAgent instances
     */
    open class Builder { // Open inner class for the Builder pattern
        protected var ai: KoogAI? = null // The AI implementation to use, initially null
        protected var systemPrompt: String = "You are a helpful assistant." // Default system prompt

        /**
         * Set the AI to use with this agent
         */
        open fun setAI(ai: KoogAI): Builder { // Method to set the AI implementation
            this.ai = ai // Store the AI instance
            return this // Return this builder for method chaining
        }

        /**
         * Set the system prompt for this agent
         */
        open fun setSystemPrompt(systemPrompt: String): Builder { // Method to set the system prompt
            this.systemPrompt = systemPrompt // Store the system prompt
            return this // Return this builder for method chaining
        }

        /**
         * Build the KoogAgent
         */
        open fun build(): KoogAgent { // Method to create the KoogAgent instance
            requireNotNull(ai) { "AI must be set" } // Throw exception if AI is not set
            return KoogAgent(ai!!, systemPrompt) // Create and return a new KoogAgent instance
        }
    } // End of Builder class
} // End of KoogAgent class
