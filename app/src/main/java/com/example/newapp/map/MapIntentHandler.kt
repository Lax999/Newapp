package com.example.newapp.map // Declares the package name for this file

// Import necessary libraries
import android.content.Context // For accessing application context
import android.content.Intent // For creating intents to launch activities
import android.net.Uri // For creating URIs for intents
import android.util.Log // For logging messages to the Android logcat

/**
 * Handler for map-related intents.
 * This class provides methods to open the maps app with specific destinations.
 */
class MapIntentHandler(private val context: Context) { // Class constructor that takes an Android context

    private val TAG = "MapIntentHandler" // Tag for logging messages from this class

    /**
     * Open the maps app with directions to the specified destination
     *
     * @param destination The destination to navigate to
     * @return true if the maps app was opened successfully, false otherwise
     */
    fun openMapsWithDirections(destination: String): Boolean { // Method to open maps app with directions
        try { // Try block to catch any exceptions
            // Encode the destination for use in a URI
            val encodedDestination = Uri.encode(destination) // URL-encode the destination string to handle special characters

            // Create a URI for Google Maps directions
            val uri = Uri.parse("google.navigation:q=$encodedDestination") // Create a URI using Google Maps navigation scheme

            // Create an intent to open the maps app
            val intent = Intent(Intent.ACTION_VIEW, uri) // Create an intent to view the URI
            intent.setPackage("com.google.android.apps.maps") // Specify Google Maps app as the target
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add flag for starting from non-activity context

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(context.packageManager) != null) { // Check if Google Maps is installed
                // Open the maps app
                context.startActivity(intent) // Launch Google Maps with the navigation intent
                Log.d(TAG, "Opened maps app with directions to: $destination") // Log success
                return true // Return success
            } else { // If Google Maps is not installed
                // Try a more generic intent that any maps app can handle
                val genericUri = Uri.parse("geo:0,0?q=$encodedDestination") // Create a URI using the generic geo scheme
                val genericIntent = Intent(Intent.ACTION_VIEW, genericUri) // Create an intent to view the generic URI
                genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add flag for starting from non-activity context

                if (genericIntent.resolveActivity(context.packageManager) != null) { // Check if any maps app is installed
                    context.startActivity(genericIntent) // Launch any available maps app
                    Log.d(TAG, "Opened generic maps app with directions to: $destination") // Log success
                    return true // Return success
                } else { // If no maps app is installed
                    // Try opening a web browser with Google Maps
                    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedDestination") // Create a URI for Google Maps website
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri) // Create an intent to view the web URI
                    webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add flag for starting from non-activity context

                    if (webIntent.resolveActivity(context.packageManager) != null) { // Check if a web browser is available
                        context.startActivity(webIntent) // Launch web browser with Google Maps website
                        Log.d(TAG, "Opened web browser with Google Maps for: $destination") // Log success
                        return true // Return success
                    } else { // If no web browser is available
                        Log.e(TAG, "No app found to handle maps intent for: $destination") // Log failure
                        return false // Return failure
                    }
                }
            }
        } catch (e: Exception) { // Catch any exceptions that might occur
            Log.e(TAG, "Error opening maps app", e) // Log the error
            return false // Return failure
        }
    } // End of openMapsWithDirections method
} // End of MapIntentHandler class
