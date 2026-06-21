package pl.speakersync.discovery

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pl.speakersync.data.DiscoveredSpeaker
import pl.speakersync.data.SpeakerProtocol
import pl.speakersync.data.SyncCapability

class BluetoothDiscoverySource(
    private val context: Context
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    @SuppressLint("MissingPermission")
    fun observe(): Flow<List<DiscoveredSpeaker>> = callbackFlow {
        if (adapter == null || !adapter.isEnabled) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        fun emitDevices() {
            trySend(collectBluetoothSpeakers())
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED,
                    BluetoothDevice.ACTION_ACL_DISCONNECTED,
                    BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
                    BluetoothAdapter.ACTION_STATE_CHANGED -> emitDevices()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        emitDevices()

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }

    @SuppressLint("MissingPermission")
    private fun collectBluetoothSpeakers(): List<DiscoveredSpeaker> {
        val adapter = adapter ?: return emptyList()
        val connectedAddresses = connectedA2dpAddresses()

        return adapter.bondedDevices
            .map { device ->
                DiscoveredSpeaker(
                    id = "bt:${device.address}",
                    name = device.name ?: device.address,
                    protocol = SpeakerProtocol.BLUETOOTH,
                    syncCapability = SyncCapability.PHONE_ROUTE,
                    address = device.address,
                    routeId = device.address,
                    isConnected = connectedAddresses.contains(device.address)
                )
            }
            .sortedByDescending { it.isConnected }
    }

    @SuppressLint("MissingPermission")
    private fun connectedA2dpAddresses(): Set<String> {
        val a2dp = bluetoothManager?.getConnectedDevices(BluetoothProfile.A2DP).orEmpty()
        val headset = bluetoothManager?.getConnectedDevices(BluetoothProfile.HEADSET).orEmpty()
        return (a2dp + headset).map { it.address }.toSet()
    }
}
