package pl.speakersync.audio

import android.content.Context
import android.media.projection.MediaProjection
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.speakersync.data.AudioQuality
import pl.speakersync.data.MirroringState
import pl.speakersync.data.SpeakerGroup
import pl.speakersync.data.GroupType
import pl.speakersync.playback.BluetoothRouteManager
import pl.speakersync.playback.DlnaPlaybackManager
import pl.speakersync.server.LiveAudioStreamServer
import pl.speakersync.server.NetworkUtils

class PhoneAudioRouter(
    private val context: Context,
    private val dlnaPlaybackManager: DlnaPlaybackManager,
    private val bluetoothRouteManager: BluetoothRouteManager
) {
    private val ringBuffer = PcmRingBuffer(48000 * 2 * 2 * 3)
    private val mirrorEngine = AudioMirrorEngine(ringBuffer)
    private var streamServer: LiveAudioStreamServer? = null
    private var port: Int = DEFAULT_PORT
    private var activeGroup: SpeakerGroup? = null

    private val _state = MutableStateFlow(MirroringState())
    val state: StateFlow<MirroringState> = _state.asStateFlow()

    fun startBluetoothRoute(group: SpeakerGroup): MirroringState {
        stopInternal(releaseProjectionOnly = true)
        activeGroup = group

        val speaker = group.speakers.first()
        val routed = bluetoothRouteManager.routeToSpeaker(speaker)
        val dualHint = bluetoothRouteManager.dualAudioHint(group.speakers.size)

        val message = buildString {
            append("Dźwięk z telefonu kierowany na Bluetooth (najlepszy kodek systemowy: aptX/LDAC). ")
            append(dualHint)
            if (!routed) append(" Jeśli dźwięk nie gra, wybierz głośnik w ustawieniach dźwięku.")
        }

        _state.value = MirroringState(
            isActive = true,
            streamUrl = null,
            message = message
        )
        return _state.value
    }

    fun startWifiStream(
        group: SpeakerGroup,
        mediaProjection: MediaProjection,
        quality: AudioQuality
    ): MirroringState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val error = MirroringState(message = "Przechwytywanie dźwięku wymaga Androida 10+.")
            _state.value = error
            return error
        }

        stopInternal(releaseProjectionOnly = false)
        activeGroup = group

        mirrorEngine.start(mediaProjection, quality)
        ensureServerRunning(quality)

        val ip = NetworkUtils.getLocalIpAddress()
        if (ip == null) {
            stopInternal(releaseProjectionOnly = false)
            val error = MirroringState(message = "Brak adresu WiFi. Połącz telefon z siecią WiFi.")
            _state.value = error
            return error
        }

        val streamUrl = "http://$ip:$port${mirrorEngine.streamPath(quality)}"
        dlnaPlaybackManager.pushLiveStream(
            speakers = group.speakers,
            streamUrl = streamUrl,
            mimeType = mirrorEngine.mimeType(quality),
            title = "Music is Better — dźwięk z telefonu"
        ) { result ->
            _state.value = _state.value.copy(message = result)
        }

        _state.value = MirroringState(
            isActive = true,
            streamUrl = streamUrl,
            message = "Strumień lossless z telefonu: $streamUrl. Odtwórz muzykę, grę lub plik na telefonie."
        )
        return _state.value
    }

    fun stop() {
        stopInternal(releaseProjectionOnly = false)
        _state.value = MirroringState(isActive = false)
    }

    private fun ensureServerRunning(quality: AudioQuality) {
        if (streamServer?.isAlive == true) return

        repeat(10) { attempt ->
            val candidate = DEFAULT_PORT + attempt
            val server = LiveAudioStreamServer(
                port = candidate,
                ringBuffer = ringBuffer,
                mimeType = mirrorEngine.mimeType(quality),
                streamPath = mirrorEngine.streamPath(quality)
            )
            runCatching {
                server.start(SOCKET_READ_TIMEOUT, false)
                streamServer = server
                port = candidate
                return
            }
        }
        error("Nie udało się uruchomić serwera strumienia audio.")
    }

    private fun stopInternal(releaseProjectionOnly: Boolean) {
        if (!releaseProjectionOnly) {
            activeGroup?.let { group ->
                if (group.type == GroupType.WIFI_STREAM) {
                    dlnaPlaybackManager.stopSpeakers(group.speakers)
                }
            }
        }
        mirrorEngine.stop()
        streamServer?.stop()
        streamServer = null
        activeGroup = null
        ringBuffer.reset()
    }

    companion object {
        const val DEFAULT_PORT = 8770
    }
}
