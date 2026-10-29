package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.floor
import kotlin.math.sqrt

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val username: String = "Shadow Hunter",
    val customTitle: String = "Habit Initiate",
    val avatarKey: String = "shield",
    val customAvatarUri: String? = null,
    val totalXp: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val selectedTheme: String = "oled"
) {
    val level: Int
        get() = (floor(sqrt(totalXp.toDouble() / 50.0)).toInt() + 1).coerceAtLeast(1)

    val currentLevelBaseXp: Int
        get() {
            val lvl = level - 1
            return lvl * lvl * 50
        }

    val nextLevelTargetXp: Int
        get() {
            val lvl = level
            return lvl * lvl * 50
        }

    val currentLevelProgress: Float
        get() {
            val range = nextLevelTargetXp - currentLevelBaseXp
            if (range <= 0) return 1f
            val currentInLevel = (totalXp - currentLevelBaseXp).coerceAtLeast(0)
            return (currentInLevel.toFloat() / range.toFloat()).coerceIn(0f, 1f)
        }

    val rankTitle: String
        get() = when (level) {
            1 -> "Novice Seeker"
            2 -> "Habit Apprentice"
            3 -> "Steady Striver"
            4 -> "Disciplined Knight"
            5 -> "Iron Will"
            6 -> "Focus Master"
            7 -> "Habit Champion"
            8 -> "Ascended Titan"
            9 -> "Grandmaster"
            else -> "Legendary Paragon"
        }
}
