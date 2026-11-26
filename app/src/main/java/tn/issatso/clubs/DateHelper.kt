package tn.issatso.clubs

enum class DateRelation {
    PAST,
    TODAY,
    FUTURE,
    INVALID
}

object DateHelper {
    const val DATE_PATTERN = "dd/MM/yyyy"

    /**
     * Parses a dd/MM/yyyy date without throwing for malformed input.
     */
    fun parseDate(value: String): Triple<Int, Int, Int>? {
        val parts = value.trim().split("/")
        if (parts.size != 3) return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        if (year < 1 || month !in 1..12) return null
        val daysInMonth = when (month) {
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        if (day !in 1..daysInMonth) return null
        return Triple(day, month, year)
    }

    /**
     * Compares a workshop date with an explicitly supplied today date.
     */
    fun compareWithToday(date: String, today: String): DateRelation {
        val value = parseDate(date) ?: return DateRelation.INVALID
        val current = parseDate(today) ?: return DateRelation.INVALID
        val valueKey = value.third * 10000 + value.second * 100 + value.first
        val currentKey = current.third * 10000 + current.second * 100 + current.first
        return when {
            valueKey < currentKey -> DateRelation.PAST
            valueKey > currentKey -> DateRelation.FUTURE
            else -> DateRelation.TODAY
        }
    }

    /**
     * Sorts workshops ascending, with invalid dates placed last.
     */
    fun sortAteliers(ateliers: MutableList<Atelier>) {
        ateliers.sortWith(compareBy<Atelier> {
            val date = parseDate(it.date)
            if (date == null) Long.MAX_VALUE else date.third.toLong() * 10000 + date.second * 100 + date.first
        }.thenBy { it.id })
    }
}
