package ru.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import ru.habittracker.data.model.FrequencyType
import ru.habittracker.data.model.Habit
import ru.habittracker.ui.theme.Dimens
import ru.habittracker.ui.theme.HabitAmber
import ru.habittracker.ui.theme.HabitBlue
import ru.habittracker.ui.theme.HabitEmerald
import ru.habittracker.ui.theme.HabitRose
import ru.habittracker.ui.theme.HabitSlate
import ru.habittracker.ui.theme.HabitViolet
import ru.habittracker.viewmodel.HabitViewModel

private val presetColors = listOf(
    HabitEmerald, HabitBlue, HabitAmber, HabitViolet, HabitRose, HabitSlate
)

private val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditScreen(
    habitId: Long,
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    val isNew = habitId == 0L

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var color by remember { mutableLongStateOf(HabitEmerald.toArgb().toLong()) }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var daysMask by remember { mutableIntStateOf(0b1111111) }
    var loaded by remember { mutableStateOf(isNew) }

    LaunchedEffect(habitId) {
        if (!isNew) {
            val habit = viewModel.getHabit(habitId)
            if (habit != null) {
                title = habit.title
                description = habit.description.orEmpty()
                color = habit.color
                frequencyType = habit.frequencyType
                reminderEnabled = habit.reminderEnabled
                daysMask = habit.daysOfWeekMask
            }
            loaded = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text(if (isNew) "Новая привычка" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка…")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание (необязательно)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Цвет", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                presetColors.forEach { c ->
                    val selected = color == c.toArgb().toLong()
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(
                                if (selected) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.onBackground,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable { color = c.toArgb().toLong() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Частота", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = frequencyType == FrequencyType.DAILY,
                    onClick = { frequencyType = FrequencyType.DAILY },
                    label = { Text("Каждый день") }
                )
                FilterChip(
                    selected = frequencyType == FrequencyType.SPECIFIC_DAYS,
                    onClick = { frequencyType = FrequencyType.SPECIFIC_DAYS },
                    label = { Text("Дни недели") }
                )
            }

            if (frequencyType == FrequencyType.SPECIFIC_DAYS) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dayLabels.forEachIndexed { index, label ->
                        val bit = 1 shl index
                        val selected = (daysMask and bit) != 0
                        FilterChip(
                            selected = selected,
                            onClick = {
                                daysMask = if (selected) daysMask and bit.inv() else daysMask or bit
                            },
                            label = { Text(label) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Напоминание", style = MaterialTheme.typography.titleMedium)
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    val habit = Habit(
                        id = if (isNew) 0 else habitId,
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                        color = color,
                        frequencyType = frequencyType,
                        daysOfWeekMask = if (frequencyType == FrequencyType.DAILY) 0b1111111 else daysMask,
                        reminderEnabled = reminderEnabled
                    )
                    if (isNew) viewModel.addHabit(habit) else viewModel.updateHabit(habit)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Сохранить")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
