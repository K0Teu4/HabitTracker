package ru.habittracker.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val dayMonthFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
    private val fullFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru"))

    fun formatDayMonth(date: LocalDate): String = date.format(dayMonthFormatter)
    fun formatFull(date: LocalDate): String = date.format(fullFormatter)

    fun todayEpochDay(): Long = LocalDate.now().toEpochDay()
}
