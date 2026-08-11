package com.example.prepx.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.prepx.data.auth.AuthRepository
import com.example.prepx.databinding.ActivitySplashBinding
import com.example.prepx.ui.auth.LoginActivity
import com.example.prepx.ui.main.MainActivity

/**
 * Fullscreen splash screen showing the PrepX logo and slogan for 3 seconds before navigating to Login screen.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait for 3 seconds (3000 ms) then navigate to Login / Main screen
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                val nextIntent = if (authRepository.isUserLoggedIn()) {
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    Intent(this@SplashActivity, LoginActivity::class.java)
                }
                startActivity(nextIntent)
                finish()
            }
        }, 3000L)
    }
}
