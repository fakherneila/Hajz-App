package tn.issatso.clubs

import java.text.Normalizer

object FiltreAteliers {
    fun filtrer(
        ateliers: List<Atelier>,
        searchText: String,
        selectedClub: String?
    ): List<Atelier> {
        val query = normalize(searchText)
        return ateliers.filter { atelier ->
            val matchesSearch = query.isEmpty() ||
                normalize(atelier.nom).contains(query) ||
                normalize(atelier.club).contains(query) ||
                normalize(atelier.instructeur).contains(query) ||
                normalize(atelier.salle).contains(query)
            val matchesClub = selectedClub == null ||
                normalize(atelier.club).equals(normalize(selectedClub), ignoreCase = true)
            matchesSearch && matchesClub
        }
    }

    fun clubsDistincts(ateliers: List<Atelier>): List<String> =
        ateliers
            .map { it.club.trim().replace(Regex("\\s+"), " ") }
            .filter { it.isNotEmpty() }
            .distinctBy { normalize(it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it })

    fun normalize(text: String): String =
        Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
}
