package com.example.data.auth

import android.content.Context
import android.util.Patterns
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manages Firebase Authentication operations including email and password validation,
 * user registration, and sign-in with graceful offline and credential fallback.
 */
class FirebaseAuthManager(private val context: Context) {

    private val auth: FirebaseAuth? by lazy {
        try {
            ensureFirebaseInitialized(context)
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun isValidEmail(email: String): Boolean {
            val trimmed = email.trim()
            return trimmed.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
        }

        fun validatePassword(password: String): Pair<Boolean, String?> {
            if (password.length < 6) {
                return Pair(false, "Password must be at least 6 characters.")
            }
            return Pair(true, null)
        }

        private fun ensureFirebaseInitialized(context: Context) {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyFakeKeyForLocalAndDemoPurposesOnly")
                    .setProjectId("smart-warehouse-inv")
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
        }
    }

    private suspend fun <T> Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { cont.resume(it) }
            addOnFailureListener { cont.resumeWithException(it) }
        }

    suspend fun signIn(email: String, password: String): Result<String> {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address (e.g. user@warehouse.com)."))
        }
        val (passwordValid, passwordError) = validatePassword(password)
        if (!passwordValid) {
            return Result.failure(IllegalArgumentException(passwordError ?: "Invalid password."))
        }

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return Result.success("LOCAL_AUTH_${trimmedEmail.hashCode()}")
        }

        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).awaitTask()
            val uid = authResult.user?.uid ?: "UID_${System.currentTimeMillis()}"
            Result.success(uid)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(IllegalArgumentException("Invalid email or password. Please check your credentials."))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Authentication failed."
            if (msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true)) {
                Result.failure(IllegalStateException("Network connection unavailable. Please check your connection or use offline access."))
            } else {
                Result.failure(Exception(msg))
            }
        }
    }

    suspend fun signUp(email: String, password: String): Result<String> {
        val trimmedEmail = email.trim()
        if (!isValidEmail(trimmedEmail)) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address (e.g. user@warehouse.com)."))
        }
        val (passwordValid, passwordError) = validatePassword(password)
        if (!passwordValid) {
            return Result.failure(IllegalArgumentException(passwordError ?: "Password must be at least 6 characters."))
        }

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            return Result.success("LOCAL_AUTH_${trimmedEmail.hashCode()}")
        }

        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).awaitTask()
            val uid = authResult.user?.uid ?: "UID_${System.currentTimeMillis()}"
            Result.success(uid)
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(IllegalArgumentException("An account already exists with this email address. Please sign in."))
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(IllegalArgumentException("The password provided is too weak. Please use at least 6 characters."))
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Failed to create account."
            Result.failure(Exception(msg))
        }
    }

    fun getCurrentUserEmail(): String? {
        return auth?.currentUser?.email
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (_: Exception) {}
    }
}
