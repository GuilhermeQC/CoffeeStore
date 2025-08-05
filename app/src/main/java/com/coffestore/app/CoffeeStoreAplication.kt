package com.coffestore.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class CoffeeStoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
