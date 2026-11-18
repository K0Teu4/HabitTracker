package ru.habittracker.data.repository

import kotlinx.coroutines.flow.Flow
import ru.habittracker.data.db.HabitDao
import ru.habittracker.data.model.Habit
import ru.habittracker.data.model.HabitLog
import ru.habittracker.util.StreakCalculator
import java.time.LocalDate

class HabitRepository(private val dao: HabitDao) {

    fun getActiveHabits(): Flow<List<Habit>> = dao.getActiveHabits()

    fun getArchivedHabits(): Flow<List<Habit>> = dao.getArchivedHabits()

    suspend fun getActiveHabitsSnapshot(): List<Habit> = dao.getActiveHabitsOnce()

    suspend fun getHabit(id: Long): Habit? = dao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = dao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) {
        dao.deleteLogsForHabit(habit.id)
        dao.deleteHabit(habit)
    }

    suspend fun archiveHabit(id: Long) = dao.archiveHabit(id)

    suspend fun unarchiveHabit(id: Long) = dao.unarchiveHabit(id)

    fun getLogsForDate(date: LocalDate): Flow<List<HabitLog>> =
        dao.getLogsForDate(date.toEpochDay())

    suspend fun getLogsForDateOnce(date: LocalDate): List<HabitLog> =
        dao.getLogsForDateOnce(date.toEpochDay())

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
        dao.getLogsForHabit(habitId)

    suspend fun toggleHabit(habitId: Long, date: LocalDate = LocalDate.now()) {
        val epochDay = date.toEpochDay()
        val existing = dao.getLog(habitId, epochDay)
        if (existing != null) {
            dao.deleteLog(habitId, epochDay)
        } else {
            dao.upsertLog(
                HabitLog(
                    habitId = habitId,
                    date = epochDay,
                    count = 1
                )
            )
        }
    }

    suspend fun setHabitCount(habitId: Long, date: LocalDate, count: Int) {
        val epochDay = date.toEpochDay()
        if (count <= 0) {
            dao.deleteLog(habitId, epochDay)
        } else {
            dao.upsertLog(
                HabitLog(
                    habitId = habitId,
                    date = epochDay,
                    count = count
                )
            )
        }
    }

    suspend fun getStreak(habit: Habit, today: LocalDate = LocalDate.now()): Int {
        val from = today.minusDays(800).toEpochDay()
        val to = today.toEpochDay()
        val logs = dao.getLogsBetween(habit.id, from, to)
        return StreakCalculator.calculateCurrentStreak(habit, logs, today)
    }

    suspend fun getBestStreak(habit: Habit, today: LocalDate = LocalDate.now()): Int {
        val from = today.minusDays(800).toEpochDay()
        val to = today.toEpochDay()
        val logs = dao.getLogsBetween(habit.id, from, to)
        return StreakCalculator.calculateBestStreak(habit, logs, today)
    }
}
