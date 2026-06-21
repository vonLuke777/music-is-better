package pl.speakersync.audio

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PcmRingBuffer(capacityBytes: Int) {
    private val buffer = ByteArray(capacityBytes)
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private var readIndex = 0
    private var writeIndex = 0
    private var size = 0
    private var closed = false

    fun write(data: ByteArray, offset: Int, length: Int) {
        lock.withLock {
            var remaining = length
            var srcOffset = offset
            while (remaining > 0) {
                if (size == buffer.size) {
                    val discard = minOf(remaining, buffer.size / 8)
                    readIndex = (readIndex + discard) % buffer.size
                    size -= discard
                    srcOffset += discard
                    remaining -= discard
                    continue
                }
                val chunk = minOf(remaining, buffer.size - size)
                for (i in 0 until chunk) {
                    buffer[writeIndex] = data[srcOffset + i]
                    writeIndex = (writeIndex + 1) % buffer.size
                }
                size += chunk
                srcOffset += chunk
                remaining -= chunk
            }
            notEmpty.signalAll()
        }
    }

    fun readBlocking(target: ByteArray, offset: Int, length: Int): Int {
        lock.withLock {
            while (size == 0 && !closed) {
                notEmpty.await()
            }
            if (size == 0 && closed) return -1
            val chunk = minOf(length, size)
            for (i in 0 until chunk) {
                target[offset + i] = buffer[readIndex]
                readIndex = (readIndex + 1) % buffer.size
            }
            size -= chunk
            return chunk
        }
    }

    fun close() {
        lock.withLock {
            closed = true
            notEmpty.signalAll()
        }
    }

    fun reset() {
        lock.withLock {
            readIndex = 0
            writeIndex = 0
            size = 0
            closed = false
        }
    }
}
