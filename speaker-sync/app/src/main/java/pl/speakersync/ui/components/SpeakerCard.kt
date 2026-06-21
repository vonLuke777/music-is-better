package pl.speakersync.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.speakersync.data.DiscoveredSpeaker
import pl.speakersync.data.SpeakerProtocol
import pl.speakersync.data.SyncCapability
import pl.speakersync.grouping.GroupManager

@Composable
fun SpeakerCard(
    speaker: DiscoveredSpeaker,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    groupManager: GroupManager = GroupManager()
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = speaker.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = speaker.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    ProtocolBadge(speaker.protocol)
                    SyncBadge(speaker.syncCapability, groupManager)
                    if (speaker.isConnected) {
                        AssistChip(onClick = {}, label = { Text("Połączony") })
                    }
                }
            }
            Checkbox(checked = selected, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
private fun ProtocolBadge(protocol: SpeakerProtocol) {
    val label = when (protocol) {
        SpeakerProtocol.DLNA -> "WiFi"
        SpeakerProtocol.BLUETOOTH -> "BT"
    }
    AssistChip(onClick = {}, label = { Text(label) })
}

@Composable
private fun SyncBadge(capability: SyncCapability, groupManager: GroupManager) {
    val color = when (capability) {
        SyncCapability.LIVE_STREAM -> Color(0xFF2E7D32)
        SyncCapability.PHONE_ROUTE -> Color(0xFF1565C0)
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = groupManager.syncLabel(capability),
                color = color
            )
        }
    )
}
