package pl.speakersync.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("speaker_sync_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun update(transform: (AppSettings) -> AppSettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        prefs.edit()
            .putBoolean(KEY_AUTO_DISCOVERY, updated.autoDiscovery)
            .putInt(KEY_SCAN_TIMEOUT, updated.scanTimeoutSeconds)
            .putString(KEY_AUDIO_QUALITY, updated.audioQuality.name)
            .putString(KEY_THEME_MODE, updated.themeMode.name)
            .putString(KEY_AMPLIFIER_URI, updated.amplifierImageUri)
            .apply()
    }

    private fun load(): AppSettings {
        val quality = runCatching {
            AudioQuality.valueOf(prefs.getString(KEY_AUDIO_QUALITY, AudioQuality.LOSSLESS_PCM.name)!!)
        }.getOrDefault(AudioQuality.LOSSLESS_PCM)

        val themeMode = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)!!)
        }.getOrDefault(ThemeMode.SYSTEM)

        return AppSettings(
            autoDiscovery = prefs.getBoolean(KEY_AUTO_DISCOVERY, true),
            scanTimeoutSeconds = prefs.getInt(KEY_SCAN_TIMEOUT, 30),
            audioQuality = quality,
            themeMode = themeMode,
            amplifierImageUri = prefs.getString(KEY_AMPLIFIER_URI, null)
        )
    }

    companion object {
        private const val KEY_AUTO_DISCOVERY = "auto_discovery"
        private const val KEY_SCAN_TIMEOUT = "scan_timeout"
        private const val KEY_AUDIO_QUALITY = "audio_quality"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AMPLIFIER_URI = "amplifier_image_uri"
    }
}
