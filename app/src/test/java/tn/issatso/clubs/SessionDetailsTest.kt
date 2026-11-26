package tn.issatso.clubs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SessionDetailsTest {
    @Before
    fun resetState() {
        BookingManager.reinitialiserDonnees()
    }

    @Test
    fun roomListHasExactSizeAndOrder() {
        assertEquals(45, Salles.salles.size)
        assertEquals("I01", Salles.salles.first())
        assertEquals("I20", Salles.salles[19])
        assertEquals("M01", Salles.salles[20])
        assertEquals("M20", Salles.salles[39])
        assertEquals(
            listOf("Bibliothèque", "Amphi 01", "Amphi 02", "L01", "L02"),
            Salles.salles.takeLast(5)
        )
    }

    @Test
    fun roomValidationIsTrimmedExactAndCaseSensitive() {
        assertTrue(Salles.estSalleValide(" I05 "))
        assertFalse(Salles.estSalleValide("I21"))
        assertFalse(Salles.estSalleValide("m01"))
        assertFalse(Salles.estSalleValide(""))
        assertFalse(Salles.estSalleValide("Inconnue"))
    }

    @Test
    fun instructorValidationCoversBlankShortAndLongNames() {
        assertTrue(BookingManager.validerInstructeur(" ") == "L’instructeur est obligatoire")
        assertTrue(BookingManager.validerInstructeur("A") ==
            "Le nom de l’instructeur doit contenir au moins 2 caractères")
        assertTrue(BookingManager.validerInstructeur("A".repeat(61).trim()) != null)
        assertNull(BookingManager.validerInstructeur("  Mme   Ben Salah  "))
    }

    @Test
    fun addWorkshopValidatesEveryField() {
        assertEquals("Le club est obligatoire",
            BookingManager.ajouterAtelier("", "Titre", "02/12/2026", 2, "Mme Test", "I10"))
        assertEquals("Le nom de l’atelier est obligatoire",
            BookingManager.ajouterAtelier("Club", "", "02/12/2026", 2, "Mme Test", "I10"))
        assertEquals("La date est obligatoire",
            BookingManager.ajouterAtelier("Club", "Titre", "", 2, "Mme Test", "I10"))
        assertEquals("La capacité doit être comprise entre 1 et 50",
            BookingManager.ajouterAtelier("Club", "Titre", "02/12/2026", 51, "Mme Test", "I10"))
        assertEquals("L’instructeur est obligatoire",
            BookingManager.ajouterAtelier("Club", "Titre", "02/12/2026", 2, " ", "I10"))
        assertEquals("Choisissez une salle valide",
            BookingManager.ajouterAtelier("Club", "Titre", "02/12/2026", 2, "Mme Test", "I21"))
    }

    @Test
    fun addWorkshopRejectsRoomConflictAndAddsValidSession() {
        val conflict = BookingManager.ajouterAtelier(
            "Nouveau Club", "Titre", "20/11/2026", 2, "Mme Test", "I01"
        )
        assertEquals("Salle déjà occupée à cette date", conflict)
        assertEquals(
            "Atelier ajouté",
            BookingManager.ajouterAtelier(
                "Nouveau Club", "Titre", "02/12/2026", 2, "  Mme   Test ", "I10"
            )
        )
        val added = BookingManager.ateliers.last()
        assertEquals("Mme Test", added.instructeur)
        assertEquals("I10", added.salle)
    }

    @Test
    fun editDetailsSuccessPreservesReservationsAndNoFalseSelfDuplicate() {
        val atelier = BookingManager.ateliers.first()
        BookingManager.reserver("Ali", "Sara", atelier)
        val reservationCount = atelier.reservations.size

        val result = BookingManager.modifierDetails(
            atelier.id, "  CyberTrace   ISSATSo ", " Initiation au Pentesting ",
            "20/11/2026", "I09", "  Mme   Ben Salah "
        )

        assertEquals("Détails mis à jour", result)
        assertEquals(reservationCount, atelier.reservations.size)
        assertEquals("I09", atelier.salle)
        assertEquals("Mme Ben Salah", atelier.instructeur)
    }

    @Test
    fun editDetailsRejectsEveryFieldAndDuplicateWorkshop() {
        val atelier = BookingManager.ateliers.first()
        assertEquals("Le club est obligatoire",
            BookingManager.modifierDetails(atelier.id, "", "Titre", "02/12/2026", "I10", "Mme Test"))
        assertEquals("Le nom de l’atelier est obligatoire",
            BookingManager.modifierDetails(atelier.id, "Club", "", "02/12/2026", "I10", "Mme Test"))
        assertEquals("La date est obligatoire",
            BookingManager.modifierDetails(atelier.id, "Club", "Titre", "", "I10", "Mme Test"))
        assertEquals("L’instructeur est obligatoire",
            BookingManager.modifierDetails(atelier.id, "Club", "Titre", "02/12/2026", "I10", " "))
        assertEquals("Choisissez une salle valide",
            BookingManager.modifierDetails(atelier.id, "Club", "Titre", "02/12/2026", "I21", "Mme Test"))
        assertEquals("Cet atelier existe déjà",
            BookingManager.modifierDetails(
                atelier.id,
                "Microsoft ISSATSo Club",
                "Azure & Cloud Computing",
                "22/11/2026",
                "I10",
                "Mme Test"
            ))
    }

    @Test
    fun editDetailsRejectsRoomConflictButAllowsNonConflictAndPastDate() {
        val atelier = BookingManager.ateliers.first()
        assertEquals("Salle déjà occupée à cette date",
            BookingManager.modifierDetails(
                atelier.id, "Club Modifié", "Titre Modifié",
                "22/11/2026", "I02", "Mme Test"
            ))
        assertEquals("Détails mis à jour",
            BookingManager.modifierDetails(
                atelier.id, "Club Modifié", "Titre Modifié",
                "01/01/2020", "L02", "Mme Test"
            ))
        assertEquals("01/01/2020", atelier.date)
    }
}
