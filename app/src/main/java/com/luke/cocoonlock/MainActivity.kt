package com.luke.cocoonlock

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.btnGrantOverlay).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnGrantAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(this, "Enable 'Cocoon Lock' in the list", Toast.LENGTH_LONG).show()
        }

        findViewById<Button>(R.id.btnSavePin).setOnClickListener {
            val pin = findViewById<EditText>(R.id.pinInput).text.toString()
            if (pin.length == 4) {
                PinStore.savePin(this, pin)
                Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show()
                updateStatus()
            } else {
                Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val hasPin = PinStore.hasPin(this)
        statusText.text = if (hasPin) {
            "PIN is set. Make sure Accessibility is enabled, then lock your device to test."
        } else {
            "1) Grant overlay permission  2) Enable accessibility service  3) Set a PIN below"
        }
    }
}
