// path: app/src/main/java/com/vocalbridge/StreamingService.kt
package com.vocalbridge

import android.app.*
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class StreamingService : Service() {
    private var server: AudioServer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(1, notification)
        
        server = AudioServer(8080)
        server?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
        
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "vocal_bridge_stream"
        val channel = NotificationChannel(channelId, "Streaming", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("VocalBridge is Live")
            .setContentText("Broadcasting audio to local network...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        server?.stop()
        super.onDestroy()
    }

    private inner class AudioServer(port: Int) : NanoHTTPD(port) {
        override fun serve(session: IHTTPSession): Response {
            val pipedOut = PipedOutputStream()
            val pipedIn = PipedInputStream(pipedOut)
            
            // থ্রেড ব্যবহার করে মাইক্রোফোন থেকে ডেটা রিড করা
            Thread {
                val bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
                val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize)
                recorder.startRecording()
                val buffer = ByteArray(bufferSize)
                try {
                    while (true) {
                        val read = recorder.read(buffer, 0, bufferSize)
                        pipedOut.write(buffer, 0, read)
                    }
                } catch (e: Exception) { recorder.stop() }
            }.start()

            return理论Response(Response.Status.OK, "audio/wav", pipedIn, -1)
        }
    }
}
