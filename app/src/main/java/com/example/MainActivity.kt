package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.HabitForgeTheme
import com.example.viewmodel.HabitViewModel

enum class AppScreen {
    HOME,
    PROFILE,
    STATS
}

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val themeKey = userProfile?.selectedTheme ?: "oled"

            HabitForgeTheme(selectedThemeId = themeKey) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HabitForgeApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun HabitForgeApp(viewModel: HabitViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    val habits by viewModel.activeHabits.collectAsStateWithLifecycle()
    val todayCompletions by viewModel.todayCompletions.collectAsStateWithLifecycle()
    val allCompletions by viewModel.allCompletions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val xpEvent by viewModel.xpEvent.collectAsStateWithLifecycle()
    val levelUpLevel by viewModel.levelUpEvent.collectAsStateWithLifecycle()

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                habits = habits,
                todayCompletions = todayCompletions,
                userProfile = userProfile,
                selectedCategory = selectedCategory,
                xpEvent = xpEvent,
                levelUpLevel = levelUpLevel,
                onSelectCategory = { viewModel.setSelectedCategory(it) },
                onToggleHabit = { viewModel.toggleHabit(it) },
                onAddHabit = { title, desc, cat, icon, color, xp ->
                    viewModel.addHabit(title, desc, cat, icon, color, xp)
                },
                onUpdateHabit = { viewModel.updateHabit(it) },
                onDeleteHabit = { viewModel.deleteHabit(it) },
                onOpenProfile = { currentScreen = AppScreen.PROFILE },
                onOpenStats = { currentScreen = AppScreen.STATS },
                onDismissXpEvent = { viewModel.dismissXpEvent() },
                onDismissLevelUp = { viewModel.dismissLevelUp() }
            )
        }

        AppScreen.PROFILE -> {
            BackHandler {
                currentScreen = AppScreen.HOME
            }
            ProfileScreen(
                profile = userProfile,
                onSaveProfile = { username, customTitle, avatarKey, customAvatarUri, theme ->
                    viewModel.updateProfile(username, customTitle, avatarKey, customAvatarUri, theme)
                },
                onBack = { currentScreen = AppScreen.HOME }
            )
        }

        AppScreen.STATS -> {
            BackHandler {
                currentScreen = AppScreen.HOME
            }
            StatsScreen(
                profile = userProfile,
                habits = habits,
                allCompletions = allCompletions,
                onBack = { currentScreen = AppScreen.HOME }
            )
        }
    }
}
