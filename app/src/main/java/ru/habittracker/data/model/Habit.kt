package ru.habittracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String? = null,
    val color: Long = 0xFF2F8F6B,          // ARGB
    val iconKey: String = "check",         // key for icon mapping
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val daysOfWeekMask: Int = 0b1111111,   // Mon=1 ... Sun=64, default all days
    val timesPerWeek: Int = 7,
    val targetPerDay: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val isArchived: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val archivedAt: Long? = null
)
