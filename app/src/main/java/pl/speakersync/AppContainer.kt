package pl.speakersync

import android.app.Application
import pl.speakersync.audio.PhoneAudioRouter
import pl.speakersync.data.DiscoveryRepository
import pl.speakersync.data.SettingsRepository
import pl.speakersync.grouping.GroupManager
import pl.speakersync.playback.BluetoothRouteManager
import pl.speakersync.playback.DlnaPlaybackManager

class AppContainer(application: Application) {
    val discoveryRepository = DiscoveryRepository(application)
    val settingsRepository = SettingsRepository(application)
    val groupManager = GroupManager()
    val dlnaPlaybackManager = DlnaPlaybackManager(discoveryRepository.getDlnaSource())
    val bluetoothRouteManager = BluetoothRouteManager(application)
    val phoneAudioRouter = PhoneAudioRouter(
        context = application,
        dlnaPlaybackManager = dlnaPlaybackManager,
        bluetoothRouteManager = bluetoothRouteManager
    )
    val nowPlayingRepository = NowPlayingRepository()
}
