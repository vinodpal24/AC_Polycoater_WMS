package com.example.acPolycoaters.Global_Classes

import android.app.Activity
import android.app.Application
import android.content.ContextWrapper
import com.pixplicity.easyprefs.library.Prefs

class MyApp : Application() {
    companion object {

        var currentApp: Application? = null
            private set
    }
    override fun onCreate() {
        super.onCreate()
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
