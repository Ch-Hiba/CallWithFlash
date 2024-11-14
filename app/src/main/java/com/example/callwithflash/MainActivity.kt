package com.example.callwithflash

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 101
    private lateinit var callReceiver: CallReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the CallReceiver
        callReceiver = CallReceiver()
        registerReceiver(callReceiver, IntentFilter("android.intent.action.PHONE_STATE"))

        // Check and request necessary permissions
        if (!hasAllPermissions()) {
            requestRequiredPermissions()
        } else {
            showToast("All permissions granted")
        }

        // Set up the switch to toggle flash functionality
        val preferences = getSharedPreferences("flash_preferences", Context.MODE_PRIVATE)
        val flashSwitch = findViewById<SwitchCompat>(R.id.swtch)

        flashSwitch.isChecked = preferences.getBoolean("flash_enabled", false)
        flashSwitch.setOnCheckedChangeListener { _, isEnabled ->
            preferences.edit().putBoolean("flash_enabled", isEnabled).apply()
            showToast("Flash on incoming calls is ${if (isEnabled) "enabled" else "disabled"}")
        }
    }

    private fun hasAllPermissions(): Boolean {
        val isCameraPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED
        val isPhoneStatePermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PERMISSION_GRANTED
        return isCameraPermissionGranted && isPhoneStatePermissionGranted
    }

    private fun requestRequiredPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PERMISSION_GRANTED }) {
                showToast("Permissions granted")
            } else {
                showToast("Permissions denied")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(callReceiver)
    }
}
