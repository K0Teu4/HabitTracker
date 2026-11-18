package ru.habittracker.util

import ru.habittracker.data.model.FrequencyType
import ru.habittracker.data.model.Habit
import ru.habittracker.data.model.HabitLog
import java.time.DayOfWeek
import java.time.LocalDate

object StreakCalculator {

    fun isScheduledDay(habit: Habit, date: LocalDate): Boolean {
        return when (habit.frequencyType) {
            FrequencyType.DAILY -> true
            FrequencyType.SPECIFIC_DAYS -> {
                val bit = 1 shl (date.dayOfWeek.value - 1) // Mon=1 → bit 0
                (habit.daysOfWeekMask and bit) != 0
            }
            FrequencyType.TIMES_PER_WEEK -> true // мягкий режим: любой день засчитывается
        }
    }

    fun isCompleted(habit: Habit, log: HabitLog?): Boolean {
        return log != null && log.count >= habit.targetPerDay
    }

    /**
     * Текущая серия.
     * Правило: идём назад от сегодня.
     * - Если день не запланирован — пропускаем.
     * - Если запланирован и выполнен — +1 к серии.
     * - Если запланирован и НЕ выполнен:
     *     - если это сегодня — не рвём серию (день ещё идёт)
     *     - иначе — серия закончена.
     */
    fun calculateCurrentStreak(
        habit: Habit,
        logs: List<HabitLog>,
        today: LocalDate = LocalDate.now()
    ): Int {
        val logMap = logs.associateBy { it.date }
        var streak = 0
        var current = today

        // Защита от бесконечного цикла
        repeat(1000) {
            if (!isScheduledDay(habit, current)) {
                current = current.minusDays(1)
                return@repeat
            }

            val log = logMap[current.toEpochDay()]
            val done = isCompleted(habit, log)

            if (done) {
                streak++
                current = current.minusDays(1)
            } else {
                if (current == today) {
                    // Сегодня ещё можно выполнить — не рвём
                    current = current.minusDays(1)
                } else {
                    return streak
                }
            }
        }
        return streak
    }

    /**
     * Лучшая серия за весь доступный период логов.
     */
    fun calculateBestStreak(
        habit: Habit,
        logs: List<HabitLog>,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (logs.isEmpty()) return 0

        val logMap = logs.associateBy { it.date }
        val minDay = logs.minOf { it.date }
        val start = LocalDate.ofEpochDay(minDay)
        var best = 0
        var currentStreak = 0
        var day = start

        while (!day.isAfter(today)) {
            if (isScheduledDay(habit, day)) {
                val log = logMap[day.toEpochDay()]
                if (isCompleted(habit, log)) {
                    currentStreak++
                    if (currentStreak > best) best = currentStreak
                } else {
                    currentStreak = 0
                }
            }
            day = day.plusDays(1)
        }
        return best
    }
}
