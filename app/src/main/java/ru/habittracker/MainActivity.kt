package ru.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.habittracker.data.prefs.ThemeMode
import ru.habittracker.ui.HabitNavHost
import ru.habittracker.ui.screens.OnboardingScreen
import ru.habittracker.ui.theme.HabitTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as HabitApp

        setContent {
            val themeMode by app.userPrefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val onboardingDone by app.userPrefs.onboardingDone.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            HabitTrackerTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!onboardingDone) {
                        OnboardingScreen(
                            onFinished = {
                                lifecycleScope.launch {
                                    app.userPrefs.setOnboardingDone(true)
                                }
                            }
                        )
                    } else {
                        HabitNavHost()
                    }
                }
            }
        }
    }
}
