package com.example.careerpilot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careerpilot.ui.CareerPilotApp
import com.example.careerpilot.ui.theme.CareerPilotTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Throwable) {
            Log.w("MainActivity", "Firebase initialization note: ${e.message}")
        }
        enableEdgeToEdge()
        setContent {
            CareerPilotTheme {
                CareerPilotApp()
            }
        }
    }
}
