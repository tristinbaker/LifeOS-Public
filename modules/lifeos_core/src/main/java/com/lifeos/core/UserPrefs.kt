package com.lifeos.core

import android.content.Context

class UserPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("lifeos_user_prefs", Context.MODE_PRIVATE)

    var birthYear: Int
        get() = prefs.getInt("birth_year", 0)
        set(value) { prefs.edit().putInt("birth_year", value).apply() }
}
