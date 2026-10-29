package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppTheme(
    val id: String,
    val displayName: String,
    val previewColor: Color,
    val previewBg: Color
) {
    OLED("oled", "Pure OLED Black", OledPrimary, OledBlack),
    MIDNIGHT_EMERALD("midnight_emerald", "Midnight Emerald", EmeraldPrimary, EmeraldBg),
    CYBER_NEON("cyber_neon", "Cyber Neon", CyberPrimary, CyberBg),
    SUNSET_EMBER("sunset_ember", "Sunset Ember", SunsetPrimary, SunsetBg),
    ARCTIC_GLACIER("arctic_glacier", "Arctic Glacier", ArcticPrimary, ArcticBg),
    SYSTEM("system", "Material Dynamic", Color(0xFF818CF8), Color(0xFF1E1E2E));

    companion object {
        fun fromId(id: String): AppTheme {
            return entries.find { it.id == id } ?: OLED
        }
    }
}

private val OledColorScheme = darkColorScheme(
    primary = OledPrimary,
    onPrimary = OledOnPrimary,
    primaryContainer = Color(0xFF005326),
    onPrimaryContainer = Color(0xFF81FF9F),
    secondary = OledSecondary,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = Color(0xFF9CF4FF),
    tertiary = OledTertiary,
    onTertiary = Color(0xFF3F3200),
    background = OledBlack,
    onBackground = OledTextPrimary,
    surface = OledSurface,
    onSurface = OledTextPrimary,
    surfaceVariant = OledSurfaceElevated,
    onSurfaceVariant = OledTextSecondary,
    outline = OledSurfaceBorder
)

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFF6EE7B7),
    secondary = EmeraldSecondary,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = EmeraldTertiary,
    onTertiary = Color(0xFF451A03),
    background = EmeraldBg,
    onBackground = EmeraldTextPrimary,
    surface = EmeraldSurface,
    onSurface = EmeraldTextPrimary,
    surfaceVariant = EmeraldSurfaceElevated,
    onSurfaceVariant = EmeraldTextSecondary,
    outline = EmeraldSurfaceBorder
)

private val CyberColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = CyberOnPrimary,
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFE9D5FF),
    secondary = CyberSecondary,
    onSecondary = Color(0xFF083344),
    secondaryContainer = Color(0xFF155E75),
    onSecondaryContainer = Color(0xFFCFFAFE),
    tertiary = CyberTertiary,
    onTertiary = Color(0xFF4C0519),
    background = CyberBg,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceElevated,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberSurfaceBorder
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = SunsetOnPrimary,
    primaryContainer = Color(0xFF7C2D12),
    onPrimaryContainer = Color(0xFFFFEDD5),
    secondary = SunsetSecondary,
    onSecondary = Color(0xFF450A0A),
    secondaryContainer = Color(0xFF991B1B),
    onSecondaryContainer = Color(0xFFFEE2E2),
    tertiary = SunsetTertiary,
    onTertiary = Color(0xFF451A03),
    background = SunsetBg,
    onBackground = SunsetTextPrimary,
    surface = SunsetSurface,
    onSurface = SunsetTextPrimary,
    surfaceVariant = SunsetSurfaceElevated,
    onSurfaceVariant = SunsetTextSecondary,
    outline = SunsetSurfaceBorder
)

private val ArcticColorScheme = darkColorScheme(
    primary = ArcticPrimary,
    onPrimary = ArcticOnPrimary,
    primaryContainer = Color(0xFF0369A1),
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = ArcticSecondary,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = ArcticTertiary,
    onTertiary = Color(0xFF1E1B4B),
    background = ArcticBg,
    onBackground = ArcticTextPrimary,
    surface = ArcticSurface,
    onSurface = ArcticTextPrimary,
    surfaceVariant = ArcticSurfaceElevated,
    onSurfaceVariant = ArcticTextSecondary,
    outline = ArcticSurfaceBorder
)

@Composable
fun HabitForgeTheme(
    selectedThemeId: String = "oled",
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val appTheme = AppTheme.fromId(selectedThemeId)

    val colorScheme: ColorScheme = when (appTheme) {
        AppTheme.OLED -> OledColorScheme
        AppTheme.MIDNIGHT_EMERALD -> EmeraldColorScheme
        AppTheme.CYBER_NEON -> CyberColorScheme
        AppTheme.SUNSET_EMBER -> SunsetColorScheme
        AppTheme.ARCTIC_GLACIER -> ArcticColorScheme
        AppTheme.SYSTEM -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                OledColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
