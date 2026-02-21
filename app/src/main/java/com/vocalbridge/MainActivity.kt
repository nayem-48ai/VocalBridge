// path: app/src/main/java/com/vocalbridge/MainActivity.kt
package com.vocalbridge

import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import androidx.appcompat.app.AppCompatActivity
import com.vocalbridge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var isStreaming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btn = findViewById<Button>(R.id.btnToggle)
        val urlDisplay = findViewById<TextView>(R.id.urlText)

        btn.setOnClickListener {
            if (!isStreaming) {
                startService(Intent(this, StreamingService::class.java))
                val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
                urlDisplay.text = "URL: http://$ip:8080"
                btn.text = "STOP STREAMING"
                btn.setBackgroundColor(android.graphics.Color.RED)
            } else {
                stopService(Intent(this, StreamingService::class.java))
                btn.text = "START STREAMING"
                urlDisplay.text = "Server Stopped"
                btn.setBackgroundColor(android.graphics.Color.parseColor("#38BDF8"))
            }
            isStreaming = !isStreaming
        }
    }
}
