// path: app/src/main/java/com/vocalbridge/StreamingService.kt
package com.vocalbridge

import android.app.*
import android.content.Intent
import android.media.*
import android.media.projection.MediaProjection
import android.os.IBinder
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import java.io.PipedInputStream
import java.io.PipedOutputStream

class StreamingService : Service() {
    private var server: AudioServer? = null
    private var audioRecord: AudioRecord? = null
    companion object {
        var projection: MediaProjection? = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "stream_channel"
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(NotificationChannel(channelId, "Streaming", NotificationManager.IMPORTANCE_LOW))

        startForeground(1, NotificationCompat.Builder(this, channelId)
            .setContentTitle("VocalBridge Live")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now).build())

        server = AudioServer(8080)
        server?.start()
        
        return START_STICKY
    }

    private inner class AudioServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            val pipedOut = PipedOutputStream()
            val pipedIn = PipedInputStream(pipedOut)

            Thread {
                try {
                    val config = AudioPlaybackCaptureConfiguration.Builder(projection!!)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                    
                    val format = AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()

                    audioRecord = AudioRecord.Builder()
                        .setAudioPlaybackCaptureConfig(config)
                        .setAudioFormat(format)
                        .build()

                    audioRecord?.startRecording()
                    val buffer = ByteArray(4096)
                    while (true) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (read > 0) pipedOut.write(buffer, 0, read)
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }.start()

            return newChunkedResponse(Response.Status.OK, "audio/wav", pipedIn)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        server?.stop()
        audioRecord?.stop()
        super.onDestroy()
    }
}
