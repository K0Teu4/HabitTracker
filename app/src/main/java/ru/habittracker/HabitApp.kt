package ru.habittracker

import android.app.Application
import ru.habittracker.data.db.AppDatabase
import ru.habittracker.data.prefs.UserPrefs
import ru.habittracker.data.repository.HabitRepository

class HabitApp : Application() {
    lateinit var repository: HabitRepository
        private set
    lateinit var userPrefs: UserPrefs
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = HabitRepository(db.habitDao())
        userPrefs = UserPrefs(this)
    }
}
