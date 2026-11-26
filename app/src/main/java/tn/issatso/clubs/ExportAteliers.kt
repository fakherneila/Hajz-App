package tn.issatso.clubs

/**
 * Builds a plain-text export without Android dependencies.
 */
object ExportAteliers {
    /**
     * Generates the ordered plain-text workshop summary for Hajz-App sharing.
     */
    fun generer(ateliers: List<Atelier>): String = buildString {
        appendLine("Hajz-App")
        appendLine("Liste des ateliers")
        appendLine()
        ateliers.forEach { atelier ->
            appendLine("Atelier : ${atelier.nom}")
            appendLine("Club : ${atelier.club}")
            appendLine("Date : ${atelier.date}")
            appendLine("Salle : ${atelier.salle}")
            appendLine("Instructeur : ${atelier.instructeur}")
            appendLine("Capacité : ${atelier.capacite}")
            appendLine("Confirmés :")
            val confirmes = atelier.reservations.filter { it.ticketAttente == null }
            if (confirmes.isEmpty()) {
                appendLine("- Aucun")
            } else {
                confirmes.forEach { appendLine("- ${it.prenom} ${it.nom}") }
            }
            appendLine("Liste d'attente :")
            val attente = atelier.reservations
                .filter { it.ticketAttente != null }
                .sortedBy { it.ticketAttente }
            if (attente.isEmpty()) {
                appendLine("- Aucune")
            } else {
                attente.forEach { reservation ->
                    appendLine("#${reservation.ticketAttente} - ${reservation.prenom} ${reservation.nom}")
                }
            }
            appendLine()
        }
    }.trimEnd()
}
