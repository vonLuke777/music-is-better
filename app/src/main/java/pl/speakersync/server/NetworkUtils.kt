package pl.speakersync.server

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    fun getLocalIpAddress(): String? {
        return NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { networkInterface ->
                networkInterface.inetAddresses.toList().mapNotNull { address ->
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        address.hostAddress
                    } else {
                        null
                    }
                }
            }
            .firstOrNull { ip -> !ip.startsWith("169.254.") }
    }
}
