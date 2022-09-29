package com.prasannakumar.strangerswebrtc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.prasannakumar.strangerswebrtc.databinding.ActivityWelcomeBinding

class Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            goToNextActivity()
        }

        binding.welcomeBtn.setOnClickListener {
            goToNextActivity()
        }
    }

    private fun goToNextActivity() {
        startActivity(Intent(this, Login::class.java))
        finishAffinity()
    }
}