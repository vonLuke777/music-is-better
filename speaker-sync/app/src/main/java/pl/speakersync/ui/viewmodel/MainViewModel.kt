package pl.speakersync.ui.viewmodel

import android.app.Application
import android.media.projection.MediaProjection
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.speakersync.SpeakerSyncApplication
import pl.speakersync.data.DeviceFilter
import pl.speakersync.data.DiscoveredSpeaker
import pl.speakersync.data.GroupType
import pl.speakersync.data.MirroringState
import pl.speakersync.data.NowPlayingInfo
import pl.speakersync.data.SpeakerGroup
import pl.speakersync.data.ThemeMode
import pl.speakersync.grouping.GroupValidationResult

data class MainUiState(
    val speakers: List<DiscoveredSpeaker> = emptyList(),
    val filter: DeviceFilter = DeviceFilter.ALL,
    val selectedIds: Set<String> = emptySet(),
    val isRefreshing: Boolean = false,
    val validation: GroupValidationResult? = null,
    val activeGroup: SpeakerGroup? = null,
    val mirroring: MirroringState = MirroringState(),
    val nowPlaying: NowPlayingInfo? = null,
    val notificationAccessEnabled: Boolean = false,
    val statusMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as SpeakerSyncApplication).container
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val settings = container.settingsRepository.settings

    private var autoRefreshJob: Job? = null
    private var pendingGroup: SpeakerGroup? = null

    init {
        viewModelScope.launch {
            container.discoveryRepository.speakers.collect { speakers ->
                _uiState.update { it.copy(speakers = speakers) }
            }
        }

        viewModelScope.launch {
            container.phoneAudioRouter.state.collect { mirroring ->
                _uiState.update { it.copy(mirroring = mirroring) }
            }
        }

        viewModelScope.launch {
            container.nowPlayingRepository.nowPlaying.collect { track ->
                _uiState.update { it.copy(nowPlaying = track) }
            }
        }

        refreshNotificationAccessState()
        startAutoRefresh()
    }

    fun setFilter(filter: DeviceFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun filteredSpeakers(): List<DiscoveredSpeaker> {
        return container.discoveryRepository.filterSpeakers(_uiState.value.filter, _uiState.value.speakers)
    }

    fun toggleSelection(speaker: DiscoveredSpeaker) {
        _uiState.update { state ->
            val updated = state.selectedIds.toMutableSet()
            if (updated.contains(speaker.id)) updated.remove(speaker.id) else updated.add(speaker.id)
            val selected = state.speakers.filter { updated.contains(it.id) }
            state.copy(
                selectedIds = updated,
                validation = container.groupManager.validateSelection(selected)
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            container.discoveryRepository.refresh()
            delay(1500)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun createGroup(): SpeakerGroup? {
        val selected = _uiState.value.speakers.filter { _uiState.value.selectedIds.contains(it.id) }
        val group = container.groupManager.createGroup(selected)
        if (group != null) {
            _uiState.update { it.copy(activeGroup = group, statusMessage = it.validation?.message) }
        }
        return group
    }

    fun startPhoneAudioRouting(onRequestProjection: () -> Unit) {
        val group = _uiState.value.activeGroup ?: return

        when (group.type) {
            GroupType.BLUETOOTH_ROUTE -> {
                val state = container.phoneAudioRouter.startBluetoothRoute(group)
                _uiState.update { it.copy(statusMessage = state.message) }
            }
            GroupType.WIFI_STREAM -> {
                pendingGroup = group
                onRequestProjection()
            }
        }
    }

    fun onMediaProjectionGranted(projection: MediaProjection) {
        val group = pendingGroup ?: _uiState.value.activeGroup ?: return
        pendingGroup = null
        val quality = container.settingsRepository.settings.value.audioQuality
        val state = container.phoneAudioRouter.startWifiStream(group, projection, quality)
        _uiState.update { it.copy(statusMessage = state.message, activeGroup = group) }
    }

    fun stopPhoneAudioRouting() {
        container.phoneAudioRouter.stop()
        _uiState.update { it.copy(statusMessage = "Przekierowanie dźwięku zatrzymane.") }
    }

    fun updateSettings(autoDiscovery: Boolean, scanTimeout: Int) {
        container.settingsRepository.update {
            it.copy(autoDiscovery = autoDiscovery, scanTimeoutSeconds = scanTimeout)
        }
        if (autoDiscovery) startAutoRefresh() else autoRefreshJob?.cancel()
    }

    fun updateThemeMode(mode: ThemeMode) {
        container.settingsRepository.update { it.copy(themeMode = mode) }
    }

    fun cycleThemeMode() {
        val current = container.settingsRepository.settings.value.themeMode
        val next = when (current) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        updateThemeMode(next)
    }

    fun refreshNotificationAccessState() {
        _uiState.update {
            it.copy(
                notificationAccessEnabled = container.nowPlayingRepository
                    .isNotificationAccessEnabled(appContext)
            )
        }
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                val timeout = container.settingsRepository.settings.value.scanTimeoutSeconds * 1000L
                delay(timeout)
                if (container.settingsRepository.settings.value.autoDiscovery) {
                    container.discoveryRepository.refresh()
                }
            }
        }
    }

    override fun onCleared() {
        container.phoneAudioRouter.stop()
        super.onCleared()
    }
}
