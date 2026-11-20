package ru.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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

// Однобуквенные — не ломаются в узкой строке
private val dayLetters = listOf("П", "В", "С", "Ч", "П", "С", "В")
private val dayFull = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditScreen(
    habitId: Long,
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    val isNew = habitId == 0L
    val scheme = MaterialTheme.colorScheme

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var color by remember { mutableLongStateOf(HabitEmerald.toArgb().toLong()) }
    var frequencyType by remember { mutableStateOf(FrequencyType.DAILY) }
    var daysMask by remember { mutableIntStateOf(0b1111111) }
    var loaded by remember { mutableStateOf(isNew) }
    var titleError by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        if (!isNew) {
            val habit = viewModel.getHabit(habitId)
            if (habit != null) {
                title = habit.title
                description = habit.description.orEmpty()
                color = habit.color
                frequencyType = habit.frequencyType
                daysMask = habit.daysOfWeekMask
            }
            loaded = true
        }
    }

    Scaffold(
        containerColor = scheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Text(
                        if (isNew) "Новая привычка" else "Редактирование",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.background)
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Загрузка…", color = scheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.screenHorizontal)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(Dimens.space2))

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (titleError && it.isNotBlank()) titleError = false
                },
                label = { Text("Название") },
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("Введите название") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.space3))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(Modifier.height(Dimens.space5))

            Text("Цвет", style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimens.space2))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetColors.forEach { c ->
                    val selected = color == c.toArgb().toLong()
                    Box(
                        modifier = Modifier
                            .size(if (selected) 40.dp else 36.dp)
                            .clip(CircleShape)
                            .background(c)
                            .then(
                                if (selected) Modifier.border(3.dp, scheme.onBackground, CircleShape)
                                else Modifier
                            )
                            .clickable { color = c.toArgb().toLong() }
                            .semantics { contentDescription = "Цвет" }
                    )
                }
            }

            Spacer(Modifier.height(Dimens.space5))

            Text("Как часто", style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimens.space2))
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
                Spacer(Modifier.height(Dimens.space3))
                // Круги дней — без FilterChip, чтобы не ломалась вёрстка
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLetters.forEachIndexed { index, letter ->
                        val bit = 1 shl index
                        val selected = (daysMask and bit) != 0
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) scheme.primary
                                        else scheme.surfaceVariant
                                    )
                                    .clickable {
                                        daysMask = if (selected) {
                                            daysMask and bit.inv()
                                        } else {
                                            daysMask or bit
                                        }
                                    }
                                    .semantics {
                                        contentDescription = dayFull[index] + if (selected) ", выбран" else ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) scheme.onPrimary else scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (daysMask == 0) {
                    Text(
                        "Выберите хотя бы один день",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.error,
                        modifier = Modifier.padding(top = Dimens.space2)
                    )
                }
            }

            Spacer(Modifier.height(Dimens.space5))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(Dimens.radiusMd))
                    .background(scheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = Dimens.space3, vertical = Dimens.space3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Напоминание", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Появится в следующих версиях",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(Dimens.space6))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                        return@Button
                    }
                    if (frequencyType == FrequencyType.SPECIFIC_DAYS && daysMask == 0) {
                        return@Button
                    }
                    val habit = Habit(
                        id = if (isNew) 0 else habitId,
                        title = title.trim(),
                        description = description.trim().ifBlank { null },
                        color = color,
                        frequencyType = frequencyType,
                        daysOfWeekMask = if (frequencyType == FrequencyType.DAILY) 0b1111111 else daysMask,
                        reminderEnabled = false
                    )
                    if (isNew) viewModel.addHabit(habit) else viewModel.updateHabit(habit)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() &&
                    !(frequencyType == FrequencyType.SPECIFIC_DAYS && daysMask == 0)
            ) {
                Text(if (isNew) "Создать" else "Сохранить")
            }

            Spacer(Modifier.height(Dimens.space8))
        }
    }
}
