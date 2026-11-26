package tn.issatso.clubs

class Atelier(
    val id: Int,
    var club: String,
    var nom: String,
    var date: String,
    var capacite: Int = 2,
    var instructeur: String = "M. Trabelsi",
    var salle: String = "I01"
) {
    val reservations = mutableListOf<Reservation>()

    override fun toString(): String {
        return "$club - $nom ($date)"
    }
}