package tn.issatso.clubs

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

    fun estSalleValide(salle: String): Boolean = salles.contains(salle.trim())
}
