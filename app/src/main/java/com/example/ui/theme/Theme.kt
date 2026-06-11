package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Custom light-toned scheme aligning with the Geometric Balance design spec
private val GeometricColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    secondary = TextMedium,
    onSecondary = TextDark,
    background = ThemeBg,
    onBackground = TextDark,
    surface = KeypadBg,
    onSurface = TextDark,
    primaryContainer = HeaderPillBg,
    onPrimaryContainer = HeaderPillText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to light/balanced theme from the HTML layout
    dynamicColor: Boolean = false, // Disable dynamic colors to enforce Geometric Balance theme exactly
    content: @Composable () -> Unit,
) {
    // We strictly use GeometricColorScheme for Geometric Balance theme fidelity!
    MaterialTheme(
        colorScheme = GeometricColorScheme,
        typography = Typography,
        content = content
    )
}
