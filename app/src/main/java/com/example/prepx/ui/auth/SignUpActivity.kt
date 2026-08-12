package com.example.prepx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.prepx.data.auth.AuthRepository
import com.example.prepx.databinding.ActivitySignUpBinding
import com.example.prepx.ui.main.MainActivity
import kotlinx.coroutines.launch

/**
 * Account creation screen for new users via Firebase Auth.
 */
class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonSignUp.setOnClickListener {
            attemptSignUp()
        }

        binding.textLoginPrompt.setOnClickListener {
            finish()
        }
    }

    private fun attemptSignUp() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val confirmPass = binding.editConfirmPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.editName.error = "Full Name is required"
            return
        }

        if (email.isEmpty()) {
            binding.editEmail.error = "Email address required"
            return
        }

        if (password.length < 6) {
            binding.editPassword.error = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPass) {
            binding.editConfirmPassword.error = "Passwords do not match"
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            val result = authRepository.signUp(name, email, password)
            showLoading(false)

            result.onSuccess {
                Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }.onFailure { error ->
                Toast.makeText(this@SignUpActivity, getShortErrorMessage(error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getShortErrorMessage(error: Throwable?): String {
        val msg = error?.message ?: ""
        return when {
            msg.contains("badly formatted", ignoreCase = true) || msg.contains("invalid email", ignoreCase = true) ->
                "Invalid email format"
            msg.contains("already in use", ignoreCase = true) || msg.contains("already exists", ignoreCase = true) ->
                "Email already registered"
            msg.contains("weak password", ignoreCase = true) ->
                "Password is too weak"
            msg.contains("network", ignoreCase = true) || msg.contains("connect", ignoreCase = true) ->
                "Network connection error"
            else -> "Sign Up failed. Please check details."
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonSignUp.isEnabled = !isLoading
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
