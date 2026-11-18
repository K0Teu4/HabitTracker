package ru.habittracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.habittracker.data.model.FrequencyType
import ru.habittracker.data.model.Habit
import ru.habittracker.data.model.HabitLog

@Database(
    entities = [Habit::class, HabitLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @androidx.room.TypeConverter
    fun toFrequencyType(value: String): FrequencyType = FrequencyType.valueOf(value)
}
