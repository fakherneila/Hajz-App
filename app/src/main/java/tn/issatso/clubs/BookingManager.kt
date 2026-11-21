package tn.issatso.clubs

object BookingManager {

    val ateliers = mutableListOf<Atelier>()
    private var prochainTicket = 1

    init {
        initialiserDonnees()
    }

    private fun initialiserDonnees() {
        ateliers.add(Atelier(1, "CyberTrace ISSATSo", "Initiation au Pentesting", "15 Nov 2026", 3))
        ateliers.add(Atelier(2, "Microsoft ISSATSo Club", "Azure & Cloud Computing", "16 Nov 2026", 4))
        ateliers.add(Atelier(3, "Nateg", "Data Engineering Basics", "17 Nov 2026", 2))
        ateliers.add(Atelier(4, "IEEE ISSATSo", "Embedded Systems", "18 Nov 2026", 3))
        ateliers.add(Atelier(5, "ElectroDev", "PCB Design Masterclass", "19 Nov 2026", 2))
        ateliers.add(Atelier(6, "ATAST", "Innovation & Entrepreneurship", "20 Nov 2026", 5))
        ateliers.add(Atelier(7, "ARSII", "Network Security", "21 Nov 2026", 3))
        ateliers.add(Atelier(8, "IOT AND DATA SCIENCE", "Smart Home with ESP32", "22 Nov 2026", 4))
        ateliers.add(Atelier(9, "CRT", "First Aid & Rescue", "23 Nov 2026", 2))
        ateliers.add(Atelier(10, "SOS", "Mental Health Awareness", "24 Nov 2026", 5))
        ateliers.add(Atelier(11, "TPL", "Leadership & Team Management", "25 Nov 2026", 3))
    }
        // ACTION : Admin - Ajouter un atelier
    fun ajouterAtelier(club: String, nom: String, date: String, capacite: Int): Boolean {
        if (club.trim().isEmpty() || nom.trim().isEmpty() || date.trim().isEmpty() || capacite <= 0) {
            return false // Validation M5
        }
        val nouvelId = (ateliers.maxOfOrNull { it.id } ?: 0) + 1
        ateliers.add(Atelier(nouvelId, club.trim(), nom.trim(), date.trim(), capacite))
        return true
    }
    

    fun placesLibres(atelier: Atelier): Int {
        var confirmes = 0
        for (r in atelier.reservations) {
            if (r.ticketAttente == null) confirmes++
        }
        return (atelier.capacite - confirmes).coerceAtLeast(0)
    }

    fun reserver(nom: String, prenom: String, atelier: Atelier): String {
        if (nom.trim().isEmpty() || prenom.trim().isEmpty()) {
            return "Erreur : Le nom et le prénom sont obligatoires."
        }

        val dejaInscrit = atelier.reservations.any {
            it.nom.equals(nom.trim(), ignoreCase = true) &&
            it.prenom.equals(prenom.trim(), ignoreCase = true)
        }
        if (dejaInscrit) {
            return "Erreur : Cet étudiant est déjà inscrit à cet atelier."
        }

        val nouvelleReservation: Reservation
        if (placesLibres(atelier) > 0) {
            nouvelleReservation = Reservation(nom.trim(), prenom.trim(), atelier, null)
        } else {
            nouvelleReservation = Reservation(nom.trim(), prenom.trim(), atelier, prochainTicket)
            prochainTicket++
        }

        atelier.reservations.add(nouvelleReservation)
        return "Succès : Inscription enregistrée (${nouvelleReservation.getStatut()})"
    }

    fun annuler(reservation: Reservation) {
        val atelier = reservation.atelier
        val etaitConfirme = reservation.ticketAttente == null
        atelier.reservations.remove(reservation)
        if (etaitConfirme) {
            promouvoirPremier(atelier)
        }
    }

    private fun promouvoirPremier(atelier: Atelier) {
        if (placesLibres(atelier) <= 0) return

        var premier: Reservation? = null
        for (r in atelier.reservations) {
            if (r.ticketAttente != null) {
                if (premier == null || r.ticketAttente!! < premier.ticketAttente!!) {
                    premier = r
                }
            }
        }
        premier?.ticketAttente = null
    }
}