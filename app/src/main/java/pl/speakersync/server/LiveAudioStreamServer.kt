package pl.speakersync.server

import fi.iki.elonen.NanoHTTPD
import pl.speakersync.audio.PcmRingBuffer
import java.io.InputStream

class LivePcmInputStream(
    private val ringBuffer: PcmRingBuffer
) : InputStream() {
    override fun read(): Int {
        val single = ByteArray(1)
        val count = ringBuffer.readBlocking(single, 0, 1)
        return if (count <= 0) -1 else single[0].toInt() and 0xFF
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return ringBuffer.readBlocking(buffer, offset, length)
    }
}

class LiveAudioStreamServer(
    port: Int,
    private val ringBuffer: PcmRingBuffer,
    private val mimeType: String,
    private val streamPath: String
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        if (session.uri != streamPath) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not found")
        }

        val inputStream = LivePcmInputStream(ringBuffer)
        val response = newChunkedResponse(Response.Status.OK, mimeType, inputStream)
        response.addHeader("Cache-Control", "no-cache, no-store")
        response.addHeader("Connection", "keep-alive")
        response.addHeader("Access-Control-Allow-Origin", "*")
        return response
    }
}
