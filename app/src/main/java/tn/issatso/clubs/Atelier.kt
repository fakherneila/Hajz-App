package tn.issatso.clubs

class Atelier(
    val id: Int,
    val club: String,
    val nom: String,
    val date: String,
    val capacite: Int = 2
) {
    val reservations = mutableListOf<Reservation>()

    override fun toString(): String {
        return "$club - $nom ($date)"
    }
}