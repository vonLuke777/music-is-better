package pl.speakersync.grouping

import pl.speakersync.data.DiscoveredSpeaker
import pl.speakersync.data.GroupType
import pl.speakersync.data.SpeakerGroup
import pl.speakersync.data.SpeakerProtocol
import pl.speakersync.data.SyncCapability
import java.util.UUID

data class GroupValidationResult(
    val isValid: Boolean,
    val groupType: GroupType?,
    val message: String?
)

class GroupManager {
    fun validateSelection(selected: List<DiscoveredSpeaker>): GroupValidationResult {
        if (selected.isEmpty()) {
            return GroupValidationResult(false, null, "Wybierz co najmniej jeden głośnik.")
        }

        val protocols = selected.map { it.protocol }.toSet()
        if (protocols.size > 1) {
            return GroupValidationResult(
                isValid = false,
                groupType = null,
                message = "Wybierz głośniki tego samego typu (WiFi albo Bluetooth)."
            )
        }

        return when (selected.first().protocol) {
            SpeakerProtocol.DLNA -> GroupValidationResult(
                isValid = true,
                groupType = GroupType.WIFI_STREAM,
                message = "Strumień lossless 48 kHz z telefonu na głośniki WiFi (DLNA)."
            )
            SpeakerProtocol.BLUETOOTH -> {
                if (selected.size > 2) {
                    GroupValidationResult(
                        isValid = false,
                        groupType = null,
                        message = "Samsung Dual Audio: maksymalnie 2 głośniki Bluetooth."
                    )
                } else {
                    GroupValidationResult(
                        isValid = true,
                        groupType = GroupType.BLUETOOTH_ROUTE,
                        message = if (selected.size == 2) {
                            "Routing BT na 2 urządzenia — użyj panelu Media Samsung."
                        } else {
                            "Routing dźwięku z telefonu na Bluetooth (aptX/LDAC przez system)."
                        }
                    )
                }
            }
        }
    }

    fun createGroup(selected: List<DiscoveredSpeaker>): SpeakerGroup? {
        val validation = validateSelection(selected)
        if (!validation.isValid || validation.groupType == null) return null

        val typeLabel = when (validation.groupType) {
            GroupType.WIFI_STREAM -> "WiFi"
            GroupType.BLUETOOTH_ROUTE -> "BT"
        }

        return SpeakerGroup(
            id = UUID.randomUUID().toString(),
            name = "$typeLabel: ${selected.joinToString(" + ") { it.name }}",
            type = validation.groupType,
            speakers = selected
        )
    }

    fun syncLabel(capability: SyncCapability): String = when (capability) {
        SyncCapability.LIVE_STREAM -> "Strumień z telefonu"
        SyncCapability.PHONE_ROUTE -> "Routing Bluetooth"
    }
}
