package com.example.yourway.chat

import android.content.Context

object SharedPrefsHelper {
    private const val PREF_NAME = "UserPref"
    private const val KEY_USERNAME = "username"

    fun getUsername(context: Context): String? {
        val sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USERNAME, null)
    }
}
