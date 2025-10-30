package com.example.equa_notepad_plats

import android.app.Application
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.equa_notepad_plats.data.SupabaseClientProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class DeltimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SupabaseClientProvider.initialize(this)
    }
}