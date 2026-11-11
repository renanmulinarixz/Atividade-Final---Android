package br.com.cotemig.poketeam.data

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("poketeam_session", Context.MODE_PRIVATE)

    fun currentUserId(): Long? = preferences.getLong("user_id", -1L).takeIf { it > 0 }

    fun save(userId: Long) {
        preferences.edit().putLong("user_id", userId).commit()
    }

    fun clear() {
        preferences.edit().remove("user_id").commit()
    }
}
