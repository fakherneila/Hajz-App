package tn.issatso.clubs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookingManagerTest {

    @Before
    fun resetState() {
        BookingManager.reinitialiserDonnees()
    }

    private fun workshop(capacity: Int = 2): Atelier =
        Atelier(100, "Club test", "Atelier test", "01/12/2026", capacity)

    @Test
    fun reserveWithFreePlaceIsConfirmedAndDecreasesAvailability() {
        val atelier = workshop()

        BookingManager.reserver("Ben", "Ali", atelier)

        assertEquals(1, atelier.reservations.size)
        assertNull(atelier.reservations.single().ticketAttente)
        assertEquals(1, BookingManager.placesLibres(atelier))
    }

    @Test
    fun fullWorkshopAssignsIncreasingFifoTickets() {
        val atelier = workshop(1)
        BookingManager.reserver("Un", "A", atelier)

        BookingManager.reserver("Deux", "B", atelier)
        BookingManager.reserver("Trois", "C", atelier)

        assertEquals(1, atelier.reservations[1].ticketAttente)
        assertEquals(2, atelier.reservations[2].ticketAttente)
    }

    @Test
    fun cancellingConfirmedReservationPromotesSmallestTicket() {
        val atelier = workshop(1)
        BookingManager.reserver("Un", "A", atelier)
        BookingManager.reserver("Deux", "B", atelier)
        BookingManager.reserver("Trois", "C", atelier)
        val confirmee = atelier.reservations[0]

        BookingManager.annuler(confirmee)

        assertNull(atelier.reservations[0].ticketAttente)
        assertEquals(0, BookingManager.placesLibres(atelier))
    }

    @Test
    fun cancellingWaitingReservationDoesNotPromoteAnotherReservation() {
        val atelier = workshop(1)
        BookingManager.reserver("Un", "A", atelier)
        BookingManager.reserver("Deux", "B", atelier)
        BookingManager.reserver("Trois", "C", atelier)
        val attente = atelier.reservations[1]

        BookingManager.annuler(attente)

        assertEquals(2, atelier.reservations.size)
        assertEquals(2, atelier.reservations.single { it.prenom == "C" }.ticketAttente)
    }

    @Test
    fun duplicateReservationIsRejectedCaseInsensitively() {
        val atelier = workshop()
        BookingManager.reserver("Ben", "Ali", atelier)

        val result = BookingManager.reserver(" ben ", "ALI", atelier)

        assertTrue(result.startsWith("Erreur"))
        assertEquals(1, atelier.reservations.size)
    }

    @Test
    fun emptyNameIsRejected() {
        val atelier = workshop()

        val result = BookingManager.reserver(" ", "Ali", atelier)

        assertTrue(result.startsWith("Erreur"))
        assertTrue(atelier.reservations.isEmpty())
    }

    @Test
    fun increasingCapacityPromotesWaitingReservationsInOrder() {
        val atelier = workshop(1)
        BookingManager.reserver("Un", "A", atelier)
        BookingManager.reserver("Deux", "B", atelier)
        BookingManager.reserver("Trois", "C", atelier)

        val promoted = BookingManager.changerCapacite(atelier, 3)

        assertEquals(2, promoted)
        assertEquals(3, atelier.reservations.count { it.ticketAttente == null })
        assertTrue(atelier.reservations.all { it.ticketAttente == null })
    }

    @Test
    fun capacityCannotDropBelowConfirmedReservations() {
        val atelier = workshop(2)
        BookingManager.reserver("Un", "A", atelier)
        BookingManager.reserver("Deux", "B", atelier)

        val promoted = BookingManager.changerCapacite(atelier, 1)

        assertEquals(-1, promoted)
        assertEquals(2, atelier.capacite)
    }

    @Test
    fun duplicateWorkshopIsRejectedIgnoringCase() {
        assertFalse(
            BookingManager.ajouterAtelier(
                " cybertrace issatso ",
                "INITIATION AU PENTESTING",
                "20 nov 2026",
                3
            )
        )
        assertNotNull(BookingManager.ateliers.find { it.id == 1 })
    }

    @Test
    fun validWorkshopIsAddedWithUniqueId() {
        val added = BookingManager.ajouterAtelier("Nouveau Club", "Kotlin", "01/12/2026", 4)

        assertTrue(added)
        assertEquals(12, BookingManager.ateliers.last().id)
    }

    @Test
    fun workshopWithInvalidCapacityIsRejected() {
        assertFalse(BookingManager.ajouterAtelier("Club", "Nom", "01/12/2026", 0))
        assertFalse(BookingManager.ajouterAtelier("Club", "Nom", "02/12/2026", 51))
    }

    @Test
    fun workshopWithEmptyFieldsIsRejected() {
        assertFalse(BookingManager.ajouterAtelier("", "Nom", "01/12/2026", 2))
        assertFalse(BookingManager.ajouterAtelier("Club", "", "01/12/2026", 2))
        assertFalse(BookingManager.ajouterAtelier("Club", "Nom", "", 2))
    }

    @Test
    fun undoCancellationRestoresReservationAndPromotionExactly() {
        val atelier = workshop(1)
        BookingManager.reserver("Un", "A", atelier)
        BookingManager.reserver("Deux", "B", atelier)
        val confirmed = atelier.reservations[0]
        val waiting = atelier.reservations[1]

        val undo = BookingManager.annuler(confirmed)
        assertNotNull(undo)
        assertNull(waiting.ticketAttente)

        BookingManager.restaurerAnnulation(undo!!)

        assertEquals(listOf(confirmed, waiting), atelier.reservations)
        assertNull(confirmed.ticketAttente)
        assertEquals(1, waiting.ticketAttente)
    }

    @Test
    fun exactDuplicateWorkshopIsRejected() {
        assertFalse(BookingManager.ajouterAtelier("CyberTrace ISSATSo", "Initiation au Pentesting", "20/11/2026", 3))
    }

    @Test
    fun duplicateWorkshopWithDifferentCaseAndSpacesIsRejected() {
        assertFalse(BookingManager.ajouterAtelier("  cybertrace   ISSATSo ", " initiation   AU pentesting ", "20/11/2026", 2))
    }

    @Test
    fun sameClubAndNameWithDifferentDateIsAccepted() {
        assertTrue(BookingManager.ajouterAtelier("CyberTrace ISSATSo", "Initiation au Pentesting", "16/11/2026", 3))
    }

    @Test
    fun sameNameAndDateWithDifferentClubIsAccepted() {
        assertTrue(BookingManager.ajouterAtelier("Autre Club", "Initiation au Pentesting", "20/11/2026", 3))
    }

    @Test
    fun duplicateDemoWorkshopInTextDateFormatIsRejected() {
        assertFalse(BookingManager.ajouterAtelier("CyberTrace ISSATSo", "Initiation au Pentesting", "20 Nov 2026", 3))
    }

    @Test
    fun waitingPositionReturnsPeopleAheadInTicketOrder() {
        val atelier = workshop(1)
        BookingManager.reserver("Un", "A", atelier)
        BookingManager.reserver("Deux", "B", atelier)
        BookingManager.reserver("Trois", "C", atelier)
        BookingManager.reserver("Quatre", "D", atelier)

        assertEquals(0, BookingManager.personnesDevant(atelier.reservations[1]))
        assertEquals(1, BookingManager.personnesDevant(atelier.reservations[2]))
        assertEquals(2, BookingManager.personnesDevant(atelier.reservations[3]))
    }

    @Test
    fun badDateStringIsInvalidAndNeverCrashes() {
        assertNull(DateHelper.parseDate("pas une date"))
        assertNull(DateHelper.parseDate("31/02/2026"))
        assertEquals(DateRelation.INVALID, DateHelper.compareWithToday("n/a", "21/11/2026"))
    }

    @Test
    fun dateRelationsPastTodayAndFutureAreCorrect() {
        assertEquals(DateRelation.PAST, DateHelper.compareWithToday("20/11/2026", "21/11/2026"))
        assertEquals(DateRelation.TODAY, DateHelper.compareWithToday("21/11/2026", "21/11/2026"))
        assertEquals(DateRelation.FUTURE, DateHelper.compareWithToday("22/11/2026", "21/11/2026"))
    }

    @Test
    fun workshopsSortByDateAscending() {
        val list = mutableListOf(
            Atelier(1, "C", "Late", "22/11/2026", 2),
            Atelier(2, "C", "Invalid", "bad", 2),
            Atelier(3, "C", "Early", "20/11/2026", 2)
        )

        DateHelper.sortAteliers(list)

        assertEquals(listOf("Early", "Late", "Invalid"), list.map { it.nom })
    }

    @Test
    fun supprimerAtelierWithoutReservationsRemovesIt() {
        val atelier = workshop()
        BookingManager.ateliers.add(atelier)

        assertTrue(BookingManager.supprimerAtelier(atelier))
        assertFalse(BookingManager.ateliers.contains(atelier))
    }

    @Test
    fun supprimerAtelierWithReservationsClearsThem() {
        val atelier = workshop(2)
        BookingManager.ateliers.add(atelier)
        BookingManager.reserver("Ali", "Sara", atelier)
        BookingManager.reserver("Ben", "Nour", atelier)

        assertTrue(BookingManager.supprimerAtelier(atelier))
        assertTrue(atelier.reservations.isEmpty())
        assertFalse(BookingManager.ateliers.contains(atelier))
    }
}
