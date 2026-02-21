// path: app/src/main/java/com/vocalbridge/MainActivity.kt
package com.vocalbridge

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var mpm: MediaProjectionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        findViewById<Button>(R.id.btnToggle).setOnClickListener {
            startActivityForResult(mpm.createScreenCaptureIntent(), 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            StreamingService.projection = mpm.getMediaProjection(resultCode, data)
            startService(Intent(this, StreamingService::class.java))
        }
    }
}
