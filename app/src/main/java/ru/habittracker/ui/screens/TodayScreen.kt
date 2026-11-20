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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.habittracker.ui.theme.Dimens
import ru.habittracker.ui.theme.HtWarning
import ru.habittracker.util.DateUtils
import ru.habittracker.viewmodel.HabitViewModel
import ru.habittracker.viewmodel.HabitWithStatus
import java.time.LocalDate

@Composable
fun TodayScreen(
    viewModel: HabitViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Long) -> Unit
) {
    val state by viewModel.todayState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            if (state.habits.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onAddHabit,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.semantics { contentDescription = "Добавить привычку" }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
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
            // Компактный заголовок — без высоты Material TopAppBar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Dimens.screenHorizontal,
                        end = Dimens.screenHorizontal,
                        top = Dimens.space3,
                        bottom = Dimens.space2
                    )
            ) {
                Text("Сегодня", style = MaterialTheme.typography.titleLarge)
                Text(
                    DateUtils.formatDayMonth(LocalDate.now()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Загрузка…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                state.habits.isEmpty() -> {
                    EmptyHabits(onAdd = onAddHabit)
                }
                else -> {
                    DayProgress(
                        completed = state.completedCount,
                        total = state.totalCount,
                        modifier = Modifier.padding(
                            horizontal = Dimens.screenHorizontal,
                            vertical = Dimens.space2
                        )
                    )
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = Dimens.screenHorizontal,
                            vertical = Dimens.space2
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.listGap)
                    ) {
                        items(state.habits, key = { it.habit.id }) { item ->
                            HabitRow(
                                item = item,
                                onToggle = { viewModel.toggleHabit(item.habit.id) },
                                onClick = { onEditHabit(item.habit.id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayProgress(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val progress = if (total == 0) 0f else completed.toFloat() / total
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Прогресс",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$completed из $total",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(Dimens.space2))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.progressHeight)
                .clip(RoundedCornerShape(Dimens.radiusSm)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun HabitRow(
    item: HabitWithStatus,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    val habit = item.habit
    val accent = Color(habit.color.toInt())
    val done = item.isCompletedToday

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .background(
                if (done) accent.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Dimens.space3, vertical = Dimens.space3)
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append(habit.title)
                    if (done) append(", выполнено")
                    if (item.currentStreak > 0) append(", серия ${item.currentStreak}")
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.checkSize)
                .clip(CircleShape)
                .background(if (done) accent else accent.copy(alpha = 0.18f))
                .clickable(role = Role.Checkbox, onClick = onToggle)
                .semantics {
                    contentDescription = if (done) "Снять отметку" else "Отметить выполненным"
                },
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(Modifier.width(Dimens.space3))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!habit.description.isNullOrBlank()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        if (item.currentStreak > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = Dimens.space2)
            ) {
                Icon(
                    Icons.Outlined.LocalFireDepartment,
                    contentDescription = null,
                    tint = HtWarning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "${item.currentStreak}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun EmptyHabits(onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.space8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Пока нет привычек",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Dimens.space2))
        Text(
            text = "Добавьте первую — и отмечайте\nвыполнение каждый день",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Dimens.space6))
        FloatingActionButton(
            onClick = onAdd,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить привычку")
        }
    }
}
