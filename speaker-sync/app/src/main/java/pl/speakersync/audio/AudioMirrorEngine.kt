package pl.speakersync.audio

import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.speakersync.data.AudioQuality

class AudioMirrorEngine(
    private val ringBuffer: PcmRingBuffer
) {
    private var audioRecord: AudioRecord? = null
    private var captureJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    val sampleRate: Int = 48_000
    val channelCount: Int = 2
    val bytesPerSample: Int = 2

    @RequiresApi(Build.VERSION_CODES.Q)
    fun start(mediaProjection: MediaProjection, quality: AudioQuality) {
        stop()
        ringBuffer.reset()

        val channelMask = AudioFormat.CHANNEL_IN_STEREO
        val encoding = AudioFormat.ENCODING_PCM_16BIT

        val captureConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
            .addMatchingUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(android.media.AudioAttributes.USAGE_GAME)
            .addMatchingUsage(android.media.AudioAttributes.USAGE_UNKNOWN)
            .build()

        val minBuffer = AudioRecord.getMinBufferSize(sampleRate, channelMask, encoding)
        val bufferSize = maxOf(minBuffer, sampleRate * channelCount * bytesPerSample / 4)

        val record = AudioRecord.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(encoding)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelMask)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize * 2)
            .setAudioPlaybackCaptureConfig(captureConfig)
            .build()

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            error("Nie udało się uruchomić przechwytywania dźwięku.")
        }

        audioRecord = record
        record.startRecording()

        val readBuffer = ByteArray(bufferSize)
        captureJob = scope.launch {
            while (isActive) {
                val read = record.read(readBuffer, 0, readBuffer.size)
                if (read > 0) {
                    ringBuffer.write(readBuffer, 0, read)
                } else if (read < 0) {
                    break
                }
            }
        }
    }

    fun stop() {
        captureJob?.cancel()
        captureJob = null
        audioRecord?.run {
            runCatching { stop() }
            release()
        }
        audioRecord = null
        ringBuffer.close()
    }

    fun mimeType(quality: AudioQuality): String {
        return when (quality) {
            AudioQuality.LOSSLESS_PCM -> "audio/L16;rate=$sampleRate;channels=$channelCount"
            AudioQuality.HIGH_AAC -> "audio/aac"
        }
    }

    fun streamPath(quality: AudioQuality): String {
        return when (quality) {
            AudioQuality.LOSSLESS_PCM -> "/live.pcm"
            AudioQuality.HIGH_AAC -> "/live.aac"
        }
    }
}
