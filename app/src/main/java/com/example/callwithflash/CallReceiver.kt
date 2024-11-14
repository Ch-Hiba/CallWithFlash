package com.example.callwithflash

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val preferences = context.getSharedPreferences("flash_preferences", Context.MODE_PRIVATE)
        val isFlashEnabled = preferences.getBoolean("flash_enabled", false)

        if (isFlashEnabled) {
            val callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (callState == TelephonyManager.EXTRA_STATE_RINGING) {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                }

                if (cameraId != null) {
                    try {
                        startFlashing(cameraManager, cameraId)
                    } catch (e: CameraAccessException) {
                        Log.e("CallReceiver", "Error accessing the camera", e)
                    }
                } else {
                    Log.e("CallReceiver", "No camera with flash available")
                }
            }
        }
    }

    private fun startFlashing(cameraManager: CameraManager, cameraId: String) {
        val handler = Handler(Looper.getMainLooper())
        val flashRunnable = object : Runnable {
            var flashCount = 0
            override fun run() {
                try {
                    if (flashCount < 10) {  // Flash 10 times
                        cameraManager.setTorchMode(cameraId, flashCount % 2 == 0)  // Alternate between on and off
                        flashCount++
                        handler.postDelayed(this, if (flashCount % 2 == 0) 500 else 300)  // Adjust delay for on/off
                    } else {
                        cameraManager.setTorchMode(cameraId, false)  // Ensure torch is off after flashing
                    }
                } catch (e: CameraAccessException) {
                    Log.e("CallReceiver", "Error during flashing", e)
                }
            }
        }
        handler.post(flashRunnable)
    }
}
