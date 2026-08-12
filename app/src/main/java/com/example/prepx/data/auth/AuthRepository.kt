package com.example.prepx.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repository wrapping Firebase Authentication services for PrepX.
 * Includes local fallback authentication so local testing works seamlessly.
 */
class AuthRepository {

    companion object {
        private const val TAG = "PrepX_Auth"
        private var mockUserEmail: String? = null
        private var mockUserId: String? = null
        private var mockUserName: String? = null
    }

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Auth not configured. Using local auth fallback mode.")
            null
        }
    }

    /**
     * Checks whether a user session is active.
     */
    fun isUserLoggedIn(): Boolean {
        return firebaseAuth?.currentUser != null || mockUserEmail != null
    }

    /**
     * Returns current user UID.
     */
    fun getCurrentUserId(): String {
        return firebaseAuth?.currentUser?.uid ?: mockUserId ?: "prepx_local_user"
    }

    /**
     * Returns current user email.
     */
    fun getCurrentUserEmail(): String {
        return firebaseAuth?.currentUser?.email ?: mockUserEmail ?: "prepx_user@example.com"
    }

    /**
     * Returns current user Full Name or display name.
     */
    fun getCurrentUserName(): String {
        val firebaseName = firebaseAuth?.currentUser?.displayName
        if (!firebaseName.isNullOrBlank()) return firebaseName
        if (!mockUserName.isNullOrBlank()) return mockUserName!!
        val emailPrefix = getCurrentUserEmail().substringBefore("@")
        return emailPrefix.replaceFirstChar { it.uppercase() }
    }

    /**
     * Authenticates an existing user with email and password via Firebase Auth.
     */
    suspend fun login(email: String, pass: String): Result<FirebaseUser?> {
        mockUserEmail = email.trim()
        mockUserId = "user_${email.hashCode()}"
        mockUserName = null

        val auth = firebaseAuth
        if (auth != null) {
            return try {
                Log.d(TAG, "Attempting Firebase login for email: $email")
                val authResult = auth.signInWithEmailAndPassword(email, pass).await()
                Result.success(authResult.user)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase login error: ${e.localizedMessage}")
                Result.failure(e)
            }
        }
        
        // Local Fallback Login for unconfigured testing environment
        Log.d(TAG, "Local login successful for $email")
        return Result.success(null)
    }

    /**
     * Registers a new user account with Full Name, email and password via Firebase Auth.
     */
    suspend fun signUp(name: String, email: String, pass: String): Result<FirebaseUser?> {
        val auth = firebaseAuth
        mockUserName = name.trim()
        mockUserEmail = email.trim()
        mockUserId = "user_${email.hashCode()}"

        if (auth != null) {
            return try {
                Log.d(TAG, "Attempting Firebase signup for name: $name, email: $email")
                val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
                val user = authResult.user
                if (user != null && name.isNotBlank()) {
                    try {
                        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                            displayName = name.trim()
                        }
                        user.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Profile name update warning: ${e.localizedMessage}")
                    }
                }
                Result.success(user)
            } catch (e: Exception) {
                Log.e(TAG, "Firebase signup error: ${e.localizedMessage}")
                Result.failure(e)
            }
        }

        Log.d(TAG, "Local signup successful for $name ($email)")
        return Result.success(null)
    }

    /**
     * Signs out the currently authenticated user session.
     */
    fun logout() {
        Log.d(TAG, "Signing out user")
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            // Ignore
        }
        mockUserEmail = null
        mockUserId = null
        mockUserName = null
    }
}
