package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.UserProfileEntity
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class XpGainEvent(
    val habitTitle: String,
    val xpAmount: Int,
    val isStreakBonus: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HabitRepository.getInstance(application)

    val activeHabits: StateFlow<List<HabitEntity>> = repository.activeHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCompletions: StateFlow<List<HabitCompletionEntity>> = repository.getTodayCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCompletions: StateFlow<List<HabitCompletionEntity>> = repository.allCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _xpEvent = MutableStateFlow<XpGainEvent?>(null)
    val xpEvent: StateFlow<XpGainEvent?> = _xpEvent.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<Int?>(null)
    val levelUpEvent: StateFlow<Int?> = _levelUpEvent.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
        }
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val previousProfile = userProfile.value
            val previousLevel = previousProfile?.level ?: 1
            val habit = activeHabits.value.find { it.id == habitId }

            val result = repository.toggleCompletion(habitId)

            if (result.isCompleted) {
                val newLevel = userProfile.value?.level ?: previousLevel
                if (newLevel > previousLevel) {
                    _levelUpEvent.value = newLevel
                }

                _xpEvent.value = XpGainEvent(
                    habitTitle = habit?.title ?: "Habit",
                    xpAmount = result.xpChange,
                    isStreakBonus = (previousProfile?.currentStreak ?: 0) > 0
                )
            }
        }
    }

    fun addHabit(
        title: String,
        description: String,
        category: String,
        iconKey: String,
        colorHex: Long,
        xpReward: Int
    ) {
        viewModelScope.launch {
            val newHabit = HabitEntity(
                title = title.trim(),
                description = description.trim(),
                category = category,
                iconKey = iconKey,
                colorHex = colorHex,
                xpReward = xpReward,
                orderIndex = activeHabits.value.size
            )
            repository.addHabit(newHabit)
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    fun updateProfile(
        username: String,
        customTitle: String,
        avatarKey: String,
        customAvatarUri: String?,
        theme: String
    ) {
        viewModelScope.launch {
            repository.updateProfile(
                username = username.trim(),
                customTitle = customTitle.trim(),
                avatarKey = avatarKey,
                customAvatarUri = customAvatarUri,
                theme = theme
            )
        }
    }

    fun setTheme(themeId: String) {
        viewModelScope.launch {
            repository.updateTheme(themeId)
        }
    }

    fun dismissXpEvent() {
        _xpEvent.value = null
    }

    fun dismissLevelUp() {
        _levelUpEvent.value = null
    }
}
