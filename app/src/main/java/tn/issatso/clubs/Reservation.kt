package tn.issatso.clubs

class Reservation(
    val nom: String,
    val prenom: String,
    val atelier: Atelier,
    var ticketAttente: Int? = null  // <-- MUST be var
) {
    fun getStatut(): String {
        return if (ticketAttente == null) "Confirmé" else "Attente #$ticketAttente"
    }

    override fun toString(): String {
        return "$prenom $nom - ${atelier.nom} [${getStatut()}]"
    }
}