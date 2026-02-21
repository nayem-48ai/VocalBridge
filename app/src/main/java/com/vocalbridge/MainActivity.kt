// path: app/src/main/java/com/vocalbridge/MainActivity.kt
package com.vocalbridge

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var projectionManager: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val btn = findViewById<Button>(R.id.btnToggle)
        val urlTxt = findViewById<TextView>(R.id.urlText)

        btn.setOnClickListener {
            // ইন্টারনাল অডিওর জন্য স্ক্রিন ক্যাপচার পারমিশন লাগে
            startActivityForResult(projectionManager.createScreenCaptureIntent(), 100)
        }
        
        val wm = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        urlTxt.text = "http://$ip:8080"
    }
}
