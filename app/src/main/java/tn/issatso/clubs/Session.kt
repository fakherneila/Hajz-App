package tn.issatso.clubs

import android.content.Context

object Session {
    private const val PREFERENCES_NAME = "hajz_session"
    private const val CURRENT_USER_KEY = "current_user"

    fun setCurrentUser(context: Context, nom: String, prenom: String) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(CURRENT_USER_KEY, "${nom.trim()}|${prenom.trim()}")
            .apply()
    }

    fun getCurrentUser(context: Context): String? =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(CURRENT_USER_KEY, null)
            ?.takeIf { it.isNotBlank() }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(CURRENT_USER_KEY)
            .apply()
    }

    fun matches(context: Context, nom: String, prenom: String): Boolean {
        val currentUser = getCurrentUser(context) ?: return false
        return currentUser.equals("${nom.trim()}|${prenom.trim()}", ignoreCase = true)
    }
}
