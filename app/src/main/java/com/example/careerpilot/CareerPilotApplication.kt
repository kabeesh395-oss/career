package com.example.careerpilot

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class CareerPilotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d("CareerPilotApp", "FirebaseApp successfully initialized.")
        } catch (e: Exception) {
            Log.w("CareerPilotApp", "FirebaseApp init note: ${e.message}")
        }
    }
}
