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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.TopAppBarDefaults
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
import ru.habittracker.ui.theme.Dimens
import ru.habittracker.viewmodel.CalendarDayStatus
import ru.habittracker.viewmodel.HabitViewModel
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
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        containerColor = scheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Text(
                        yearMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru"))
                            .replaceFirstChar { it.titlecase(Locale("ru")) } + " " + yearMonth.year,
                        style = MaterialTheme.typography.titleLarge
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = scheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.screenHorizontal)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { d ->
                    Text(
                        text = d,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(Dimens.space2))

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Загрузка…", color = scheme.onSurfaceVariant)
                }
            } else {
                val firstDay = yearMonth.atDay(1)
                val startOffset = firstDay.dayOfWeek.value - 1
                val daysInMonth = yearMonth.lengthOfMonth()
                val totalCells = ((startOffset + daysInMonth + 6) / 7) * 7

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (rowStart in 0 until totalCells step 7) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (col in 0..6) {
                                val cellIndex = rowStart + col
                                val dayNumber = cellIndex - startOffset + 1
                                Box(modifier = Modifier.weight(1f)) {
                                    if (dayNumber in 1..daysInMonth) {
                                        val date = yearMonth.atDay(dayNumber)
                                        DayCell(
                                            day = dayNumber,
                                            status = statusMap[date],
                                            isToday = date == today,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    } else {
                                        Spacer(
                                            Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.space6))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(Color(0xFF2F8F6B).copy(alpha = 0.85f), "Всё")
                    LegendItem(Color(0xFFC4841D).copy(alpha = 0.7f), "Частично")
                    LegendItem(scheme.surfaceVariant, "Пусто")
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
    val scheme = MaterialTheme.colorScheme
    val completed = status?.completed ?: 0
    val total = status?.total ?: 0

    val bg = when {
        total <= 0 -> scheme.surfaceVariant.copy(alpha = 0.55f)
        completed >= total -> Color(0xFF2F8F6B).copy(alpha = 0.85f)
        completed > 0 -> Color(0xFFC4841D).copy(alpha = 0.7f)
        else -> scheme.surfaceVariant.copy(alpha = 0.75f)
    }

    val fg = when {
        total > 0 && completed >= total -> Color.White
        else -> scheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .then(
                if (isToday) Modifier.border(2.dp, scheme.primary, RoundedCornerShape(10.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = fg
            )
            if (total > 0) {
                Text(
                    text = "$completed/$total",
                    fontSize = 10.sp,
                    color = fg.copy(alpha = 0.9f)
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
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
