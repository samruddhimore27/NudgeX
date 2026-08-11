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
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val confirmPass = binding.editConfirmPassword.text.toString().trim()

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
            val result = authRepository.signUp(email, password)
            showLoading(false)

            result.onSuccess { user ->
                Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                navigateToMain()
            }.onFailure { error ->
                Toast.makeText(this@SignUpActivity, "Sign Up Failed: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
            }
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
