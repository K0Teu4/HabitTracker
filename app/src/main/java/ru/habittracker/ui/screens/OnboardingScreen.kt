package ru.habittracker.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.habittracker.ui.theme.Dimens

private data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: ImageVector
)

private val pages = listOf(
    OnboardingPage(
        "Привычки без шума",
        "Отмечайте день, следите за серией и календарём. Всё хранится на устройстве.",
        Icons.Outlined.CheckCircle
    ),
    OnboardingPage(
        "Один экран на день",
        "«Сегодня» — главный список. Нажали — выполнили. Прогресс и серия рядом.",
        Icons.Outlined.LocalFireDepartment
    ),
    OnboardingPage(
        "Можно сразу начать",
        "Добавьте первую привычку. Напоминания и резервные копии — позже, без обязательных разрешений сейчас.",
        Icons.Outlined.CalendarMonth
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val last = pagerState.currentPage == pages.lastIndex
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.space6)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.space2),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinished) {
                Text("Пропустить", color = scheme.primary)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val item = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.space2),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(RoundedCornerShape(Dimens.radiusLg))
                        .background(scheme.primaryContainer)
                        .border(
                            width = 1.dp,
                            color = scheme.outline.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(Dimens.radiusLg)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = scheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(Dimens.space6))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = scheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(Dimens.space3))
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.space4),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { i ->
                val selected = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 10.dp else 8.dp)
                        .background(
                            if (selected) scheme.primary
                            else scheme.outline.copy(alpha = 0.45f),
                            CircleShape
                        )
                )
            }
        }

        Button(
            onClick = {
                if (last) onFinished()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Dimens.space5)
        ) {
            Text(if (last) "Начать" else "Далее")
        }
    }
}
