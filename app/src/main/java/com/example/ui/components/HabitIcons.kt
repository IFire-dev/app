package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIconHelper {
    val availableIcons = listOf(
        "fitness_center" to Icons.Default.FitnessCenter,
        "water_drop" to Icons.Default.WaterDrop,
        "menu_book" to Icons.Default.MenuBook,
        "bolt" to Icons.Default.Bolt,
        "directions_run" to Icons.Default.DirectionsRun,
        "self_improvement" to Icons.Default.SelfImprovement,
        "bed" to Icons.Default.Bed,
        "code" to Icons.Default.Code,
        "psychology" to Icons.Default.Psychology,
        "favorite" to Icons.Default.Favorite,
        "shield" to Icons.Default.Shield,
        "star" to Icons.Default.Star,
        "check_circle" to Icons.Default.CheckCircle
    )

    fun getIcon(key: String): ImageVector {
        return availableIcons.find { it.first == key }?.second ?: Icons.Default.CheckCircle
    }

    val categories = listOf("All", "Fitness", "Health", "Mind", "Productivity", "Routine")

    val avatarPresets = listOf(
        AvatarPreset("shield", "Shield Knight", Icons.Default.Shield, 0xFF00E676),
        AvatarPreset("flame", "Phoenix Flame", Icons.Default.LocalFireDepartment, 0xFFFB923C),
        AvatarPreset("star", "Cosmic Voyager", Icons.Default.Star, 0xFFFFD600),
        AvatarPreset("bolt", "Cyber Hacker", Icons.Default.Bolt, 0xFFA855F7),
        AvatarPreset("zen", "Zen Monk", Icons.Default.SelfImprovement, 0xFF38BDF8),
        AvatarPreset("mind", "Sage Scholar", Icons.Default.Psychology, 0xFF10B981),
        AvatarPreset("runner", "Shadow Runner", Icons.Default.DirectionsRun, 0xFFF43F5E),
        AvatarPreset("heart", "Life Guardian", Icons.Default.Favorite, 0xFFEC4899)
    )

    fun getAvatar(key: String): AvatarPreset {
        return avatarPresets.find { it.id == key } ?: avatarPresets.first()
    }
}

data class AvatarPreset(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val colorHex: Long
)
