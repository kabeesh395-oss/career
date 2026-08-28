package com.example.careerpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.careerpilot.ui.CareerPilotApp
import com.example.careerpilot.ui.theme.CareerPilotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareerPilotTheme {
                CareerPilotApp()
            }
        }
    }
}
