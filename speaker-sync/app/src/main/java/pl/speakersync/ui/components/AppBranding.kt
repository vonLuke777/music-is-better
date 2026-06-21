package pl.speakersync.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.speakersync.ui.theme.rememberMusicBetterFontFamily

private val LogoGreen = Color(0xFF39FF14)

@Composable
fun AppTitle(showTagline: Boolean = true) {
    val logoFont = rememberMusicBetterFontFamily()
    val glow = LogoGreen.copy(alpha = 0.8f)

    androidx.compose.foundation.layout.Column {
        Text(
            text = "Music is Better",
            fontFamily = logoFont,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.colorScheme.primary,
            style = TextStyle(shadow = Shadow(color = glow, blurRadius = 10f))
        )
        if (showTagline) {
            Text(
                text = "Dźwięk z telefonu na wszystkie głośniki",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}
