package com.example.acPolycoaters.Global_Classes

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatDelegate
import com.pixplicity.easyprefs.library.Prefs

class MyApp : Application() {
    companion object {

        var currentApp: Application? = null
            private set
    }
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()
        currentApp= this
        // Example: FirebaseApp.initializeApp(this)
    }
}
