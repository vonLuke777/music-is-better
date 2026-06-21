package pl.speakersync.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun rememberMusicBetterFontFamily(): FontFamily {
    return remember { FontFamily.Serif }
}

@Composable
fun rememberMusicBetterSubtitleFontFamily(): FontFamily {
    return remember { FontFamily.SansSerif }
}
