package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "Routine", // Fitness, Mind, Productivity, Health, Routine
    val iconKey: String = "check_circle",
    val colorHex: Long = 0xFF00E676,
    val xpReward: Int = 30,
    val targetDaysMask: Int = 127, // 1111111 binary for Mon-Sun
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val orderIndex: Int = 0
)
