package pl.speakersync.discovery

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.android.AndroidUpnpServiceImpl
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.RemoteDevice
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.registry.DefaultRegistryListener
import org.fourthline.cling.registry.Registry
import pl.speakersync.data.DiscoveredSpeaker
import pl.speakersync.data.SpeakerProtocol
import pl.speakersync.data.SyncCapability
import java.util.concurrent.CopyOnWriteArrayList

class DlnaUpnpService : AndroidUpnpServiceImpl()

class DlnaDiscoverySource(
    private val context: Context
) {
    private val devices = CopyOnWriteArrayList<DiscoveredSpeaker>()
    private var upnpService: AndroidUpnpService? = null
    private var listener: ((List<DiscoveredSpeaker>) -> Unit)? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            upnpService = service as AndroidUpnpService
            upnpService?.registry?.addListener(registryListener)
            upnpService?.controlPoint?.search()
            emitDevices()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            upnpService?.registry?.removeListener(registryListener)
            upnpService = null
            devices.clear()
            emitDevices()
        }
    }

    private val registryListener = object : DefaultRegistryListener() {
        override fun remoteDeviceAdded(registry: Registry?, device: RemoteDevice?) {
            device?.let { addDevice(it) }
        }

        override fun remoteDeviceRemoved(registry: Registry?, device: RemoteDevice?) {
            device?.let { removeDevice(it) }
        }

        override fun localDeviceAdded(registry: Registry?, device: LocalDevice?) = Unit
        override fun localDeviceRemoved(registry: Registry?, device: LocalDevice?) = Unit
    }

    fun start(onUpdate: (List<DiscoveredSpeaker>) -> Unit) {
        listener = onUpdate
        context.bindService(
            Intent(context, DlnaUpnpService::class.java),
            serviceConnection,
            Service.BIND_AUTO_CREATE
        )
    }

    fun refresh() {
        upnpService?.controlPoint?.search()
    }

    fun stop() {
        listener = null
        runCatching { context.unbindService(serviceConnection) }
        devices.clear()
    }

    fun getUpnpService(): AndroidUpnpService? = upnpService

    private fun addDevice(device: Device<*, *, *>) {
        val type = device.type
        if (type !is UDADeviceType || type.type != "MediaRenderer") return

        val speaker = DiscoveredSpeaker(
            id = "dlna:${device.identity.udn.identifierString}",
            name = device.details.friendlyName ?: device.displayString,
            protocol = SpeakerProtocol.DLNA,
            syncCapability = SyncCapability.LIVE_STREAM,
            address = device.displayString
        )

        devices.removeAll { it.id == speaker.id }
        devices.add(speaker)
        emitDevices()
    }

    private fun removeDevice(device: Device<*, *, *>) {
        devices.removeAll { it.id == "dlna:${device.identity.udn.identifierString}" }
        emitDevices()
    }

    private fun emitDevices() {
        listener?.invoke(devices.toList())
    }
}
