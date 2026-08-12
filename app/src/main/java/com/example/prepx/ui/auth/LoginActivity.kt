package com.example.prepx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prepx.data.auth.AuthRepository
import com.example.prepx.databinding.ActivityLoginBinding
import com.example.prepx.ui.main.MainActivity
import kotlinx.coroutines.launch

/**
 * Authentication screen for user login via Firebase Auth.
 * Automatically redirects active user sessions directly to MainActivity.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Auto-redirect logged-in users
        if (authRepository.isUserLoggedIn()) {
            navigateToMain()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonLogin.setOnClickListener {
            attemptLogin()
        }

        binding.textSignUpPrompt.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptLogin() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.editEmail.error = "Email address required"
            return
        }

        if (password.isEmpty()) {
            binding.editPassword.error = "Password required"
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            val result = authRepository.login(email, password)
            showLoading(false)

            result.onSuccess { user ->
                val display = user?.email ?: email
                Toast.makeText(this@LoginActivity, "Welcome, $display!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }.onFailure { error ->
                Toast.makeText(this@LoginActivity, getShortErrorMessage(error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getShortErrorMessage(error: Throwable?): String {
        val msg = error?.message ?: ""
        return when {
            msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true) ->
                "Invalid email format"
            msg.contains("wrong password", ignoreCase = true) || msg.contains("invalid credential", ignoreCase = true) || msg.contains("user not found", ignoreCase = true) ->
                "Invalid email or password"
            msg.contains("network", ignoreCase = true) || msg.contains("connect", ignoreCase = true) ->
                "Network connection error"
            else -> "Login failed. Please try again."
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
