package com.example.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.data.dao.HabitDao
import com.example.data.database.AppDatabase
import com.example.data.entity.HabitCompletionEntity
import com.example.data.entity.HabitEntity
import com.example.data.entity.UserProfileEntity
import com.example.widget.HabitWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(
    private val habitDao: HabitDao,
    private val context: Context
) {

    val activeHabits: Flow<List<HabitEntity>> = habitDao.getActiveHabits()
    val userProfile: Flow<UserProfileEntity?> = habitDao.getUserProfile()
    val allCompletions: Flow<List<HabitCompletionEntity>> = habitDao.getAllCompletions()

    fun getTodayCompletions(date: String = getTodayDateString()): Flow<List<HabitCompletionEntity>> {
        return habitDao.getCompletionsForDate(date)
    }

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        val existingHabits = habitDao.getActiveHabitsSync()
        if (existingHabits.isEmpty()) {
            val defaultHabits = listOf(
                HabitEntity(
                    title = "Morning Workout",
                    description = "20 min strength, cardio or stretching",
                    category = "Fitness",
                    iconKey = "fitness_center",
                    colorHex = 0xFF00E676,
                    xpReward = 40,
                    orderIndex = 0
                ),
                HabitEntity(
                    title = "Drink 2L Water",
                    description = "Stay hydrated throughout the day",
                    category = "Health",
                    iconKey = "water_drop",
                    colorHex = 0xFF00E5FF,
                    xpReward = 25,
                    orderIndex = 1
                ),
                HabitEntity(
                    title = "Read 15 Pages",
                    description = "Focus reading before sleep or during break",
                    category = "Mind",
                    iconKey = "menu_book",
                    colorHex = 0xFFA78BFA,
                    xpReward = 35,
                    orderIndex = 2
                ),
                HabitEntity(
                    title = "Daily Focus Session",
                    description = "Deep work on primary priority task",
                    category = "Productivity",
                    iconKey = "bolt",
                    colorHex = 0xFFFFAB00,
                    xpReward = 35,
                    orderIndex = 3
                )
            )
            for (habit in defaultHabits) {
                habitDao.insertHabit(habit)
            }
        }

        val profile = habitDao.getUserProfileSync()
        if (profile == null) {
            habitDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    username = "Shadow Hunter",
                    customTitle = "Habit Initiate",
                    avatarKey = "shield",
                    totalXp = 60,
                    currentStreak = 2,
                    longestStreak = 5,
                    selectedTheme = "oled"
                )
            )
        }
    }

    suspend fun toggleCompletion(habitId: Long, date: String = getTodayDateString()): ToggleResult =
        withContext(Dispatchers.IO) {
            val existing = habitDao.getCompletion(habitId, date)
            val habit = habitDao.getHabitById(habitId)
            val profile = habitDao.getUserProfileSync() ?: UserProfileEntity()

            if (existing != null) {
                // Delete completion
                habitDao.deleteCompletion(habitId, date)
                val newXp = (profile.totalXp - existing.xpEarned).coerceAtLeast(0)
                val updatedProfile = profile.copy(totalXp = newXp)
                habitDao.insertOrUpdateProfile(updatedProfile)
                refreshWidget()
                ToggleResult(isCompleted = false, xpChange = -existing.xpEarned, newTotalXp = newXp)
            } else {
                // Insert completion
                val baseXp = habit?.xpReward ?: 30
                val streakBonus = (profile.currentStreak * 5).coerceAtMost(50)
                val totalAwarded = baseXp + streakBonus

                val completion = HabitCompletionEntity(
                    habitId = habitId,
                    completedDate = date,
                    completedTimestamp = System.currentTimeMillis(),
                    xpEarned = totalAwarded
                )
                habitDao.insertCompletion(completion)

                val newTotalXp = profile.totalXp + totalAwarded
                val newStreak = calculateUpdatedStreak(date)
                val longest = maxOf(profile.longestStreak, newStreak)

                val updatedProfile = profile.copy(
                    totalXp = newTotalXp,
                    currentStreak = newStreak,
                    longestStreak = longest
                )
                habitDao.insertOrUpdateProfile(updatedProfile)
                refreshWidget()
                ToggleResult(isCompleted = true, xpChange = totalAwarded, newTotalXp = newTotalXp)
            }
        }

    fun toggleCompletionSync(habitId: Long, date: String = getTodayDateString()): Boolean {
        val existing = habitDao.getCompletionSync(habitId, date)
        val habit = habitDao.getHabitByIdSync(habitId)
        val profile = habitDao.getUserProfileSync() ?: UserProfileEntity()

        return if (existing != null) {
            habitDao.deleteCompletionSync(habitId, date)
            val newXp = (profile.totalXp - existing.xpEarned).coerceAtLeast(0)
            habitDao.insertOrUpdateProfileSync(profile.copy(totalXp = newXp))
            refreshWidget()
            false
        } else {
            val baseXp = habit?.xpReward ?: 30
            val streakBonus = (profile.currentStreak * 5).coerceAtMost(50)
            val totalAwarded = baseXp + streakBonus

            habitDao.insertCompletionSync(
                HabitCompletionEntity(
                    habitId = habitId,
                    completedDate = date,
                    completedTimestamp = System.currentTimeMillis(),
                    xpEarned = totalAwarded
                )
            )
            val newStreak = calculateUpdatedStreak(date)
            habitDao.insertOrUpdateProfileSync(
                profile.copy(
                    totalXp = profile.totalXp + totalAwarded,
                    currentStreak = newStreak,
                    longestStreak = maxOf(profile.longestStreak, newStreak)
                )
            )
            refreshWidget()
            true
        }
    }

    private fun calculateUpdatedStreak(todayStr: String): Int {
        val allCompletions = habitDao.getAllCompletionsSync()
        val distinctDates = allCompletions.map { it.completedDate }.toSet()
        if (distinctDates.isEmpty()) return 0

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var checkDate = LocalDate.parse(todayStr, formatter)
        var streak = 0

        // If today has completion, count it
        if (distinctDates.contains(checkDate.format(formatter))) {
            streak++
            checkDate = checkDate.minusDays(1)
        } else {
            // Check yesterday
            val yesterday = checkDate.minusDays(1)
            if (!distinctDates.contains(yesterday.format(formatter))) {
                return 0
            }
            checkDate = yesterday
        }

        while (distinctDates.contains(checkDate.format(formatter))) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    suspend fun addHabit(habit: HabitEntity): Long = withContext(Dispatchers.IO) {
        val id = habitDao.insertHabit(habit)
        refreshWidget()
        id
    }

    suspend fun updateHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        habitDao.updateHabit(habit)
        refreshWidget()
    }

    suspend fun deleteHabit(habitId: Long) = withContext(Dispatchers.IO) {
        habitDao.deleteHabitById(habitId)
        refreshWidget()
    }

    suspend fun updateProfile(
        username: String,
        customTitle: String,
        avatarKey: String,
        customAvatarUri: String?,
        theme: String
    ) = withContext(Dispatchers.IO) {
        val current = habitDao.getUserProfileSync() ?: UserProfileEntity()
        val updated = current.copy(
            username = username,
            customTitle = customTitle,
            avatarKey = avatarKey,
            customAvatarUri = customAvatarUri,
            selectedTheme = theme
        )
        habitDao.insertOrUpdateProfile(updated)
    }

    suspend fun updateTheme(theme: String) = withContext(Dispatchers.IO) {
        val current = habitDao.getUserProfileSync() ?: UserProfileEntity()
        habitDao.insertOrUpdateProfile(current.copy(selectedTheme = theme))
    }

    fun refreshWidget() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, HabitWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, HabitWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (_: Exception) {
            // Ignore widget notification failure if provider not ready
        }
    }

    companion object {
        fun getTodayDateString(): String {
            return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }

        @Volatile
        private var INSTANCE: HabitRepository? = null

        fun getInstance(context: Context): HabitRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val instance = HabitRepository(db.habitDao(), context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

data class ToggleResult(
    val isCompleted: Boolean,
    val xpChange: Int,
    val newTotalXp: Int
)
