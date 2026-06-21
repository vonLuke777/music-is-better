#!/bin/bash
set -euo pipefail

echo "=== Przygotowanie poprawnej konfiguracji Gradle ==="

cat > settings.gradle.kts <<'EOF'
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MusicIsBetter"
include(":app")
EOF

cat > app/build.gradle.kts <<'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "pl.speakersync"
    compileSdk = 35

    defaultConfig {
        applicationId = "pl.speakersync"
        minSdk = 29
        targetSdk = 35
        versionCode = 2
        versionName = "2.0.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.4")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.mediarouter:mediarouter:1.7.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("org.eclipse.jetty:jetty-server:8.1.22.v20160922")
    implementation("org.eclipse.jetty:jetty-servlet:8.1.22.v20160922")
    implementation("org.eclipse.jetty:jetty-client:8.1.22.v20160922")
    implementation("org.slf4j:slf4j-android:1.7.36")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
EOF

echo "=== Naprawa plikow Kotlin (znane bledy kompilacji) ==="

mkdir -p app/src/main/java/pl/speakersync/discovery
mkdir -p app/src/main/java/pl/speakersync/playback

cat > app/src/main/java/pl/speakersync/discovery/DlnaDiscoverySource.kt <<'EOF'
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
EOF

cat > app/src/main/java/pl/speakersync/discovery/BluetoothDiscoverySource.kt <<'EOF'
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
EOF

cat > app/src/main/java/pl/speakersync/playback/RemotePlaybackManagers.kt <<'EOF'
package pl.speakersync.playback

import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.Service
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

            val avTransport = findAvTransportService(device) ?: run {
                failCount++
                reportIfDone(successCount, failCount, speakers.size, onResult)
                return@forEach
            }

            runCatching {
                service.execute(
                    object : SetAVTransportURI(
                        avTransport,
                        streamUrl,
                        buildDidl(item)
                    ) {
                        override fun success(response: org.fourthline.cling.model.action.ActionInvocation<*>?) {
                            service.execute(object : Play(avTransport) {})
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
            val avTransport = findAvTransportService(device) ?: return@forEach
            runCatching {
                service.execute(object : Stop(avTransport) {})
            }
        }
    }

    private fun findAvTransportService(device: Device<*, *, *>): Service<*, *, *>? {
        return device.findService(UDAServiceType("AVTransport", 1))
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
        val protocol = ProtocolInfo("http-get", "*", item.mimeType, "*")
        return """
            <DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">
                <item id="0" parentID="0" restricted="1">
                    <dc:title>${item.title}</dc:title>
                    <upnp:class>object.item.audioItem.audioBroadcast</upnp:class>
                    <res protocolInfo="$protocol">${item.url}</res>
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
        val route = router.routes.find { it.id == routeId || it.name == speaker.name }
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
EOF

cat > app/src/main/java/pl/speakersync/AppContainer.kt <<'EOF'
package pl.speakersync

import android.app.Application
import pl.speakersync.audio.PhoneAudioRouter
import pl.speakersync.data.DiscoveryRepository
import pl.speakersync.data.SettingsRepository
import pl.speakersync.grouping.GroupManager
import pl.speakersync.media.NowPlayingRepository
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
EOF

echo "=== Pobieranie bibliotek Cling (DLNA) ==="
mkdir -p app/libs
BASE=http://4thline.org/m2
curl -fsSL "${BASE}/org/fourthline/cling/cling-core/2.1.2/cling-core-2.1.2.jar" -o app/libs/cling-core-2.1.2.jar
curl -fsSL "${BASE}/org/fourthline/cling/cling-support/2.1.2/cling-support-2.1.2.jar" -o app/libs/cling-support-2.1.2.jar
curl -fsSL "${BASE}/org/seamless/seamless-util/1.1.2/seamless-util-1.1.2.jar" -o app/libs/seamless-util-1.1.2.jar
curl -fsSL "${BASE}/org/seamless/seamless-http/1.1.2/seamless-http-1.1.2.jar" -o app/libs/seamless-http-1.1.2.jar
curl -fsSL "${BASE}/org/seamless/seamless-xml/1.1.2/seamless-xml-1.1.2.jar" -o app/libs/seamless-xml-1.1.2.jar
ls -la app/libs

echo "=== Gotowe ==="
