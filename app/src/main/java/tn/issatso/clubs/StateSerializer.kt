package tn.issatso.clubs

/**
 * Delimiter-safe pure-Kotlin serialization for the complete in-memory application state.
 */
object StateSerializer {
    private const val DELIMITER = '|'

    data class UiState(
        val activeView: String,
        val filter: Int,
        val searchText: String,
        val selectedClub: String?,
        val adminMode: Boolean,
        val selectedWorkshopId: Int?
    )

    data class Snapshot(
        val workshops: List<Atelier>,
        val ticketCounters: Map<Int, Int>,
        val nextAtelierId: Int,
        val ui: UiState
    )

    fun serialize(snapshot: Snapshot): String {
        val lines = mutableListOf<String>()
        lines += "STATE|1"
        lines += "UI|${escape(snapshot.ui.activeView)}|${snapshot.ui.filter}|${escape(snapshot.ui.searchText)}|" +
            "${escape(snapshot.ui.selectedClub ?: "")}|${snapshot.ui.adminMode}|" +
            "${snapshot.ui.selectedWorkshopId ?: ""}"
        lines += "NEXT|${snapshot.nextAtelierId}"
        snapshot.workshops.forEach { workshop ->
            val counter = snapshot.ticketCounters[workshop.id] ?: 1
            lines += "W|${workshop.id}|${escape(workshop.club)}|${escape(workshop.nom)}|" +
                "${escape(workshop.date)}|${workshop.capacite}|$counter|" +
                "${escape(workshop.instructeur)}|${escape(workshop.salle)}"
            workshop.reservations.forEach { reservation ->
                lines += "R|${workshop.id}|${escape(reservation.nom)}|${escape(reservation.prenom)}|" +
                    "${reservation.ticketAttente ?: ""}"
            }
        }
        return lines.joinToString("\n")
    }

    fun deserialize(value: String): Snapshot? {
        if (value.isBlank()) return null
        val lines = value.split('\n')
        if (lines.firstOrNull() != "STATE|1") return null

        val workshops = mutableMapOf<Int, Atelier>()
        val reservations = mutableListOf<Triple<Int, ReservationDraft, Int?>>()
        val ticketCounters = mutableMapOf<Int, Int>()
        var nextAtelierId = 12
        var ui = UiState("A", 0, "", null, false, null)

        lines.drop(1).forEach { line ->
            val parts = line.split(DELIMITER)
            when (parts.firstOrNull()) {
                "NEXT" -> nextAtelierId = parts.getOrNull(1)?.toIntOrNull() ?: nextAtelierId
                "UI" -> {
                    ui = UiState(
                        unescape(parts.getOrNull(1).orEmpty()),
                        parts.getOrNull(2)?.toIntOrNull() ?: 0,
                        unescape(parts.getOrNull(3).orEmpty()),
                        parts.getOrNull(4)?.let { unescape(it).ifEmpty { null } },
                        parts.getOrNull(5)?.toBooleanStrictOrNull() ?: false,
                        parts.getOrNull(6)?.toIntOrNull()
                    )
                }
                "W" -> {
                    val id = parts.getOrNull(1)?.toIntOrNull()
                    if (id != null) {
                        val workshop = Atelier(
                            id,
                            unescape(parts.getOrNull(2).orEmpty()),
                            unescape(parts.getOrNull(3).orEmpty()),
                            unescape(parts.getOrNull(4).orEmpty()),
                            parts.getOrNull(5)?.toIntOrNull() ?: 2,
                            unescape(parts.getOrNull(7).orEmpty().ifEmpty { "M. Trabelsi" }),
                            unescape(parts.getOrNull(8).orEmpty().ifEmpty { "I01" })
                        )
                        workshops[id] = workshop
                        ticketCounters[id] = parts.getOrNull(6)?.toIntOrNull() ?: 1
                    }
                }
                "R" -> {
                    val workshopId = parts.getOrNull(1)?.toIntOrNull()
                    if (workshopId != null) {
                        reservations += Triple(
                            workshopId,
                            ReservationDraft(
                                unescape(parts.getOrNull(2).orEmpty()),
                                unescape(parts.getOrNull(3).orEmpty())
                            ),
                            parts.getOrNull(4)?.toIntOrNull()
                        )
                    }
                }
            }
        }

        reservations.forEach { (workshopId, draft, ticket) ->
            val workshop = workshops[workshopId] ?: return@forEach
            workshop.reservations += Reservation(draft.nom, draft.prenom, workshop, ticket)
        }
        return Snapshot(workshops.values.toList(), ticketCounters, nextAtelierId, ui)
    }

    private data class ReservationDraft(val nom: String, val prenom: String)

    private fun escape(value: String): String =
        value.replace("\\", "\\\\")
            .replace("|", "\\p")
            .replace("\n", "\\n")
            .replace("\r", "\\r")

    private fun unescape(value: String): String {
        val output = StringBuilder()
        var index = 0
        while (index < value.length) {
            if (value[index] == '\\' && index + 1 < value.length) {
                when (value[index + 1]) {
                    '\\' -> output.append('\\')
                    'p' -> output.append('|')
                    'n' -> output.append('\n')
                    'r' -> output.append('\r')
                    else -> output.append(value[index])
                }
                index += 2
            } else {
                output.append(value[index])
                index++
            }
        }
        return output.toString()
    }
}
