package com.example.newapp.koog // Declares the package name for this file

// Import necessary libraries and classes
import android.util.Log // For logging messages to the Android logcat
import com.example.newapp.BuildConfig // To access build configuration values like API URLs
import com.example.newapp.ollama.ChatCompletionRequest // Data class for Ollama API request
import com.example.newapp.ollama.ChatMessage // Data class for chat messages in the request/response
import com.example.newapp.ollama.OllamaApiService // Retrofit interface for Ollama API
import kotlinx.coroutines.Dispatchers // For specifying the thread context for coroutines
import kotlinx.coroutines.delay // For adding delays in coroutines
import kotlinx.coroutines.withContext // For switching coroutine context
import okhttp3.OkHttpClient // HTTP client for network requests
import okhttp3.logging.HttpLoggingInterceptor // For logging HTTP requests and responses
import retrofit2.Retrofit // REST client for API calls
import retrofit2.converter.gson.GsonConverterFactory // JSON converter for Retrofit
import java.util.concurrent.TimeUnit // For specifying time units for timeouts

/**
 * Implementation of the Koog AI framework using Ollama API with various LLM models.
 * The implementation tries multiple models and API URLs to ensure robustness.
 */
class KoogAI private constructor() { // Private constructor to enforce singleton pattern through create() method
    private val TAG = "KoogAI" // Tag for logging messages from this class

    // Get the API URL from BuildConfig and add fallback URLs
    private val apiUrls = listOf(
        BuildConfig.OLLAMA_API_URL,  // Primary URL from secrets.properties
        "http://10.0.2.2:11434",     // Android emulator special IP for host's localhost
        "http://127.0.0.1:11434"     // Direct localhost (works on physical device if Ollama is on the device)
    ).distinct() // Remove duplicates in case BuildConfig URL is the same as one of the fallbacks

    // List of models to try in order of preference
    private val models = listOf(
        "llama3.2",     // Llama 3.2 model (exact name as mentioned by user)
        "llama3.2:8b",  // Llama 3.2 8B parameter model
        "llama3.2:latest", // Latest version of Llama 3.2 model
        "llama3",       // Llama 3 model
        "llama3:8b",    // Llama 3 8B parameter model
        "llama3:latest", // Latest version of Llama 3 model
        "llama3.1",     // Llama 3.1 model
        "llama3.1:8b",  // Llama 3.1 8B parameter model
        "llama3.1:latest", // Latest version of Llama 3.1 model
        "llama2",       // Llama 2 model
        "mistral",      // Mistral model
        "gemma:2b",     // Gemma 2B parameter model
        "phi",          // Phi model
        "orca-mini"     // Orca Mini model
    ) // List of models to try in sequence if previous ones fail

    /**
     * Process input and generate a response using Ollama models or fallback to mock responses
     * 
     * @param input The user input to process
     * @param systemPrompt The system prompt to use for the AI (default: "You are a helpful assistant.")
     * @return The AI's response
     */
    suspend fun generateResponse(input: String, systemPrompt: String = "You are a helpful assistant."): String { // Suspend function that can be called from a coroutine
        return withContext(Dispatchers.IO) { // Switch to IO dispatcher for network operations
            // Add a longer delay to ensure network is ready
            try {
                Log.d(TAG, "Adding a delay before connecting to Ollama") // Log the delay operation
                delay(2000) // 2000ms (2 second) delay to give more time for network initialization
            } catch (e: Exception) {
                Log.e(TAG, "Error during delay", e) // Log any errors during the delay
            }

            // Try each API URL until one works
            for (apiUrl in apiUrls) { // Iterate through each possible API URL
                try {
                    Log.d(TAG, "Trying to connect to Ollama at $apiUrl") // Log the current URL being tried
                    Log.d(TAG, "If you're running Ollama locally with 'ollama serve', make sure it's running in a separate terminal window") // Helpful user guidance

                    // Create OkHttpClient with logging
                    val logging = HttpLoggingInterceptor().apply { // Create a logging interceptor for debugging
                        level = HttpLoggingInterceptor.Level.BODY // Set logging level to show full request/response bodies
                    }

                    val client = OkHttpClient.Builder() // Create an HTTP client
                        .addInterceptor(logging) // Add the logging interceptor
                        .connectTimeout(15, TimeUnit.SECONDS) // Set connection timeout to 15 seconds
                        .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout to 60 seconds for large responses
                        .writeTimeout(30, TimeUnit.SECONDS)   // Set write timeout to 30 seconds
                        .build() // Build the OkHttpClient instance

                    Log.d(TAG, "Created OkHttpClient with increased timeouts") // Log client creation

                    // Create Retrofit instance
                    val retrofit = Retrofit.Builder() // Create a Retrofit builder
                        .baseUrl(apiUrl) // Set the base URL for API calls
                        .client(client) // Set the HTTP client
                        .addConverterFactory(GsonConverterFactory.create()) // Add Gson converter for JSON parsing
                        .build() // Build the Retrofit instance

                    Log.d(TAG, "Created Retrofit instance for $apiUrl") // Log Retrofit creation

                    // Create OllamaApiService
                    val ollamaService = retrofit.create(OllamaApiService::class.java) // Create the API service interface
                    Log.d(TAG, "Created OllamaApiService") // Log service creation

                    // Try each model until one works
                    for (model in models) { // Iterate through each model in the preference list
                        try {
                            Log.d(TAG, "Trying to use model: $model") // Log the current model being tried
                            Log.d(TAG, "Make sure this model is installed in Ollama. You can install it with 'ollama pull $model'") // Helpful user guidance

                            // Create request
                            val request = ChatCompletionRequest( // Create a new chat completion request
                                model = model, // Set the model name
                                messages = listOf( // Create a list of messages for the conversation
                                    ChatMessage( // System message defines the AI's behavior
                                        role = "system", // Role is "system" for instructions to the AI
                                        content = systemPrompt // Use the provided system prompt
                                    ),
                                    ChatMessage( // User message contains the actual query
                                        role = "user", // Role is "user" for the user's input
                                        content = input // Use the provided user input
                                    )
                                ),
                                stream = false // Don't stream the response, get it all at once
                            )

                            Log.d(TAG, "Sending request to Ollama: $request") // Log the request being sent

                            Log.d(TAG, "About to make API call to Ollama with model: $model") // Log before making the API call

                            try {
                                // Make API call
                                val response = ollamaService.generateChatCompletion(request) // Send the request to the Ollama API
                                Log.d(TAG, "API call completed with status code: ${response.code()}") // Log the response code

                                if (response.isSuccessful) { // Check if the HTTP response was successful (200-299)
                                    val body = response.body() // Get the response body
                                    Log.d(TAG, "Ollama response successful. Body: $body") // Log the response body

                                    if (body != null) { // Check if the body is not null
                                        if (body.message.content.isNotEmpty()) { // Check if the message content is not empty
                                            Log.d(TAG, "Successfully got response from model: $model") // Log success
                                            return@withContext body.message.content // Return the message content as the result
                                        } else {
                                            Log.e(TAG, "Empty content in response message") // Log empty content error
                                            // Try the next model instead of returning an error message immediately
                                            continue // Skip to the next model
                                        }
                                    } else {
                                        Log.e(TAG, "Response body is null") // Log null body error
                                        // Try the next model instead of returning an error message immediately
                                        continue // Skip to the next model
                                    }
                                } else {
                                    // Handle error response
                                    val errorBody = response.errorBody()?.string() ?: "Unknown error" // Get error details or default message
                                    Log.e(TAG, "HTTP error ${response.code()}: $errorBody") // Log the HTTP error

                                    // If model not found (404) or model loading error, try the next model
                                    if (response.code() == 404 || errorBody.contains("model") || errorBody.contains("not found")) {
                                        Log.w(TAG, "Model $model not found or not loaded, trying next model") // Log model not found
                                        continue // Skip to the next model
                                    }

                                    // For other errors, try the next model
                                    Log.w(TAG, "Error with model $model, trying next model") // Log general error
                                    continue // Skip to the next model
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Exception during API call for model $model", e) // Log any exceptions during the API call
                                // Try the next model
                                continue // Skip to the next model
                            }
                        } catch (e: Exception) {
                            // Log the error but continue to try the next model
                            Log.e(TAG, "Error using model $model", e) // Log any exceptions during model processing
                        }
                    }

                    // If all models failed, try the next URL
                    Log.w(TAG, "All models failed for URL $apiUrl, trying next URL") // Log that all models failed for this URL
                    continue // Skip to the next URL
                } catch (e: Exception) {
                    // Log the error but continue to try the next URL
                    Log.e(TAG, "Error connecting to $apiUrl", e) // Log connection error
                    Log.e(TAG, "Connection error details: ${e.message}") // Log detailed error message

                    // Provide more specific guidance based on the error
                    when { // Check error message for specific patterns to provide better guidance
                        e.message?.contains("Failed to connect", ignoreCase = true) == true -> {
                            Log.e(TAG, "Connection refused. Make sure Ollama is running with 'ollama serve' in a separate terminal") // Log connection refused guidance
                        }
                        e.message?.contains("timeout", ignoreCase = true) == true -> {
                            Log.e(TAG, "Connection timed out. The server might be busy or not responding") // Log timeout guidance
                        }
                        e.message?.contains("host", ignoreCase = true) == true -> {
                            Log.e(TAG, "Host not found. Check your network connection and make sure the URL is correct") // Log host not found guidance
                        }
                    }
                }
            }

            Log.w(TAG, "All Ollama API URLs failed, using mock implementation") // Log that all URLs failed
            // If all URLs failed, use the mock implementation
            getMockResponse(input) // Fall back to mock responses
        }
    } // End of generateResponse method




// Back up responses
    /**
     * Get mock response when API URL is not valid or connection fails
     */
    private fun getMockResponse(input: String): String { // Method to generate mock responses when API is unavailable
        Log.i(TAG, "Using mock response for input: $input") // Log that we're using a mock response

        // First message should always indicate that we're using a fallback
        if (input == "test") { // Special case for the test message sent during initialization
            // This is the test message from ChatViewModel initialization
            return "I'm currently in fallback mode. Unable to connect to Ollama. Please check that:\n" + // Return a helpful message
                   "1. Ollama is running with 'ollama serve' in a separate terminal\n" + // Step 1: Check if Ollama is running
                   "2. You have the llama3.2 model installed (run 'ollama list' to check)\n" + // Step 2: Check if the model is installed
                   "3. If not installed, run 'ollama pull llama3.2' to install it" // Step 3: Install the model if needed
        }

        // Add a prefix to all responses to indicate we're in fallback mode
        val prefix = "[FALL BACK MODEL] " // Prefix to indicate we're using the fallback mode

        val baseResponse = when { // Select a response based on the input content
            input.contains("hello", ignoreCase = true) -> "Hello! How can I help you today?" // Greeting response
            input.contains("how are you", ignoreCase = true) -> "I'm doing well, thank you for asking!" // Well-being response
            input.contains("weather", ignoreCase = true) -> "I don't have access to real-time weather data, but I can help you find a weather service." // Weather query response
            input.contains("name", ignoreCase = true) -> "I'm your Koog AI assistant." // Name query response
            input.contains("help", ignoreCase = true) -> "I'm here to help! What do you need assistance with?" // Help query response
            input.contains("ollama", ignoreCase = true) -> // Ollama-related query response
                "It looks like you're trying to use Ollama. Please check that:\n" + // Helpful message about Ollama
                "1. Ollama is installed on your machine\n" + // Step 1: Check installation
                "2. Ollama is running with 'ollama serve' in a separate terminal\n" + // Step 2: Check if it's running
                "3. You have the llama3.2 model installed (run 'ollama list' to check)\n" + // Step 3: Check model installation
                "4. If not installed, run 'ollama pull llama3.2' to install it\n" + // Step 4: Install model if needed
                "5. The API URL in secrets.properties is set to http://127.0.0.1:11434 (or http://10.0.2.2:11434 for emulators)" // Step 5: Check API URL
            input.contains("error", ignoreCase = true) || input.contains("issue", ignoreCase = true) || input.contains("problem", ignoreCase = true) -> // Error-related query response
                "I'm currently experiencing connection issues with the Ollama server. Please check that:\n" + // Connection issues message
                "1. Ollama is installed and running on your machine\n" + // Step 1: Check installation and running status
                "2. The correct model is available (try 'ollama list' in terminal)\n" + // Step 2: Check model availability
                "3. The API URL in secrets.properties is correct\n" + // Step 3: Check API URL
                "4. Your device can connect to the Ollama server" // Step 4: Check connectivity
            input.contains("model", ignoreCase = true) -> "I'm using a fallback mode because I couldn't connect to any Ollama models. Please make sure you have models installed with 'ollama pull llama3' or similar commands." // Model-related query response
            input.contains("connect", ignoreCase = true) -> "I'm having trouble connecting to the Ollama server. Please check your network connection and make sure Ollama is running." // Connection-related query response
            input.length < 5 -> "Could you please provide more details?" // Short input response
            else -> "That's interesting. Can you tell me more about that?" // Default response for unrecognized inputs
        }

        return prefix + baseResponse // Return the prefixed response
    }

    companion object { // Companion object for static methods and properties
        /**
         * Create a new instance of KoogAI
         */
        fun create(): KoogAI { // Factory method to create a new KoogAI instance
            return KoogAI() // Return a new instance of KoogAI
        }
    } // End of companion object
}
