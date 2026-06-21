package pl.speakersync.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.speakersync.discovery.BluetoothDiscoverySource
import pl.speakersync.discovery.DlnaDiscoverySource

class DiscoveryRepository(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val bluetoothSource = BluetoothDiscoverySource(appContext)
    private val dlnaSource = DlnaDiscoverySource(appContext)

    private val bluetoothDevices = MutableStateFlow<List<DiscoveredSpeaker>>(emptyList())
    private val dlnaDevices = MutableStateFlow<List<DiscoveredSpeaker>>(emptyList())

    private val _speakers = MutableStateFlow<List<DiscoveredSpeaker>>(emptyList())
    val speakers: StateFlow<List<DiscoveredSpeaker>> = _speakers.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        combine(bluetoothDevices, dlnaDevices) { bt, dlna ->
            mergeLists(bt, dlna)
        }.onEach { merged ->
            _speakers.value = merged
        }.launchIn(scope)

        bluetoothSource.observe().onEach { bluetoothDevices.value = it }.launchIn(scope)
        dlnaSource.start { dlnaDevices.value = it }
        _isScanning.value = true
    }

    fun refresh() {
        dlnaSource.refresh()
    }

    fun getDlnaSource(): DlnaDiscoverySource = dlnaSource

    fun filterSpeakers(filter: DeviceFilter, speakers: List<DiscoveredSpeaker>): List<DiscoveredSpeaker> {
        return when (filter) {
            DeviceFilter.ALL -> speakers
            DeviceFilter.WIFI_ONLY -> speakers.filter { it.protocol == SpeakerProtocol.DLNA }
            DeviceFilter.BT_ONLY -> speakers.filter { it.protocol == SpeakerProtocol.BLUETOOTH }
        }
    }

    private fun mergeLists(
        bluetooth: List<DiscoveredSpeaker>,
        dlna: List<DiscoveredSpeaker>
    ): List<DiscoveredSpeaker> {
        return (dlna + bluetooth)
            .distinctBy { it.id }
            .sortedWith(
                compareByDescending<DiscoveredSpeaker> { it.isConnected }
                    .thenBy { it.name.lowercase() }
            )
    }
}
