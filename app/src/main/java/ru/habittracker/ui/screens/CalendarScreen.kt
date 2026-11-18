package ru.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.habittracker.viewmodel.CalendarDayStatus
import ru.habittracker.viewmodel.HabitViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: HabitViewModel) {
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    var days by remember { mutableStateOf<List<CalendarDayStatus>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(yearMonth) {
        loading = true
        days = viewModel.loadMonthStatus(yearMonth)
        loading = false
    }

    val statusMap = remember(days) { days.associateBy { it.date } }
    val today = LocalDate.now()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                            .replaceFirstChar { it.titlecase(Locale("ru")) } + " " + yearMonth.year
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Предыдущий")
                    }
                },
                actions = {
                    IconButton(onClick = { yearMonth = yearMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Следующий")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            // Дни недели
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Загрузка…")
                }
            } else {
                val firstDay = yearMonth.atDay(1)
                // Monday = 1 ... Sunday = 7 → offset 0..6
                val startOffset = firstDay.dayOfWeek.value - 1
                val daysInMonth = yearMonth.lengthOfMonth()
                val totalCells = startOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0..6) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - startOffset + 1
                                if (dayNumber in 1..daysInMonth) {
                                    val date = yearMonth.atDay(dayNumber)
                                    val status = statusMap[date]
                                    DayCell(
                                        day = dayNumber,
                                        status = status,
                                        isToday = date == today,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Легенда
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(Color(0xFF4CAF50).copy(alpha = 0.7f), "Всё")
                    LegendItem(Color(0xFFFF9800).copy(alpha = 0.5f), "Частично")
                    LegendItem(MaterialTheme.colorScheme.surfaceVariant, "Пусто / нет")
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    status: CalendarDayStatus?,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = when {
        status == null || status.total == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        status.completed >= status.total -> Color(0xFF4CAF50).copy(alpha = 0.75f)
        status.completed > 0 -> Color(0xFFFF9800).copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .then(
                if (isToday) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(10.dp)
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
            if (status != null && status.total > 0) {
                Text(
                    text = "${status.completed}/${status.total}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
