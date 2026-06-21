package pl.speakersync.data

enum class SpeakerProtocol {
    DLNA,
    BLUETOOTH
}

enum class SyncCapability {
    LIVE_STREAM,
    PHONE_ROUTE
}

enum class DeviceFilter {
    ALL,
    WIFI_ONLY,
    BT_ONLY
}

enum class GroupType {
    WIFI_STREAM,
    BLUETOOTH_ROUTE
}

enum class AudioQuality {
    LOSSLESS_PCM,
    HIGH_AAC
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

data class DiscoveredSpeaker(
    val id: String,
    val name: String,
    val protocol: SpeakerProtocol,
    val syncCapability: SyncCapability,
    val address: String,
    val routeId: String? = null,
    val isConnected: Boolean = false
)

data class SpeakerGroup(
    val id: String,
    val name: String,
    val type: GroupType,
    val speakers: List<DiscoveredSpeaker>
)

data class MirroringState(
    val isActive: Boolean = false,
    val streamUrl: String? = null,
    val message: String? = null
)

data class NowPlayingInfo(
    val title: String,
    val artist: String
)

data class AppSettings(
    val autoDiscovery: Boolean = true,
    val scanTimeoutSeconds: Int = 30,
    val audioQuality: AudioQuality = AudioQuality.LOSSLESS_PCM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val amplifierImageUri: String? = null
)
