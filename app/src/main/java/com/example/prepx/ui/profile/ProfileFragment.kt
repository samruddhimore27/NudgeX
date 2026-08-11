package com.example.prepx.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.prepx.data.auth.AuthRepository
import com.example.prepx.data.model.ItemType
import com.example.prepx.databinding.FragmentProfileBinding
import com.example.prepx.ui.PlannerViewModel
import com.example.prepx.ui.auth.LoginActivity
import kotlinx.coroutines.launch

/**
 * Profile screen managing user session, dark/light theme choice, and coding profile handles.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlannerViewModel by activityViewModels()
    private val authRepository = AuthRepository()

    companion object {
        private const val PREFS_NAME = "prepx_handles_prefs"
        private const val KEY_CF_HANDLE = "codeforces_handle"
        private const val KEY_CC_HANDLE = "codechef_handle"
        private const val KEY_LC_HANDLE = "leetcode_handle"
        private const val KEY_THEME_MODE = "app_theme_mode"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUserInfo()
        loadSavedSettings()
        observeStats()
        setupListeners()
    }

    private fun setupUserInfo() {
        binding.textUserEmail.text = authRepository.getCurrentUserEmail()
        val uid = authRepository.getCurrentUserId()
        binding.textUserId.text = if (uid.isNotEmpty()) "UID: $uid" else "Guest Session"
    }

    private fun loadSavedSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Handles
        binding.editCodeforcesHandle.setText(prefs.getString(KEY_CF_HANDLE, ""))
        binding.editCodeChefHandle.setText(prefs.getString(KEY_CC_HANDLE, ""))
        binding.editLeetCodeHandle.setText(prefs.getString(KEY_LC_HANDLE, ""))

        // Theme Mode
        val savedTheme = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val isNight = when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                val currentUiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
                currentUiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
        binding.switchDarkTheme.isChecked = isNight
    }

    private fun applyTheme(nightMode: Int) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, nightMode).apply()
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun saveHandles() {
        val cf = binding.editCodeforcesHandle.text.toString().trim()
        val cc = binding.editCodeChefHandle.text.toString().trim()
        val lc = binding.editLeetCodeHandle.text.toString().trim()

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString(KEY_CF_HANDLE, cf)
            putString(KEY_CC_HANDLE, cc)
            putString(KEY_LC_HANDLE, lc)
            apply()
        }

        Toast.makeText(requireContext(), "Coding handles saved! 💾", Toast.LENGTH_SHORT).show()
    }

    private fun viewCodingProfiles() {
        val cf = binding.editCodeforcesHandle.text.toString().trim()
        val cc = binding.editCodeChefHandle.text.toString().trim()
        val lc = binding.editLeetCodeHandle.text.toString().trim()

        val targetUrl = when {
            cf.isNotEmpty() -> "https://codeforces.com/profile/$cf"
            cc.isNotEmpty() -> "https://www.codechef.com/users/$cc"
            lc.isNotEmpty() -> "https://leetcode.com/$lc"
            else -> null
        }

        if (targetUrl != null) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Could not open browser for profile.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please enter at least one username first!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.allItems.collect { items ->
                        val total = items.size
                        val completed = items.count { it.isCompleted }
                        val completedGoals = items.count { it.isCompleted && it.type == ItemType.GOAL }

                        binding.textStatTotal.text = "$total"
                        binding.textStatCompleted.text = "$completed"
                        binding.textStatGoals.text = "$completedGoals"
                    }
                }

                launch {
                    viewModel.streakCount.collect { streak ->
                        binding.textStatStreak.text = "$streak Days"
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            val targetMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentSaved = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

            if (currentSaved != targetMode) {
                applyTheme(targetMode)
                val themeName = if (isChecked) "Dark Mode 🌙" else "Light Mode ☀️"
                Toast.makeText(requireContext(), "Theme changed to $themeName", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonSaveHandles.setOnClickListener {
            saveHandles()
        }

        binding.buttonViewProfiles.setOnClickListener {
            viewCodingProfiles()
        }

        binding.buttonSignOut.setOnClickListener {
            authRepository.logout()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
