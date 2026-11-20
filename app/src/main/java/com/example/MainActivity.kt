package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ai.configureAppCheck
import com.example.ui.navigation.StudyBuddyApp
import com.example.ui.theme.StudyBuddyTheme

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before rendering UI or creating ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            StudyBuddyTheme {
                StudyBuddyApp(container = appContainer)
            }
        }
    }
}
