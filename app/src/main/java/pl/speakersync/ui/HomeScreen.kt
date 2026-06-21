package pl.speakersync.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.speakersync.data.DeviceFilter
import pl.speakersync.ui.components.AmplifierHero
import pl.speakersync.ui.components.AppTitle
import pl.speakersync.ui.components.NowPlayingBar
import pl.speakersync.ui.components.SpeakerCard
import pl.speakersync.ui.viewmodel.MainUiState
import pl.speakersync.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    onOpenSettings: () -> Unit,
    onOpenGroupBuilder: () -> Unit,
    onOpenNotificationAccess: () -> Unit
) {
    val speakers = viewModel.filteredSpeakers()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AppTitle(showTagline = false) },
                actions = {
                    IconButton(onClick = { viewModel.cycleThemeMode() }) {
                        Icon(Icons.Default.DarkMode, contentDescription = "Tryb dzień/noc")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        },
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = onOpenGroupBuilder,
                text = { Text("Utwórz grupę (${uiState.selectedIds.size})") }
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
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item {
                    AmplifierHero(
                        isMusicActive = uiState.mirroring.isActive,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                item {
                    FilterRow(
                        selected = uiState.filter,
                        onSelect = viewModel::setFilter
                    )
                }

                if (speakers.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (uiState.isRefreshing) {
                                    CircularProgressIndicator()
                                }
                                Text(
                                    text = "Szukam głośników WiFi i Bluetooth…",
                                    modifier = Modifier.padding(top = 16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                } else {
                    items(speakers, key = { it.id }) { speaker ->
                        SpeakerCard(
                            speaker = speaker,
                            selected = uiState.selectedIds.contains(speaker.id),
                            onToggle = { viewModel.toggleSelection(speaker) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: DeviceFilter, onSelect: (DeviceFilter) -> Unit) {
    val filters = listOf(
        DeviceFilter.ALL to "Wszystkie",
        DeviceFilter.WIFI_ONLY to "WiFi",
        DeviceFilter.BT_ONLY to "BT"
    )

    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(label) }
            )
        }
    }
}
