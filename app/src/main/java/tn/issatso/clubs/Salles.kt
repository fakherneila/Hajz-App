package tn.issatso.clubs

/**
 * Official selectable room list for Hajz-App sessions.
 */
object Salles {
    val salles: List<String> = buildList {
        addAll((1..20).map { "I%02d".format(it) })
        addAll((1..20).map { "M%02d".format(it) })
        add("Bibliothèque")
        add("Amphi 01")
        add("Amphi 02")
        add("L01")
        add("L02")
    }

    /**
     * Checks an exact, trimmed match. Room codes are case-sensitive.
     */
    fun estSalleValide(s: String): Boolean = salles.contains(s.trim())
}
