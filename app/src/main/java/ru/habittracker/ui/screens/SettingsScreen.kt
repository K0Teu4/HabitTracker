package ru.habittracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import ru.habittracker.data.prefs.ThemeMode
import ru.habittracker.data.prefs.UserPrefs
import ru.habittracker.ui.theme.Dimens

@Composable
fun SettingsScreen(userPrefs: UserPrefs) {
    val themeMode by userPrefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()
    var showThemeDialog by remember { mutableStateOf(false) }

    val themeLabel = when (themeMode) {
        ThemeMode.SYSTEM -> "Системная"
        ThemeMode.LIGHT -> "Светлая"
        ThemeMode.DARK -> "Тёмная"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = Dimens.screenHorizontal)
    ) {
        Text(
            "Настройки",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = Dimens.space3, bottom = Dimens.space4)
        )

        SettingsGroup("Оформление")
        SettingsRow(
            title = "Тема",
            value = themeLabel,
            onClick = { showThemeDialog = true }
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        SettingsRow(title = "Язык", value = "Русский")

        Spacer(Modifier.height(Dimens.space6))
        SettingsGroup("Данные")
        SettingsRow(title = "Резервная копия", value = "Скоро")
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        SettingsRow(title = "Экспорт JSON", value = "Скоро")

        Spacer(Modifier.height(Dimens.space6))
        SettingsGroup("О приложении")
        SettingsRow(title = "Версия", value = "0.4.0")
        Text(
            "Quiet Precision · Habit Tracker",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Dimens.space2)
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Тема") },
            text = {
                Column {
                    ThemeOption("Системная", themeMode == ThemeMode.SYSTEM) {
                        scope.launch { userPrefs.setThemeMode(ThemeMode.SYSTEM) }
                        showThemeDialog = false
                    }
                    ThemeOption("Светлая", themeMode == ThemeMode.LIGHT) {
                        scope.launch { userPrefs.setThemeMode(ThemeMode.LIGHT) }
                        showThemeDialog = false
                    }
                    ThemeOption("Тёмная", themeMode == ThemeMode.DARK) {
                        scope.launch { userPrefs.setThemeMode(ThemeMode.DARK) }
                        showThemeDialog = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = if (selected) "✓  $label" else "     $label",
        style = MaterialTheme.typography.bodyLarge,
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimens.space3)
    )
}

@Composable
private fun SettingsGroup(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = Dimens.space2, bottom = Dimens.space2)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = Dimens.space3)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
