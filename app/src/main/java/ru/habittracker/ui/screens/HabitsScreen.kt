package ru.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.habittracker.data.model.FrequencyType
import ru.habittracker.data.model.Habit
import ru.habittracker.ui.theme.Dimens
import ru.habittracker.viewmodel.HabitViewModel

@Composable
fun HabitsScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit
) {
    val habits by viewModel.activeHabits.collectAsState()
    val archived by viewModel.archivedHabits.collectAsState()
    var showArchived by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (!showArchived) {
                FloatingActionButton(
                    onClick = onAddHabit,
                    containerColor = scheme.primary,
                    contentColor = scheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.space3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (showArchived) "Архив" else "Привычки",
                    style = MaterialTheme.typography.titleLarge
                )
                if (archived.isNotEmpty() || showArchived) {
                    TextButton(onClick = { showArchived = !showArchived }) {
                        Text(if (showArchived) "Активные" else "Архив (${archived.size})")
                    }
                }
            }

            val list = if (showArchived) archived else habits

            if (list.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(Dimens.space8),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showArchived) "Архив пуст" else "Нет активных привычек",
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = Dimens.screenHorizontal,
                        vertical = Dimens.space2
                    ),
                    verticalArrangement = Arrangement.spacedBy(Dimens.listGap)
                ) {
                    items(list, key = { it.id }) { habit ->
                        HabitListItem(
                            habit = habit,
                            isArchived = showArchived,
                            onEdit = { onEditHabit(habit.id) },
                            onArchive = { viewModel.archiveHabit(habit.id) },
                            onUnarchive = { viewModel.unarchiveHabit(habit.id) },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HabitListItem(
    habit: Habit,
    isArchived: Boolean,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val color = Color(habit.color.toInt())
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .background(scheme.surface)
            .clickable(onClick = onEdit)
            .padding(end = Dimens.space1, top = Dimens.space3, bottom = Dimens.space3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = Dimens.space3)
                .width(4.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )

        Spacer(Modifier.width(Dimens.space3))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                habit.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                frequencyLabel(habit),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )
        }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Меню")
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                if (!isArchived) {
                    DropdownMenuItem(text = { Text("Редактировать") }, onClick = {
                        menuExpanded = false
                        onEdit()
                    })
                    DropdownMenuItem(text = { Text("В архив") }, onClick = {
                        menuExpanded = false
                        onArchive()
                    })
                } else {
                    DropdownMenuItem(text = { Text("Вернуть") }, onClick = {
                        menuExpanded = false
                        onUnarchive()
                    })
                }
                DropdownMenuItem(text = { Text("Удалить") }, onClick = {
                    menuExpanded = false
                    onDelete()
                })
            }
        }
    }
}

private fun frequencyLabel(habit: Habit): String = when (habit.frequencyType) {
    FrequencyType.DAILY -> "Каждый день"
    FrequencyType.SPECIFIC_DAYS -> {
        val labels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        val selected = labels.filterIndexed { i, _ -> (habit.daysOfWeekMask and (1 shl i)) != 0 }
        if (selected.size == 7) "Каждый день"
        else selected.joinToString(" · ")
    }
    FrequencyType.TIMES_PER_WEEK -> "${habit.timesPerWeek}× в неделю"
}
