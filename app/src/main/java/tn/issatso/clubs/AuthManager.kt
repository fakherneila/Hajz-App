package tn.issatso.clubs

import android.content.Context

object AuthManager {
    private const val PREFERENCES_NAME = "hajz_session"
    private const val USERNAME_KEY = "user_username"
    private const val NOM_KEY = "user_nom"
    private const val PRENOM_KEY = "user_prenom"
    private const val ROLE_KEY = "user_role"

    val users = listOf(
        User("etudiant1", "Ben Salah", "Ali", Role.ETUDIANT),
        User("etudiant2", "Trabelsi", "Sami", Role.ETUDIANT),
        User("admin", "Admin", "Administrateur", Role.ADMIN)
    )

    fun login(username: String, password: String): User? {
        val user = users.firstOrNull { it.username == username.trim() } ?: return null
        val expectedPassword = if (user.role == Role.ADMIN) "admin123" else user.username
        return user.takeIf { password == expectedPassword }
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun getCurrentUser(context: Context): User? {
        val preferences =
            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val username = preferences.getString(USERNAME_KEY, null) ?: return null
        val nom = preferences.getString(NOM_KEY, null) ?: return null
        val prenom = preferences.getString(PRENOM_KEY, null) ?: return null
        val role = preferences.getString(ROLE_KEY, null) ?: return null
        return users.firstOrNull {
            it.username == username &&
                it.nom == nom &&
                it.prenom == prenom &&
                it.role.name == role
        }
    }

    fun saveUser(context: Context, user: User) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(USERNAME_KEY, user.username)
            .putString(NOM_KEY, user.nom)
            .putString(PRENOM_KEY, user.prenom)
            .putString(ROLE_KEY, user.role.name)
            .apply()
    }
}
