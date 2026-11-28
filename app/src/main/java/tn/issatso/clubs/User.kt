package tn.issatso.clubs

enum class Role {
    ETUDIANT,
    ADMIN
}

data class User(
    val username: String,
    val nom: String,
    val prenom: String,
    val role: Role
)
