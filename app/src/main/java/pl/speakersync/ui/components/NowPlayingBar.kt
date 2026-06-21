package pl.speakersync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.speakersync.data.NowPlayingInfo

private val BarBlack = Color(0xFF0A0A0A)
private val BarGreen = Color(0xFF39FF14)

@Composable
fun NowPlayingBar(
    nowPlaying: NowPlayingInfo?,
    notificationAccessEnabled: Boolean,
    onRequestNotificationAccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BarBlack)
            .then(
                if (!notificationAccessEnabled) {
                    Modifier.clickable(onClick = onRequestNotificationAccess)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        when {
            !notificationAccessEnabled -> {
                Text(
                    text = "Włącz dostęp do powiadomień",
                    color = BarGreen.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Dotknij, aby pokazać wykonawcę i tytuł utworu",
                    color = Color(0xFF888890),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            nowPlaying != null -> {
                Text(
                    text = nowPlaying.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = nowPlaying.artist.ifBlank { "Nieznany wykonawca" },
                    color = BarGreen.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )
            }
            else -> {
                Text(
                    text = "Brak odtwarzanego utworu",
                    color = Color(0xFF777780),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Odtwórz muzykę w aplikacji Muzyka lub innym odtwarzaczu",
                    color = Color(0xFF555560),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                )
            }
        }
    }
}
