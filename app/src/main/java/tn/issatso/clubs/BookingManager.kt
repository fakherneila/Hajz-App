package tn.issatso.clubs

object BookingManager {

    data class CancellationUndo(
        val reservation: Reservation,
        val atelier: Atelier,
        val originalIndex: Int,
        val promotedReservation: Reservation?,
        val promotedTicket: Int?
    )

    val ateliers = mutableListOf<Atelier>()
    private val prochainsTickets = mutableMapOf<Int, Int>()
    private var nextAtelierId = 12

    init {
        reinitialiserDonnees()
    }

    private fun initialiserDonnees() {
        ateliers.add(Atelier(1, "CyberTrace ISSATSo", "Initiation au Pentesting", "20/11/2026", 3, "Mme Ben Salah", "I01"))
        ateliers.add(Atelier(2, "Microsoft ISSATSo Club", "Azure & Cloud Computing", "22/11/2026", 4, "M. Trabelsi", "I02"))
        ateliers.add(Atelier(3, "Nateg", "Data Engineering Basics", "23/11/2026", 2, "Mme Dridi", "I03"))
        ateliers.add(Atelier(4, "IEEE ISSATSo", "Embedded Systems", "24/11/2026", 3, "M. Ayari", "I04"))
        ateliers.add(Atelier(5, "ElectroDev", "PCB Design Masterclass", "25/11/2026", 2, "Mme Gharbi", "I05"))
        ateliers.add(Atelier(6, "ATAST", "Innovation & Entrepreneurship", "26/11/2026", 5, "M. Kacem", "M01"))
        ateliers.add(Atelier(7, "ARSII", "Network Security", "27/11/2026", 3, "Mme Mansour", "M02"))
        ateliers.add(Atelier(8, "IOT AND DATA SCIENCE", "Smart Home with ESP32", "28/11/2026", 4, "M. Saidi", "M03"))
        ateliers.add(Atelier(9, "CRT", "First Aid & Rescue", "29/11/2026", 2, "Mme Ben Amor", "Amphi 01"))
        ateliers.add(Atelier(10, "SOS", "Mental Health Awareness", "30/11/2026", 5, "M. Riahi", "Amphi 02"))
        ateliers.add(Atelier(11, "TPL", "Leadership & Team Management", "01/12/2026", 3, "Mme Hmida", "Bibliothèque"))
        DateHelper.sortAteliers(ateliers)
    }

    /**
     * Restores the in-memory demo state and resets waiting-list ticket counters.
     */
    fun reinitialiserDonnees() {
        ateliers.clear()
        prochainsTickets.clear()
        nextAtelierId = 12
        initialiserDonnees()
    }

    /**
     * Returns and restores the in-memory state without recreating demo workshops.
     */
    fun getNextAtelierId(): Int = nextAtelierId
    fun getTicketCounters(): Map<Int, Int> = prochainsTickets.toMap()

    fun restaurerEtat(
        ateliersRestores: List<Atelier>,
        prochainsTicketsRestores: Map<Int, Int>,
        nextId: Int
    ) {
        ateliers.clear()
        ateliers.addAll(ateliersRestores)
        prochainsTickets.clear()
        prochainsTickets.putAll(prochainsTicketsRestores)
        nextAtelierId = nextId
    }

    /**
     * Adds a workshop when all fields are valid and no duplicate exists.
     */
    fun ajouterAtelier(club: String, nom: String, date: String, capacite: Int): Boolean {
        return ajouterAtelier(
            club,
            nom,
            date,
            capacite,
            "M. Trabelsi",
            "I20"
        ).startsWith("Atelier ajouté")
    }

    /**
     * Validates and adds a workshop with instructor and room details.
     *
     * @return a success message or the specific French validation error.
     */
    fun ajouterAtelier(
        club: String,
        nom: String,
        date: String,
        capacite: Int,
        instructeur: String,
        salle: String
    ): String {
        val normalizedClub = normalizeText(club)
        val normalizedName = normalizeText(nom)
        val normalizedDate = normalizeDate(date)
        val instructorError = validerInstructeur(instructeur)
        if (normalizedClub.isEmpty()) return "Le club est obligatoire"
        if (normalizedName.isEmpty()) return "Le nom de l’atelier est obligatoire"
        if (normalizedDate.isEmpty()) return "La date est obligatoire"
        if (capacite !in 1..50) return "La capacité doit être comprise entre 1 et 50"
        if (instructorError != null) return instructorError
        if (!Salles.estSalleValide(salle)) return "Choisissez une salle valide"
        val doublon = ateliers.any {
            normalizeText(it.club).lowercase() == normalizedClub.lowercase() &&
                normalizeText(it.nom).lowercase() == normalizedName.lowercase() &&
                normalizeDate(it.date) == normalizedDate
        }
        if (doublon) return "Cet atelier existe déjà"
        if (ateliers.any {
                normalizeDate(it.date) == normalizedDate &&
                    it.salle.trim() == salle.trim()
            }
        ) return "Salle déjà occupée à cette date"

        val nouvelId = nextAtelierId
        nextAtelierId++
        ateliers.add(
            Atelier(
                nouvelId,
                normalizedClub,
                normalizedName,
                normalizedDate,
                capacite,
                normalizeText(instructeur),
                salle.trim()
            )
        )
        return "Atelier ajouté"
    }

    /**
     * Validates a trimmed and collapsed instructor name.
     *
     * @return a French error string, or null when valid.
     */
    fun validerInstructeur(instructeur: String): String? {
        val normalized = normalizeText(instructeur)
        if (normalized.isEmpty()) return "L’instructeur est obligatoire"
        if (normalized.length < 2) return "Le nom de l’instructeur doit contenir au moins 2 caractères"
        if (normalized.length > 60) return "Le nom de l’instructeur ne doit pas dépasser 60 caractères"
        return null
    }

    /**
     * Updates editable workshop details without replacing or losing reservations.
     *
     * @return "Détails mis à jour" or the first specific validation error.
     */
    fun modifierDetails(
        atelierId: Int,
        club: String,
        nom: String,
        date: String,
        salle: String,
        instructeur: String
    ): String {
        val atelier = ateliers.firstOrNull { it.id == atelierId }
            ?: return "Atelier introuvable"
        val normalizedClub = normalizeText(club)
        val normalizedName = normalizeText(nom)
        val normalizedDate = normalizeDate(date)
        if (normalizedClub.isEmpty()) return "Le club est obligatoire"
        if (normalizedName.isEmpty()) return "Le nom de l’atelier est obligatoire"
        if (normalizedDate.isEmpty()) return "La date est obligatoire"
        val instructorError = validerInstructeur(instructeur)
        if (instructorError != null) return instructorError
        if (!Salles.estSalleValide(salle)) return "Choisissez une salle valide"
        val duplicate = ateliers.any {
            it.id != atelierId &&
                normalizeText(it.club).lowercase() == normalizedClub.lowercase() &&
                normalizeText(it.nom).lowercase() == normalizedName.lowercase() &&
                normalizeDate(it.date) == normalizedDate
        }
        if (duplicate) return "Cet atelier existe déjà"
        val conflict = ateliers.any {
            it.id != atelierId &&
                normalizeDate(it.date) == normalizedDate &&
                it.salle.trim() == salle.trim()
        }
        if (conflict) return "Salle déjà occupée à cette date"

        atelier.club = normalizedClub
        atelier.nom = normalizedName
        atelier.date = normalizedDate
        atelier.salle = salle.trim()
        atelier.instructeur = normalizeText(instructeur)
        return "Détails mis à jour"
    }

    private fun normalizeText(value: String): String =
        value.trim().replace(Regex("\\s+"), " ")

    private fun normalizeDate(value: String): String {
        val trimmed = value.trim()
        val slashParts = trimmed.split("/")
        if (slashParts.size == 3 &&
            slashParts.all { it.isNotBlank() } &&
            slashParts.all { it.all(Char::isDigit) }
        ) {
            val day = slashParts[0].padStart(2, '0')
            val month = slashParts[1].padStart(2, '0')
            val year = slashParts[2]
            return "$day/$month/$year"
        }
        val textParts = trimmed.split(Regex("\\s+"))
        if (textParts.size == 3) {
            val month = when (textParts[1].lowercase()) {
                "jan", "janv", "janvier" -> "01"
                "feb", "fev", "février", "fevrier" -> "02"
                "mar", "mars" -> "03"
                "apr", "avr", "avril" -> "04"
                "may", "mai" -> "05"
                "jun", "juin" -> "06"
                "jul", "juil", "juillet" -> "07"
                "aug", "aou", "août", "aout" -> "08"
                "sep", "sept", "septembre" -> "09"
                "oct", "octobre" -> "10"
                "nov", "novembre" -> "11"
                "dec", "déc", "decembre", "décembre" -> "12"
                else -> ""
            }
            if (month.isNotEmpty() && textParts[0].all(Char::isDigit) && textParts[2].all(Char::isDigit)) {
                return "${textParts[0].padStart(2, '0')}/$month/${textParts[2]}"
            }
        }
        return trimmed.lowercase()
    }

    /**
     * Returns the number of confirmed places still available for a workshop.
     */
    fun placesLibres(atelier: Atelier): Int {
        return (atelier.capacite - reservationsConfirmees(atelier)).coerceAtLeast(0)
    }

    /**
     * Counts waiting reservations with a smaller ticket number in the same workshop.
     */
    fun personnesDevant(reservation: Reservation): Int {
        val ticket = reservation.ticketAttente ?: return 0
        return reservation.atelier.reservations.count {
            val ticketAttente = it.ticketAttente
            ticketAttente != null && ticketAttente < ticket
        }
    }

    /**
     * Registers a student or appends them to the workshop FIFO waiting list.
     */
    fun reserver(nom: String, prenom: String, atelier: Atelier): String {
        val nomNettoye = nom.trim()
        val prenomNettoye = prenom.trim()
        if (nomNettoye.isEmpty() || prenomNettoye.isEmpty()) {
            return "Erreur : Le nom et le prénom sont obligatoires."
        }

        val dejaInscrit = atelier.reservations.any {
            it.nom.equals(nomNettoye, ignoreCase = true) &&
                it.prenom.equals(prenomNettoye, ignoreCase = true)
        }
        if (dejaInscrit) {
            return "Erreur : Cet étudiant est déjà inscrit à cet atelier."
        }

        val ticket = if (placesLibres(atelier) > 0) {
            null
        } else {
            val prochain = prochainsTickets.getOrDefault(atelier.id, 1)
            prochainsTickets[atelier.id] = prochain + 1
            prochain
        }

        val nouvelleReservation = Reservation(nomNettoye, prenomNettoye, atelier, ticket)
        atelier.reservations.add(nouvelleReservation)
        return "Succès : Inscription enregistrée (${nouvelleReservation.getStatut()})"
    }

    /**
     * Cancels a reservation and promotes the oldest waiting reservation if needed.
     */
    fun annuler(reservation: Reservation): CancellationUndo? {
        val atelier = reservation.atelier
        val etaitConfirme = reservation.ticketAttente == null
        val originalIndex = atelier.reservations.indexOf(reservation)
        if (originalIndex < 0) return null

        val promouvable = if (etaitConfirme) premierEnAttente(atelier) else null
        val promotedTicket = promouvable?.ticketAttente
        atelier.reservations.removeAt(originalIndex)
        promouvable?.ticketAttente = null
        return CancellationUndo(
            reservation,
            atelier,
            originalIndex,
            promouvable,
            promotedTicket
        )
    }

    /**
     * Restores a cancellation snapshot, including the previous promotion state.
     */
    fun restaurerAnnulation(undo: CancellationUndo) {
        undo.promotedReservation?.ticketAttente = undo.promotedTicket
        if (!undo.atelier.reservations.contains(undo.reservation)) {
            val index = undo.originalIndex.coerceIn(0, undo.atelier.reservations.size)
            undo.atelier.reservations.add(index, undo.reservation)
        }
    }

    /**
     * Changes capacity and promotes waiting students in ticket order when possible.
     *
     * @return the number of reservations promoted, or -1 when the capacity is invalid.
     */
    fun changerCapacite(atelier: Atelier, nouvelleCapacite: Int): Int {
         if (nouvelleCapacite !in 1..50 || nouvelleCapacite < reservationsConfirmees(atelier)) return -1
        atelier.capacite = nouvelleCapacite
        var promus = 0
        while (placesLibres(atelier) > 0) {
            val avant = premierEnAttente(atelier) ?: break
            avant.ticketAttente = null
            promus++
        }
        return promus
    }

    /**
     * Removes a workshop and every reservation attached to it.
     *
     * @return true when the workshop existed and was removed.
     */
    fun supprimerAtelier(atelier: Atelier): Boolean {
        val removed = ateliers.remove(atelier)
        if (removed) {
            atelier.reservations.clear()
            prochainsTickets.remove(atelier.id)
        }
        return removed
    }

    private fun reservationsConfirmees(atelier: Atelier): Int =
        atelier.reservations.count { it.ticketAttente == null }

    private fun premierEnAttente(atelier: Atelier): Reservation? =
        atelier.reservations
            .filter { it.ticketAttente != null }
            .minByOrNull { it.ticketAttente!! }
}