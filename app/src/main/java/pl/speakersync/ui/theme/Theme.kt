package pl.speakersync.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BrandPurple = Color(0xFF7C3AED)
private val BrandPurpleDark = Color(0xFF4C1D95)
private val BrandOrange = Color(0xFFF97316)
private val BrandSurface = Color(0xFFFAF5FF)

private val LightColors = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = BrandPurpleDark,
    secondary = BrandOrange,
    onSecondary = Color.White,
    tertiary = Color(0xFFDB2777),
    background = BrandSurface,
    surface = Color.White,
    onBackground = Color(0xFF1F1235),
    onSurface = Color(0xFF1F1235)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA78BFA),
    onPrimary = BrandPurpleDark,
    primaryContainer = BrandPurpleDark,
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = BrandOrange,
    onSecondary = Color.White,
    background = Color(0xFF120822),
    surface = Color(0xFF1E1033),
    onBackground = Color(0xFFF3E8FF),
    onSurface = Color(0xFFF3E8FF)
)

@Composable
fun MusicIsBetterTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}

@Composable
fun SpeakerSyncTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) = MusicIsBetterTheme(darkTheme = darkTheme, content = content)
