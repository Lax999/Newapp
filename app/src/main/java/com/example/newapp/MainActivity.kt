package com.example.newapp // Declares the package name for this file

// Import necessary libraries and classes
import android.os.Bundle // For handling saved instance state
import androidx.activity.ComponentActivity // Base class for activities using Jetpack Compose
import androidx.activity.compose.setContent // For setting the Compose content in the activity
import androidx.compose.foundation.layout.fillMaxSize // For making a composable fill the entire available space
import androidx.compose.ui.Modifier // For applying modifiers to composables
import com.example.newapp.ui.screens.ChatScreen // The main chat screen composable
import com.example.newapp.ui.theme.NewAppTheme // The app's theme defined using Compose

/**
 * Main activity for the application.
 * This is the entry point of the app and sets up the UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() { // Define the main activity class extending ComponentActivity
    override fun onCreate(savedInstanceState: Bundle?) { // Called when the activity is first created
        super.onCreate(savedInstanceState) // Call the parent class implementation
        setContent { // Set the Compose content for this activity
            NewAppTheme { // Apply the app's theme to all composables within
                // Display the ChatScreen composable, making it fill the entire screen
                ChatScreen(modifier = Modifier.fillMaxSize()) // Pass a modifier to make the chat screen fill the entire available space
            }
        }
    }
}
