package ru.habittracker

import android.app.Application
import ru.habittracker.data.db.AppDatabase
import ru.habittracker.data.repository.HabitRepository

class HabitApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { HabitRepository(database.habitDao()) }
}
