package pl.speakersync.media

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import pl.speakersync.SpeakerSyncApplication
import pl.speakersync.data.NowPlayingInfo

class MediaNotificationListener : NotificationListenerService() {

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            refreshNowPlaying()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            refreshNowPlaying()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshNowPlaying()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refreshNowPlaying()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refreshNowPlaying()
    }

    private fun refreshNowPlaying() {
        val repository = (application as SpeakerSyncApplication).container.nowPlayingRepository
        val manager = getSystemService(MediaSessionManager::class.java) ?: return
        val component = ComponentName(this, MediaNotificationListener::class.java)

        runCatching {
            val controllers = manager.getActiveSessions(component)
            controllers.forEach { it.unregisterCallback(callback) }

            val active = controllers.firstOrNull { controller ->
                controller.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: controllers.firstOrNull()

            active?.registerCallback(callback)

            active?.metadata?.let { metadata ->
                val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty()
                val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
                    ?: metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                    ?: metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                    ?: ""

                if (title.isNotBlank()) {
                    repository.update(NowPlayingInfo(title = title, artist = artist))
                    return@runCatching
                }
            }

            repository.update(null)
        }.onFailure {
            repository.update(null)
        }
    }
}
