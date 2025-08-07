package com.example.newapp.viewmodel // Declares the package name for this file

// Import necessary libraries and classes
import android.app.Application // For accessing application context
import androidx.lifecycle.ViewModel // Base class for ViewModels
import androidx.lifecycle.ViewModelProvider // Factory interface for creating ViewModels

/**
 * Factory for creating ChatViewModel instances with an Application parameter.
 * This is needed because ChatViewModel requires an Application parameter in its constructor.
 */
class ChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory { // Define the factory class that takes an Application parameter
    override fun <T : ViewModel> create(modelClass: Class<T>): T { // Override the create method from ViewModelProvider.Factory
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) { // Check if the requested ViewModel class is ChatViewModel or a subclass
            @Suppress("UNCHECKED_CAST") // Suppress the unchecked cast warning
            return ChatViewModel(application) as T // Create a new instance of ChatViewModel with the application parameter and cast it to the requested type
        }
        throw IllegalArgumentException("Unknown ViewModel class") // Throw an exception if the requested ViewModel class is not ChatViewModel or a subclass
    }
}
