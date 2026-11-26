package tn.issatso.clubs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StateSerializerTest {
    @Test
    fun oldSavedDataUsesSafeInstructorAndRoomDefaults() {
        val oldData = "STATE|1\nUI|A|0|| |false|\nNEXT|12\n" +
            "W|1|CyberTrace ISSATSo|Initiation au Pentesting|20/11/2026|3|1"

        val restored = StateSerializer.deserialize(oldData)

        assertNotNull(restored)
        assertEquals("M. Trabelsi", restored!!.workshops.single().instructeur)
        assertEquals("I01", restored.workshops.single().salle)
    }
    @Test
    fun emptyStateRoundTrips() {
        val snapshot = StateSerializer.Snapshot(
            emptyList(),
            emptyMap(),
            12,
            StateSerializer.UiState("A", 0, "", null, false, null)
        )

        val restored = StateSerializer.deserialize(StateSerializer.serialize(snapshot))

        assertNotNull(restored)
        assertTrue(restored!!.workshops.isEmpty())
        assertEquals("A", restored.ui.activeView)
    }

    @Test
    fun namesWithDelimiterBackslashAndSpecialCharactersRoundTrip() {
        val workshop = Atelier(7, "Club | A\\B", "Nom | \nAtelier", "01/12/2026", 2)
        workshop.reservations += Reservation("Ben|Back\\Slash", "Élise\nNour", workshop, null)
        val snapshot = StateSerializer.Snapshot(
            listOf(workshop),
            mapOf(7 to 4),
            20,
            StateSerializer.UiState("B", 2, "texte|\\ligne", "Club | A\\B", true, 7)
        )

        val restored = StateSerializer.deserialize(StateSerializer.serialize(snapshot))!!

        assertEquals("Club | A\\B", restored.workshops.single().club)
        assertEquals("Nom | \nAtelier", restored.workshops.single().nom)
        assertEquals("Ben|Back\\Slash", restored.workshops.single().reservations.single().nom)
        assertEquals("Élise\nNour", restored.workshops.single().reservations.single().prenom)
        assertNull(restored.workshops.single().reservations.single().ticketAttente)
        assertEquals("texte|\\ligne", restored.ui.searchText)
        assertEquals(7, restored.ui.selectedWorkshopId)
    }

    @Test
    fun nullTicketsAndWaitingQueueRoundTrip() {
        val workshop = Atelier(3, "Club", "Atelier", "02/12/2026", 1)
        workshop.reservations += Reservation("Confirmé", "Ali", workshop, null)
        workshop.reservations += Reservation("Attente1", "Sara", workshop, 1)
        workshop.reservations += Reservation("Attente2", "Nour", workshop, 2)
        val snapshot = StateSerializer.Snapshot(
            listOf(workshop),
            mapOf(3 to 3),
            4,
            StateSerializer.UiState("C", 2, "", null, false, null)
        )

        val restored = StateSerializer.deserialize(StateSerializer.serialize(snapshot))!!
        val restoredWorkshop = restored.workshops.single()

        assertEquals(listOf(null, 1, 2), restoredWorkshop.reservations.map { it.ticketAttente })
        assertEquals(1, restoredWorkshop.reservations.count { it.ticketAttente == null })
        assertEquals(3, restored.ticketCounters[3])
    }

    @Test
    fun manyWorkshopsAndDeletedOrUserAddedStateRoundTrip() {
        val workshops = (1..30).map { Atelier(it, "Club $it", "Atelier $it", "20/11/2026", it % 50 + 1) }
        val snapshot = StateSerializer.Snapshot(
            workshops.take(29),
            emptyMap(),
            31,
            StateSerializer.UiState("A", 0, "", "Club 29", true, null)
        )

        val restored = StateSerializer.deserialize(StateSerializer.serialize(snapshot))!!

        assertEquals(29, restored.workshops.size)
        assertEquals(31, restored.nextAtelierId)
        assertEquals("Club 29", restored.ui.selectedClub)
        assertTrue(restored.ui.adminMode)
    }
}
