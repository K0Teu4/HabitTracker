package ru.habittracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.habittracker.data.model.Habit
import ru.habittracker.data.model.HabitLog

@Dao
interface HabitDao {

    // --- Habits ---

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY position ASC, createdAt ASC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY position ASC, createdAt ASC")
    suspend fun getActiveHabitsOnce(): List<Habit>

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY archivedAt DESC")
    fun getArchivedHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("UPDATE habits SET isArchived = 1, archivedAt = :timestamp WHERE id = :id")
    suspend fun archiveHabit(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE habits SET isArchived = 0, archivedAt = NULL WHERE id = :id")
    suspend fun unarchiveHabit(id: Long)

    // --- Logs ---

    @Query("SELECT * FROM habit_logs WHERE date = :epochDay")
    fun getLogsForDate(epochDay: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE date = :epochDay")
    suspend fun getLogsForDateOnce(epochDay: Long): List<HabitLog>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY date DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date = :epochDay LIMIT 1")
    suspend fun getLog(habitId: Long, epochDay: Long): HabitLog?

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND date BETWEEN :fromDay AND :toDay")
    suspend fun getLogsBetween(habitId: Long, fromDay: Long, toDay: Long): List<HabitLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: HabitLog): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :epochDay")
    suspend fun deleteLog(habitId: Long, epochDay: Long)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}
