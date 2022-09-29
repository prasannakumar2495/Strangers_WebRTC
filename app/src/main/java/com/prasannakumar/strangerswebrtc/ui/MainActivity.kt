package com.prasannakumar.strangerswebrtc.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.prasannakumar.strangerswebrtc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val permissions =
        arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    private val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.find.setOnClickListener {
            if (isPermissionGranted())
                startActivity(Intent(this, Connecting::class.java))
            else
                askPermissions()
        }
    }

    private fun isPermissionGranted(): Boolean {
        for (permission in permissions) {
            return ActivityCompat.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED
        }
        return false
    }

    private fun askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }
}