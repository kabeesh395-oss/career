package com.example.careerpilot.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthUserState(
    val uid: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isAuthenticated: Boolean = false,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

class FirebaseAuthManager(private val context: Context) {

    private fun getAuthSafe(): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Throwable) {
            Log.w("FirebaseAuthManager", "Firebase Auth not available / not configured: ${e.message}")
            null
        }
    }

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }

    private val _userState = MutableStateFlow(AuthUserState())
    val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

    init {
        checkCurrentAuth()
        try {
            getAuthSafe()?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                updateUserState(user)
            }
        } catch (e: Throwable) {
            Log.w("FirebaseAuthManager", "Firebase Auth init note: ${e.message}")
        }
    }

    private fun checkCurrentAuth() {
        try {
            val current = getAuthSafe()?.currentUser
            updateUserState(current)
        } catch (e: Throwable) {
            Log.w("FirebaseAuthManager", "Firebase check auth error: ${e.message}")
            _userState.value = AuthUserState(
                uid = null,
                email = null,
                displayName = null,
                photoUrl = null,
                isAuthenticated = false,
                statusMessage = "Authentication unavailable: ${e.localizedMessage ?: "Service error"}"
            )
        }
    }

    private fun updateUserState(user: FirebaseUser?) {
        if (user != null) {
            _userState.value = AuthUserState(
                uid = user.uid,
                email = user.email,
                displayName = user.displayName ?: user.email?.substringBefore("@") ?: "Career Hub User",
                photoUrl = user.photoUrl?.toString(),
                isAuthenticated = true,
                statusMessage = "Authenticated"
            )
        } else {
            _userState.value = AuthUserState(
                uid = null,
                email = null,
                displayName = null,
                photoUrl = null,
                isAuthenticated = false,
                statusMessage = "Signed out."
            )
        }
    }

    /**
     * Email / Password Sign-In (Strict real authentication)
     */
    suspend fun signInWithEmailAndPassword(email: String, pass: String): Result<AuthUserState> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty."))
        }
        _userState.value = _userState.value.copy(isSyncing = true, statusMessage = "Authenticating account...")

        val auth = getAuthSafe()
            ?: return Result.failure(IllegalStateException("Firebase Auth is not configured on this device."))

        return try {
            val authResult = auth.signInWithEmailAndPassword(trimmedEmail, pass).await()
            val user = authResult.user
            if (user != null) {
                updateUserState(user)
                Result.success(_userState.value)
            } else {
                updateUserState(null)
                Result.failure(Exception("Authentication failed: No user record returned."))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Sign-in rejected: ${e.message}")
            updateUserState(null)
            Result.failure(e)
        }
    }

    /**
     * Email / Password Registration (Strict real registration)
     */
    suspend fun signUpWithEmailAndPassword(name: String, email: String, pass: String): Result<AuthUserState> {
        val trimmedEmail = email.trim()
        val cleanName = name.trim().ifEmpty { trimmedEmail.substringBefore("@") }
        if (trimmedEmail.isEmpty() || pass.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty."))
        }
        _userState.value = _userState.value.copy(isSyncing = true, statusMessage = "Creating Career Hub account...")

        val auth = getAuthSafe()
            ?: return Result.failure(IllegalStateException("Firebase Auth is not configured on this device."))

        return try {
            val authResult = auth.createUserWithEmailAndPassword(trimmedEmail, pass).await()
            val user = authResult.user
            if (user != null) {
                updateUserState(user)
                Result.success(_userState.value)
            } else {
                updateUserState(null)
                Result.failure(Exception("Registration failed: User could not be created."))
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthManager", "Registration rejected: ${e.message}")
            updateUserState(null)
            Result.failure(e)
        }
    }

    /**
     * Google Sign-In using Android Jetpack CredentialManager
     */
    suspend fun signInWithGoogle(webClientId: String? = null): Result<AuthUserState> {
        _userState.value = _userState.value.copy(isSyncing = true, statusMessage = "Initiating Google Sign-In...")

        val auth = getAuthSafe()
            ?: return Result.failure(IllegalStateException("Firebase Auth is not configured on this device."))

        return try {
            val serverClientId = webClientId ?: "default_client_id"
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                // Sign in to Firebase Auth with ID token
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val user = authResult.user

                updateUserState(user)
                Result.success(_userState.value)
            } else {
                Result.failure(Exception("Google Sign-In Credential not available."))
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthManager", "CredentialManager flow error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Sign out from Auth
     */
    fun signOut() {
        try {
            getAuthSafe()?.signOut()
        } catch (e: Throwable) {
            Log.w("FirebaseAuthManager", "Sign out note: ${e.message}")
        }
        updateUserState(null)
    }

    fun getCurrentUserId(): String? {
        return try {
            getAuthSafe()?.currentUser?.uid ?: _userState.value.uid
        } catch (e: Throwable) {
            _userState.value.uid
        }
    }
}
