package metropolia.myapplication.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Both native hosts use a light canvas. Standard M3 components inherit these roles.
private val QubeColors = lightColorScheme(
    primary = Color(0xFF365E91),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5E3FF),
    onPrimaryContainer = Color(0xFF102F54),
    inversePrimary = Color(0xFFA7C8FF),
    surfaceTint = Color(0xFF365E91),
    secondary = Color(0xFF505F76),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD8E3F8),
    onSecondaryContainer = Color(0xFF26364B),
    tertiary = Color(0xFF306A58),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB4EFDA),
    onTertiaryContainer = Color(0xFF073C2E),
    background = Color(0xFFF9F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFF9F9FF),
    onSurface = Color(0xFF191C20),
    onSurfaceVariant = Color(0xFF424750),
    surfaceVariant = Color(0xFFDFE2EB),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF3F3FA),
    surfaceContainer = Color(0xFFEDEDF4),
    surfaceContainerHigh = Color(0xFFE7E8EF),
    surfaceContainerHighest = Color(0xFFE1E2E9),
    outline = Color(0xFF737780),
    outlineVariant = Color(0xFFC3C6CF),
)

@Composable
internal fun QubeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = QubeColors, content = content)
}
