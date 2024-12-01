package com.example.quantex

import android.content.Context

object ThemeUtils {
    fun loadNightModePref(context: Context): Boolean {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("night_mode", false)
    }

    fun saveNightModePref(context: Context, isNightMode: Boolean) {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean("night_mode", isNightMode)
            apply()
        }
    }
}