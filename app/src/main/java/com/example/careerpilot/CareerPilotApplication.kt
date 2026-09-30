package com.example.careerpilot

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class CareerPilotApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("CareerPilotApp", "FirebaseApp successfully initialized.")
        } catch (e: Throwable) {
            Log.w("CareerPilotApp", "FirebaseApp init note: ${e.message}")
        }
    }
}
