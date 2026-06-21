package pl.speakersync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.speakersync.data.GroupType
import pl.speakersync.data.ThemeMode
import pl.speakersync.ui.components.AmplifierHero
import pl.speakersync.ui.components.NowPlayingBar
import pl.speakersync.ui.components.AppTitle
import pl.speakersync.ui.viewmodel.MainUiState
import pl.speakersync.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupBuilderScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenMirror: () -> Unit
) {
    val selected = uiState.speakers.filter { uiState.selectedIds.contains(it.id) }
    val validation = uiState.validation

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grupa głośników") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Wybrane głośniki (${selected.size})", style = MaterialTheme.typography.titleMedium)
            selected.forEach { speaker ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${speaker.name} (${speaker.protocol.name})",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            validation?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = it.message ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = if (it.isValid) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            Button(
                onClick = {
                    if (viewModel.createGroup() != null) onOpenMirror()
                },
                enabled = validation?.isValid == true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (validation?.groupType) {
                        GroupType.WIFI_STREAM -> "Przekieruj dźwięk na WiFi"
                        GroupType.BLUETOOTH_ROUTE -> "Przekieruj dźwięk na Bluetooth"
                        null -> "Utwórz grupę"
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MirrorScreen(
    uiState: MainUiState,
    onBack: () -> Unit,
    onStartMirroring: () -> Unit,
    onStopMirroring: () -> Unit,
    onOpenNotificationAccess: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Music is Better") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        },
        bottomBar = {
            NowPlayingBar(
                nowPlaying = uiState.nowPlaying,
                notificationAccessEnabled = uiState.notificationAccessEnabled,
                onRequestNotificationAccess = onOpenNotificationAccess
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AmplifierHero(
                isMusicActive = uiState.mirroring.isActive,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.activeGroup?.let { group ->
                Text("Grupa: ${group.name}", style = MaterialTheme.typography.titleMedium)
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Jak to działa", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Aplikacja NIE korzysta ze Spotify, YouTube ani innych serwisów. " +
                            "Przekierowuje dźwięk odtwarzany na telefonie (pliki, gry, przeglądarka) na wybrane głośniki.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Bluetooth: najlepsza jakość przez system (aptX / LDAC). " +
                            "WiFi/DLNA: strumień lossless PCM 48 kHz stereo.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (uiState.mirroring.isActive) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Aktywne przekierowanie", style = MaterialTheme.typography.titleSmall)
                        uiState.mirroring.streamUrl?.let {
                            Text("Strumień: $it", style = MaterialTheme.typography.bodySmall)
                        }
                        uiState.mirroring.message?.let {
                            Text(it, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
                OutlinedButton(onClick = onStopMirroring, modifier = Modifier.fillMaxWidth()) {
                    Text("Zatrzymaj")
                }
            } else {
                Button(onClick = onStartMirroring, modifier = Modifier.fillMaxWidth()) {
                    Text("Start — przekieruj dźwięk z telefonu")
                }
            }

            uiState.statusMessage?.let {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(text = it, modifier = Modifier.padding(12.dp))
                }
            }

            Text(
                text = "Po starcie odtwórz muzykę lub dźwięk na telefonie. " +
                    "Aplikacje blokujące przechwytywanie (np. Netflix) mogą nie działać — to ograniczenie Androida.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    autoDiscovery: Boolean,
    scanTimeout: Int,
    themeMode: ThemeMode,
    onAutoDiscoveryChange: (Boolean) -> Unit,
    onScanTimeoutChange: (Int) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ustawienia") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Wygląd", style = MaterialTheme.typography.titleMedium)
            Text("Tryb dzień / noc", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeModeChange(ThemeMode.LIGHT) },
                    label = { Text("Dzień") }
                )
                FilterChip(
                    selected = themeMode == ThemeMode.DARK,
                    onClick = { onThemeModeChange(ThemeMode.DARK) },
                    label = { Text("Noc") }
                )
                FilterChip(
                    selected = themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeModeChange(ThemeMode.SYSTEM) },
                    label = { Text("System") }
                )
            }

            Text("Głośniki", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Automatyczne wykrywanie")
                Switch(checked = autoDiscovery, onCheckedChange = onAutoDiscoveryChange)
            }
            Text("Skan co $scanTimeout s", style = MaterialTheme.typography.bodySmall)

            androidx.compose.material3.OutlinedTextField(
                value = scanTimeout.toString(),
                onValueChange = { value -> value.toIntOrNull()?.let(onScanTimeoutChange) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Odświeżanie listy głośników (sekundy)") }
            )

            Text(
                text = "Domyślna jakość WiFi: PCM lossless 48 kHz stereo. " +
                    "Telefon i głośniki muszą być w tej samej sieci WiFi.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
