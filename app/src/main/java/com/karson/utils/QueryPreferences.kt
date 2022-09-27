package com.karson.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit

const val PREF_SEARCH_QUERY = "search_query"
const val PREF_LAST_RESULT_ID = "last_result_id"
const val PREF_IS_POLLING = "is_polling"

object QueryPreferences {

    private fun getPrefs(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun getValue(context: Context, key: String): String {
        return getPrefs(context).getString(key, "")!!
    }

    fun putValue(context: Context, key: String, value: String) {
        getPrefs(context).edit {
            putString(key, value)
        }
    }

    fun getBooleanValue(context: Context, key: String): Boolean {
        return getPrefs(context).getBoolean(key, false)
    }

    fun putBooleanValue(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit {
            putBoolean(key, value)
        }
    }

}