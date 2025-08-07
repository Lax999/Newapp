package com.example.newapp.ollama // Declares the package name for this file

/**
 * Request model for Ollama chat completions.
 * This class represents the JSON structure expected by the Ollama API for chat completion requests.
 *
 * @property model The name of the model to use (e.g., "llama2").
 * @property messages The list of messages in the conversation.
 * @property stream Whether to stream the response (default: false).
 */
data class ChatCompletionRequest( // Kotlin data class that will be serialized to JSON
    val model: String, // The name of the LLM model to use for generating the response
    val messages: List<ChatMessage>, // The conversation history as a list of messages
    val stream: Boolean = false // Whether to stream the response token by token (false means get the complete response at once)
)

/**
 * Represents a message in a chat conversation.
 * Each message has a role (who is speaking) and content (what they're saying).
 *
 * @property role The role of the message sender (e.g., "system", "user", "assistant").
 * @property content The content of the message.
 */
data class ChatMessage( // Kotlin data class for representing a single message in the conversation
    val role: String, // Who is speaking: "system" (instructions), "user" (the human), or "assistant" (the AI)
    val content: String // The actual text content of the message
)

/**
 * Response model for Ollama chat completions.
 * This class represents the JSON structure returned by the Ollama API for chat completion responses.
 *
 * @property model The name of the model used.
 * @property message The generated message.
 * @property done Whether the generation is complete.
 */
data class ChatCompletionResponse( // Kotlin data class that will be deserialized from JSON
    val model: String, // The name of the model that generated the response
    val message: ChatMessage, // The generated message with role (usually "assistant") and content
    val done: Boolean // Whether the generation is complete (always true for non-streaming responses)
)
