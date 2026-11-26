package tn.issatso.clubs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltreAteliersTest {
    private val ateliers = listOf(
        Atelier(1, "CyberTrace ISSATSo", "Initiation au Pentesting", "20/11/2026", 3, "Mme Ben Salah", "I05"),
        Atelier(2, "Équipe Éclair", "Programmation Kotlin", "21/11/2026", 4, "M. Trabelsi", "M02"),
        Atelier(3, "Data Club", "Big Data avancé", "22/11/2026", 2, "Mme Dridi", "Amphi 01")
    )

    @Test
    fun searchOnlyMatchesNameOrClub() {
        assertEquals(listOf("Initiation au Pentesting"), FiltreAteliers.filtrer(ateliers, "pentest", null).map { it.nom })
        assertEquals(listOf("Big Data avancé"), FiltreAteliers.filtrer(ateliers, "data club", null).map { it.nom })
    }

    @Test
    fun chipOnlyFiltersBySelectedClub() {
        assertEquals(
            listOf("Initiation au Pentesting"),
            FiltreAteliers.filtrer(ateliers, "", "CyberTrace ISSATSo").map { it.nom }
        )
    }

    @Test
    fun searchAndChipCombine() {
        assertEquals(
            listOf("Programmation Kotlin"),
            FiltreAteliers.filtrer(ateliers, "kotlin", "Équipe Éclair").map { it.nom }
        )
        assertTrue(FiltreAteliers.filtrer(ateliers, "pentest", "Équipe Éclair").isEmpty())
    }

    @Test
    fun noResultReturnsEmptyList() {
        assertTrue(FiltreAteliers.filtrer(ateliers, "introuvable", null).isEmpty())
    }

    @Test
    fun accentsAndCaseAreIgnored() {
        assertEquals(
            listOf("Programmation Kotlin"),
            FiltreAteliers.filtrer(ateliers, "equipe eclair", null).map { it.nom }
        )
        assertEquals(
            listOf("Big Data avancé"),
            FiltreAteliers.filtrer(ateliers, "avance", null).map { it.nom }
        )
    }

    @Test
    fun emptyAndWhitespaceQueriesMatchAll() {
        assertEquals(ateliers, FiltreAteliers.filtrer(ateliers, "", null))
        assertEquals(ateliers, FiltreAteliers.filtrer(ateliers, "   ", null))
    }

    @Test
    fun searchMatchesInstructorAndRoom() {
        assertEquals(
            listOf("Initiation au Pentesting"),
            FiltreAteliers.filtrer(ateliers, "ben salah", null).map { it.nom }
        )
        assertEquals(
            listOf("Programmation Kotlin"),
            FiltreAteliers.filtrer(ateliers, "m02", null).map { it.nom }
        )
    }

    @Test
    fun clubsAreDistinctAndSorted() {
        val duplicate = ateliers + Atelier(4, "cybertrace issatso", "Autre", "23/11/2026", 2)
        assertEquals(
            listOf("CyberTrace ISSATSo", "Data Club", "Équipe Éclair"),
            FiltreAteliers.clubsDistincts(duplicate)
        )
    }
}
