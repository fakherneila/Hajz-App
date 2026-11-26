package tn.issatso.clubs

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportAteliersTest {
    @Test
    fun exportStartsWithAppNameAndContainsWorkshopDetails() {
        val atelier = Atelier(1, "Club Test", "Atelier Kotlin", "21/11/2026", 3, "Mme Ben Salah", "I05")
        atelier.reservations.add(Reservation("Ali", "Sara", atelier, null))
        atelier.reservations.add(Reservation("Ben", "Nour", atelier, 2))
        atelier.reservations.add(Reservation("Chen", "Lea", atelier, 1))

        val text = ExportAteliers.generer(listOf(atelier))

        assertTrue(text.startsWith("Hajz-App"))
        assertTrue(text.contains("Atelier : Atelier Kotlin"))
        assertTrue(text.contains("Club : Club Test"))
        assertTrue(text.contains("Date : 21/11/2026"))
        assertTrue(text.contains("Capacité : 3"))
        assertTrue(text.contains("Salle : I05"))
        assertTrue(text.contains("Instructeur : Mme Ben Salah"))
        assertTrue(text.contains("- Sara Ali"))
        assertTrue(text.contains("#1 - Lea Chen"))
        assertTrue(text.contains("#2 - Nour Ben"))
        assertTrue(text.indexOf("#1") < text.indexOf("#2"))
    }

    @Test
    fun exportUsesEmptyLabelsWhenNoReservationsExist() {
        val text = ExportAteliers.generer(listOf(Atelier(1, "Club", "Nom", "21/11/2026", 2)))

        assertTrue(text.contains("Confirmés :"))
        assertTrue(text.contains("- Aucun"))
        assertTrue(text.contains("Liste d'attente :"))
        assertTrue(text.contains("- Aucune"))
        assertFalse(text.contains("Atelio"))
    }

    @Test
    fun exportKeepsWorkshopListOrderAndWaitingTicketOrder() {
        val first = Atelier(1, "Club A", "Premier", "21/11/2026", 1)
        val second = Atelier(2, "Club B", "Deuxième", "22/11/2026", 1)
        second.reservations.add(Reservation("Nom B", "Prénom B", second, 2))
        second.reservations.add(Reservation("Nom A", "Prénom A", second, 1))

        val text = ExportAteliers.generer(listOf(first, second))

        assertTrue(text.indexOf("Atelier : Premier") < text.indexOf("Atelier : Deuxième"))
        assertTrue(text.indexOf("#1 - Prénom A Nom A") < text.indexOf("#2 - Prénom B Nom B"))
    }
}
