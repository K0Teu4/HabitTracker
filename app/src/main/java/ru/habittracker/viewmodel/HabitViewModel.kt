package ru.habittracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.habittracker.data.model.Habit
import ru.habittracker.data.repository.HabitRepository
import ru.habittracker.util.StreakCalculator
import java.time.LocalDate
import java.time.YearMonth

data class HabitWithStatus(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

data class TodayUiState(
    val habits: List<HabitWithStatus> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

data class CalendarDayStatus(
    val date: LocalDate,
    val completed: Int,
    val total: Int
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0L)

    val todayState: StateFlow<TodayUiState> = combine(
        repository.getActiveHabits(),
        repository.getLogsForDate(LocalDate.now()),
        refreshTrigger
    ) { habits, logs, _ ->
        Pair(habits, logs)
    }.mapLatest { (habits, logs) ->
        val logMap = logs.associateBy { it.habitId }
        val items = habits.map { habit ->
            val log = logMap[habit.id]
            val completed = log != null && log.count >= habit.targetPerDay
            val streak = repository.getStreak(habit)
            val best = repository.getBestStreak(habit)
            HabitWithStatus(
                habit = habit,
                isCompletedToday = completed,
                currentStreak = streak,
                bestStreak = best
            )
        }
        TodayUiState(
            habits = items,
            completedCount = items.count { it.isCompletedToday },
            totalCount = items.size,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodayUiState()
    )

    val activeHabits = repository.getActiveHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleHabit(habitId: Long, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            repository.toggleHabit(habitId, date)
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun archiveHabit(id: Long) {
        viewModelScope.launch {
            repository.archiveHabit(id)
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            refreshTrigger.value = System.currentTimeMillis()
        }
    }

    suspend fun getHabit(id: Long): Habit? = repository.getHabit(id)

    suspend fun loadMonthStatus(yearMonth: YearMonth): List<CalendarDayStatus> {
        val habits = repository.getActiveHabitsSnapshot()
        if (habits.isEmpty()) return emptyList()

        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        val result = mutableListOf<CalendarDayStatus>()
        var day = start
        while (!day.isAfter(end)) {
            val logs = repository.getLogsForDateOnce(day)
            val logMap = logs.associateBy { it.habitId }
            var completed = 0
            var total = 0
            for (habit in habits) {
                if (StreakCalculator.isScheduledDay(habit, day)) {
                    total++
                    val log = logMap[habit.id]
                    if (log != null && log.count >= habit.targetPerDay) {
                        completed++
                    }
                }
            }
            result.add(CalendarDayStatus(day, completed, total))
            day = day.plusDays(1)
        }
        return result
    }
}

class HabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
