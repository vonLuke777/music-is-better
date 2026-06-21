package pl.speakersync.playback

import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.avtransport.callback.Play
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import org.fourthline.cling.support.avtransport.callback.Stop
import org.fourthline.cling.support.model.ProtocolInfo
import pl.speakersync.data.DiscoveredSpeaker
import pl.speakersync.discovery.DlnaDiscoverySource

class DlnaPlaybackManager(
    private val dlnaSource: DlnaDiscoverySource
) {
    fun pushLiveStream(
        speakers: List<DiscoveredSpeaker>,
        streamUrl: String,
        mimeType: String,
        title: String,
        onResult: (String) -> Unit
    ) {
        val service = dlnaSource.getUpnpService()?.controlPoint ?: run {
            onResult("Usługa DLNA nie jest gotowa.")
            return
        }

        if (speakers.isEmpty()) {
            onResult("Brak urządzeń DLNA.")
            return
        }

        var successCount = 0
        var failCount = 0
        val item = LiveStreamItem(streamUrl, mimeType, title)

        speakers.forEach { speaker ->
            val device = findDevice(speaker.id) ?: run {
                failCount++
                reportIfDone(successCount, failCount, speakers.size, onResult)
                return@forEach
            }

            runCatching {
                service.execute(
                    object : SetAVTransportURI(
                        device.findService(UDAServiceType("AVTransport", 1)),
                        streamUrl,
                        buildDidl(item)
                    ) {
                        override fun success(response: org.fourthline.cling.model.action.ActionInvocation<*>?) {
                            service.execute(object : Play(device.findService(UDAServiceType("AVTransport", 1))) {})
                            successCount++
                            reportIfDone(successCount, failCount, speakers.size, onResult)
                        }

                        override fun failure(
                            invocation: org.fourthline.cling.model.action.ActionInvocation<*>?,
                            operation: org.fourthline.cling.model.meta.Action?,
                            defaultMsg: String?
                        ) {
                            failCount++
                            reportIfDone(successCount, failCount, speakers.size, onResult)
                        }
                    }
                )
            }.onFailure {
                failCount++
                reportIfDone(successCount, failCount, speakers.size, onResult)
            }
        }
    }

    fun stopSpeakers(speakers: List<DiscoveredSpeaker>) {
        val service = dlnaSource.getUpnpService()?.controlPoint ?: return
        speakers.forEach { speaker ->
            val device = findDevice(speaker.id) ?: return@forEach
            runCatching {
                service.execute(object : Stop(device.findService(UDAServiceType("AVTransport", 1))) {})
            }
        }
    }

    private fun reportIfDone(
        success: Int,
        fail: Int,
        total: Int,
        onResult: (String) -> Unit
    ) {
        if (success + fail >= total) {
            onResult("DLNA: $success OK, $fail błędów. Strumień lossless z telefonu — odtwórz dźwięk na urządzeniu.")
        }
    }

    private fun findDevice(speakerId: String): Device<*, *, *>? {
        val udn = speakerId.removePrefix("dlna:")
        return dlnaSource.getUpnpService()?.registry?.devices?.find {
            it.identity.udn.identifierString == udn
        }
    }

    private fun buildDidl(item: LiveStreamItem): String {
        return """
            <DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">
                <item id="0" parentID="0" restricted="1">
                    <dc:title>${item.title}</dc:title>
                    <upnp:class>object.item.audioItem.audioBroadcast</upnp:class>
                    <res protocolInfo="${ProtocolInfo("http-get", item.mimeType, "*")}">${item.url}</res>
                </item>
            </DIDL-Lite>
        """.trimIndent()
    }

    private data class LiveStreamItem(
        val url: String,
        val mimeType: String,
        val title: String
    )
}

class BluetoothRouteManager(
    private val context: android.content.Context
) {
    fun routeToSpeaker(speaker: pl.speakersync.data.DiscoveredSpeaker): Boolean {
        val routeId = speaker.routeId ?: return false
        val router = androidx.mediarouter.media.MediaRouter.getInstance(context)
        val route = router.routes.find { it.id == routeId || it.description == speaker.address }
            ?: router.routes.find { it.name == speaker.name }
            ?: return false

        router.selectRoute(route)
        return true
    }

    fun dualAudioHint(selectedCount: Int): String {
        return if (selectedCount == 2) {
            "Samsung Dual Audio: panel Media → zaznacz oba głośniki BT."
        } else {
            "Dźwięk idzie bezpośrednio z telefonu — bez pośredników typu Spotify/YouTube."
        }
    }
}
