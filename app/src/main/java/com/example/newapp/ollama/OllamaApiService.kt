package com.example.newapp.ollama // Declares the package name for this file

// Import necessary libraries for Retrofit
import retrofit2.Response // For handling API responses
import retrofit2.http.Body // For annotating request body parameters
import retrofit2.http.POST // For annotating POST request methods

/**
 * Retrofit interface for the Ollama API.
 * This interface defines the endpoints for communicating with the Ollama service.
 */
interface OllamaApiService { // Interface that Retrofit will implement at runtime
    /**
     * Generate a chat completion using the Ollama API.
     * This method sends a POST request to the Ollama API's chat endpoint.
     * 
     * @param request The chat completion request containing the model name, messages, and other parameters.
     * @return The chat completion response wrapped in a Retrofit Response object for error handling.
     */
    @POST("api/chat") // Annotation specifying this is a POST request to the "api/chat" endpoint
    suspend fun generateChatCompletion(@Body request: ChatCompletionRequest): Response<ChatCompletionResponse> // Suspend function that can be called from a coroutine
    // @Body annotation indicates that the request parameter should be serialized to JSON and sent as the request body
    // Returns a Response<ChatCompletionResponse> which includes both the response body and metadata like status code
}
