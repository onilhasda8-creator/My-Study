package com.studyplanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Indigo-centric academic palette
val Indigo80  = Color(0xFFBDB9FF)
val Indigo40  = Color(0xFF4A45C0)
val Teal80    = Color(0xFF70EFDE)
val Teal40    = Color(0xFF006B5F)
val Coral80   = Color(0xFFFFB4A0)
val Coral40   = Color(0xFF9B3620)

private val DarkColorScheme = darkColorScheme(
    primary         = Indigo80,
    onPrimary       = Color(0xFF1A178A),
    primaryContainer= Color(0xFF322EAE),
    onPrimaryContainer = Color(0xFFE0DFFF),
    secondary       = Teal80,
    onSecondary     = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFF8CF8E5),
    tertiary        = Coral80,
    onTertiary      = Color(0xFF5C1909),
    background      = Color(0xFF111318),
    onBackground    = Color(0xFFE2E2E9),
    surface         = Color(0xFF111318),
    onSurface       = Color(0xFFE2E2E9),
    surfaceVariant  = Color(0xFF46464F),
    onSurfaceVariant= Color(0xFFC7C5D0),
    outline         = Color(0xFF90909A)
)

private val LightColorScheme = lightColorScheme(
    primary         = Indigo40,
    onPrimary       = Color.White,
    primaryContainer= Color(0xFFE2E0FF),
    onPrimaryContainer = Color(0xFF09006F),
    secondary       = Teal40,
    onSecondary     = Color.White,
    secondaryContainer = Color(0xFF9DF2DF),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary        = Coral40,
    onTertiary      = Color.White,
    background      = Color(0xFFFBF8FF),
    onBackground    = Color(0xFF1B1B21),
    surface         = Color(0xFFFBF8FF),
    onSurface       = Color(0xFF1B1B21),
    surfaceVariant  = Color(0xFFE4E1EC),
    onSurfaceVariant= Color(0xFF46464F),
    outline         = Color(0xFF777680)
)

// Preset subject colors (used in Subject color picker)
val SubjectColors = listOf(
    Color(0xFF4A45C0), // Indigo
    Color(0xFF006B5F), // Teal
    Color(0xFF9B3620), // Coral
    Color(0xFF006E1C), // Forest green
    Color(0xFF7B4F00), // Amber
    Color(0xFF69006E), // Purple
    Color(0xFF00629D), // Blue
    Color(0xFF8B2252), // Pink
)

@Composable
fun StudyPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // keep our custom palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
